package com.example.lawnavigator.presentation.lectures_list

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.Lecture

class LecturesListContract {
    data class State(
        val lectures: List<Lecture> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data class OnLectureClicked(val lectureId: Int) : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class NavigateToLecture(val lectureId: Int) : Effect()
    }
}