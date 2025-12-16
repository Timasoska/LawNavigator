package com.example.lawnavigator.presentation.test_creator

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.QuestionDraft
import com.example.lawnavigator.domain.model.TestDraft

class TestCreatorContract {
    data class State(
        val isLoading: Boolean = false,
        // Черновик теста
        val testDraft: TestDraft,

        // Управление диалогом вопроса
        val isQuestionDialogOpen: Boolean = false,
        val editingQuestion: QuestionDraft? = null // Если null - создаем новый, иначе редактируем
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnSaveTestClicked : Event()

        // Изменение метаданных
        data class OnTitleChanged(val title: String) : Event()
        data class OnTimeLimitChanged(val minutes: String) : Event()

        // Работа с вопросами
        data object OnAddQuestionClicked : Event() // Открыть диалог для нового
        data class OnEditQuestionClicked(val question: QuestionDraft) : Event() // Открыть для старого
        data class OnDeleteQuestionClicked(val questionId: String) : Event()

        // Событие из диалога (Сохранить вопрос в список)
        data class OnSaveQuestion(val question: QuestionDraft) : Event()
        data object OnCloseDialog : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class ShowMessage(val msg: String) : Effect()
    }
}