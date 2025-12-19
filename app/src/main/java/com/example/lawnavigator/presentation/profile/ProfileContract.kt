package com.example.lawnavigator.presentation.profile

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.UserAnalytics
import com.example.lawnavigator.presentation.theme.ThemeMode

class ProfileContract {

    data class State(
        val email: String = "",
        val analytics: UserAnalytics? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,

        // --- НОВЫЕ ПОЛЯ ---
        val showJoinGroupDialog: Boolean = false,
        val inviteCodeInput: String = ""
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnRefresh : Event()
        data object OnLogoutClicked : Event()
        data class OnRecommendationClicked(val topicId: Int) : Event()
        data object OnBackClicked : Event()
        data class OnThemeChanged(val mode: ThemeMode) : Event()

        // --- НОВЫЕ СОБЫТИЯ ---
        data object OnJoinGroupClicked : Event() // Открыть диалог
        data class OnInviteCodeChanged(val code: String) : Event() // Ввод текста
        data object OnConfirmJoinGroup : Event() // Нажать "Вступить"
        data object OnDismissDialog : Event() // Закрыть
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateToLogin : Effect()
        data object NavigateBack : Effect()
        data class NavigateToTopic(val topicId: Int) : Effect()

        // --- НОВЫЙ ЭФФЕКТ ---
        data class ShowMessage(val msg: String) : Effect()
    }
}