package com.example.lawnavigator.presentation.lecture

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.Lecture

class LectureContract {
    data class State(
        val lecture: Lecture? = null,
        val isLoading: Boolean = false,
        val isFavorite: Boolean = false,
        val initialScrollIndex: Int = 0,
        val searchQuery: String? = null,

        // --- ДЛЯ УЧИТЕЛЯ ---
        val isTeacher: Boolean = false,

        // Редактирование
        val isEditing: Boolean = false,
        val editedTitle: String = "",
        val editedContent: String = "",

        // Удаление
        val showDeleteDialog: Boolean = false
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnFavoriteClicked : Event()
        data class OnSaveProgress(val scrollIndex: Int) : Event()

        // Редактирование
        data object OnEditClicked : Event()
        data object OnSaveEditsClicked : Event()
        data object OnCancelEditClicked : Event()
        data class OnTitleChanged(val newTitle: String) : Event()
        data class OnContentChanged(val newContent: String) : Event()

        // Удаление
        data object OnDeleteClicked : Event()
        data object OnConfirmDelete : Event()
        data object OnDismissDeleteDialog : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class ShowMessage(val msg: String) : Effect()
    }
}