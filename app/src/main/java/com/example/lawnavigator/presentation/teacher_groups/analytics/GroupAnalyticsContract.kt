package com.example.lawnavigator.presentation.teacher_groups.analytics

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.data.dto.StudentRiskDto

class GroupAnalyticsContract {
    data class State(
        val students: List<StudentRiskDto> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnRefresh : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
    }
}