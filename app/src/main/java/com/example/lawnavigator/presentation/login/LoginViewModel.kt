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
            // Ввод текста
            is LoginContract.Event.OnEmailChanged -> setState { copy(email = event.email, error = null) }
            is LoginContract.Event.OnPasswordChanged -> setState { copy(password = event.password, error = null) }
            is LoginContract.Event.OnNameChanged -> setState { copy(name = event.name, error = null) }
            is LoginContract.Event.OnConfirmPasswordChanged -> setState { copy(confirmPassword = event.pass, error = null) }
            is LoginContract.Event.OnInviteCodeChanged -> setState { copy(inviteCode = event.code, error = null) }

            // Переключатели
            is LoginContract.Event.OnTogglePasswordVisibility -> setState { copy(isPasswordVisible = !isPasswordVisible) }
            is LoginContract.Event.OnToggleMode -> setState {
                // Сбрасываем лишние поля при смене режима
                copy(isRegisterMode = !isRegisterMode, error = null, isTeacherMode = false, inviteCode = "")
            }
            is LoginContract.Event.OnToggleTeacherMode -> setState { copy(isTeacherMode = !isTeacherMode) }

            // Нажатие главной кнопки
            is LoginContract.Event.OnSubmitClicked -> {
                if (currentState.isRegisterMode) register() else login()
            }

            is LoginContract.Event.OnErrorShown -> setState { copy(error = null) }
        }
    }

    private fun login() {
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            setState { copy(error = "Заполните все поля") }
            return
        }

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            loginUseCase(currentState.email, currentState.password).fold(
                onSuccess = { setEffect { LoginContract.Effect.NavigateToHome } },
                onFailure = { setState { copy(error = it.message, isLoading = false) } }
            )
        }
    }

    private fun register() {
        val s = currentState

        // Валидация на клиенте
        if (s.name.isBlank()) { setState { copy(error = "Введите имя") }; return }
        if (s.email.isBlank()) { setState { copy(error = "Введите email") }; return }
        if (s.password.length < 6) { setState { copy(error = "Пароль слишком короткий") }; return }
        if (s.password != s.confirmPassword) { setState { copy(error = "Пароли не совпадают") }; return }
        if (s.isTeacherMode && s.inviteCode.isBlank()) { setState { copy(error = "Введите код приглашения") }; return }

        setState { copy(isLoading = true) }

        val codeToSend = if (s.isTeacherMode) s.inviteCode else null

        viewModelScope.launch {
            registerUseCase(s.email, s.password, s.name, codeToSend).fold(
                onSuccess = { setEffect { LoginContract.Effect.NavigateToHome } },
                onFailure = { setState { copy(error = it.message, isLoading = false) } }
            )
        }
    }
}