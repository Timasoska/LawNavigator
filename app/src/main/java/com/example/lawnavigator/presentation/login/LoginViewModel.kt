package com.example.lawnavigator.presentation.login

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.LoginUseCase
import com.example.lawnavigator.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : BaseViewModel<LoginContract.State, LoginContract.Event, LoginContract.Effect>() {

    override fun createInitialState() = LoginContract.State()

    override fun handleEvent(event: LoginContract.Event) {
        when (event) {
            is LoginContract.Event.OnEmailChanged -> {
                setState { copy(email = event.email, error = null) }
            }
            is LoginContract.Event.OnPasswordChanged -> {
                setState { copy(password = event.password, error = null) }
            }
            is LoginContract.Event.OnLoginClicked -> login()
            is LoginContract.Event.OnRegisterClicked -> register()
            is LoginContract.Event.OnErrorShown -> {
                setState { copy(error = null) }
            }
        }
    }

    private fun login() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            val email = currentState.email
            val password = currentState.password

            // Вызываем UseCase
            val result = loginUseCase(email, password)

            setState { copy(isLoading = false) }

            result.fold(
                onSuccess = {
                    setEffect { LoginContract.Effect.NavigateToHome }
                },
                onFailure = { error ->
                    setState { copy(error = error.message ?: "Ошибка входа") }
                }
            )
        }
    }

    private fun register() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            val result = registerUseCase(currentState.email, currentState.password)
            setState { copy(isLoading = false) }

            result.fold(
                onSuccess = {
                    // После успешной регистрации сразу пускаем внутрь
                    setEffect { LoginContract.Effect.NavigateToHome }
                },
                onFailure = { error ->
                    setState { copy(error = error.message ?: "Ошибка регистрации") }
                }
            )
        }
    }
}