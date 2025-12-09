package com.example.lawnavigator.presentation.login

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState

class LoginContract {

    // Состояние: Данные, которые мы рисуем
    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState

    // Намерения: Действия пользователя
    sealed class Event : ViewIntent {
        data class OnEmailChanged(val email: String) : Event()
        data class OnPasswordChanged(val password: String) : Event()
        data object OnLoginClicked : Event()
        data object OnRegisterClicked : Event()
        data object OnErrorShown : Event() // Сброс ошибки (например, после показа Snackbar)
    }

    // Эффекты: Навигация
    sealed class Effect : ViewSideEffect {
        data object NavigateToHome : Effect()
    }
}