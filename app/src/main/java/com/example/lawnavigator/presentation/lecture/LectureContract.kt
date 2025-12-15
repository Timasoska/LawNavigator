package com.example.lawnavigator.presentation.lecture

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.Lecture

class LectureContract {
    data class State(
        val lecture: Lecture? = null,
        val isLoading: Boolean = false,
        val isFavorite: Boolean = false, // Локальное состояние кнопки
        val initialScrollIndex: Int = 0 // <--- Позиция для авто-скролла
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnFavoriteClicked : Event()
        data class OnSaveProgress(val scrollIndex: Int) : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class ShowMessage(val msg: String) : Effect()
    }
}