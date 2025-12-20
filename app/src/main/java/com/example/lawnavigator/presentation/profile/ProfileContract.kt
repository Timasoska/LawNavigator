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

        val showJoinGroupDialog: Boolean = false,
        val inviteCodeInput: String = "",

        // --- НОВЫЕ ПОЛЯ ДЛЯ СПИСКА УЧАСТНИКОВ ---
        val showMembersDialog: Boolean = false,
        val groupMembers: List<String> = emptyList()
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnRefresh : Event()
        data object OnLogoutClicked : Event()
        data class OnRecommendationClicked(val topicId: Int) : Event()
        data object OnBackClicked : Event()
        data class OnThemeChanged(val mode: ThemeMode) : Event()

        data object OnJoinGroupClicked : Event()
        data class OnInviteCodeChanged(val code: String) : Event()
        data object OnConfirmJoinGroup : Event()
        data object OnDismissDialog : Event()

        // Клик по группе (нужен ID, поэтому изменим DTO/Domain позже, пока используем заглушку ID)
        data class OnGroupClicked(val groupId: Int) : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateToLogin : Effect()
        data object NavigateBack : Effect()
        data class NavigateToTopic(val topicId: Int) : Effect()
        data class ShowMessage(val msg: String) : Effect()
    }
}