package com.example.lawnavigator.presentation.login

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState

class LoginContract {

    data class State(
        // Основные поля
        val email: String = "",
        val password: String = "",

        // Новые поля для регистрации
        val name: String = "",
        val confirmPassword: String = "",
        val inviteCode: String = "",

        // Состояния UI
        val isRegisterMode: Boolean = false, // true = Регистрация, false = Вход
        val isTeacherMode: Boolean = false,  // true = Показать поле Invite Code
        val isPasswordVisible: Boolean = false,

        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState

    sealed class Event : ViewIntent {
        // Ввод данных
        data class OnEmailChanged(val email: String) : Event()
        data class OnPasswordChanged(val password: String) : Event()
        data class OnNameChanged(val name: String) : Event()
        data class OnConfirmPasswordChanged(val pass: String) : Event()
        data class OnInviteCodeChanged(val code: String) : Event()

        // Переключатели
        data object OnToggleMode : Event() // Вход <-> Регистрация
        data object OnToggleTeacherMode : Event() // Студент <-> Учитель
        data object OnTogglePasswordVisibility : Event()

        // Действия
        data object OnSubmitClicked : Event() // Единая кнопка (Войти или Создать)
        data object OnErrorShown : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateToHome : Effect()
    }
}