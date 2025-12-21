package com.example.lawnavigator.presentation.home

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.model.EngagementStatus

class HomeContract {

    data class State(
        val disciplines: List<Discipline> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isTeacher: Boolean = false,
        val engagementStatus: EngagementStatus? = null,
        val userName: String = "User" // <--- НОВОЕ ПОЛЕ
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnRetryClicked : Event()
        data class OnDisciplineClicked(val disciplineId: Int) : Event()
        data object OnTeacherGroupsClicked : Event()
        data object OnRefresh : Event() // <--- ДОБАВЛЕНО
    }

    sealed class Effect : ViewSideEffect {
        data class NavigateToTopics(val disciplineId: Int) : Effect()
        data object NavigateToTeacherGroups : Effect()
    }
}