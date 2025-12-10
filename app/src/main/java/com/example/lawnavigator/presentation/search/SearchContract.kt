package com.example.lawnavigator.presentation.search

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.Lecture

class SearchContract {
    data class State(
        val query: String = "",
        val results: List<Lecture> = emptyList(),
        val isLoading: Boolean = false
    ) : ViewState

    sealed class Event : ViewIntent {
        data class OnQueryChanged(val query: String) : Event()
        data class OnLectureClicked(val lectureId: Int) : Event()
        data object OnBackClicked : Event()
    }

    sealed class Effect : ViewSideEffect {
        data class NavigateToLecture(val lectureId: Int) : Effect()
        data object NavigateBack : Effect()
    }
}