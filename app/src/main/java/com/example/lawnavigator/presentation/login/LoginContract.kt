package com.example.lawnavigator.presentation.login

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState

class LoginContract {

    // Состояние: Добавили флаг видимости пароля
    data class State(
        val email: String = "",
        val password: String = "",
        val isPasswordVisible: Boolean = false, // <--- НОВОЕ ПОЛЕ
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState

    // Намерения: Добавили событие переключения
    sealed class Event : ViewIntent {
        data class OnEmailChanged(val email: String) : Event()
        data class OnPasswordChanged(val password: String) : Event()
        data object OnTogglePasswordVisibility : Event() // <--- НОВОЕ СОБЫТИЕ
        data object OnLoginClicked : Event()
        data object OnRegisterClicked : Event()
        data object OnErrorShown : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateToHome : Effect()
    }
}