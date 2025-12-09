package com.example.lawnavigator.presentation.profile

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.UserAnalytics

class ProfileContract {

    /**
     * Состояние экрана профиля.
     */
    data class State(
        val email: String = "student@example.com", // В идеале email тоже хранить в DataStore
        val analytics: UserAnalytics? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState

    /**
     * Действия пользователя.
     */
    sealed class Event : ViewIntent {
        data object OnLogoutClicked : Event()
        data class OnRecommendationClicked(val topicId: Int) : Event()
        data object NavigateBack : Effect() // <--- Добавили
        data object OnBackClicked : Event() // <--- Добавили
    }

    /**
     * Эффекты (Навигация).
     */
    sealed class Effect : ViewSideEffect {
        data object NavigateToLogin : Effect()
        data object NavigateBack : Effect() // <--- Добавили
        data class NavigateToTopic(val topicId: Int) : Effect()
    }
}