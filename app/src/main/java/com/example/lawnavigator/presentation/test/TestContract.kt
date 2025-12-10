package com.example.lawnavigator.presentation.test

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.TestContent

class TestContract {

    data class State(
        val test: TestContent? = null,
        val currentQuestionIndex: Int = 0, // <--- Добавили индекс текущего вопроса
        val selectedAnswers: Map<Int, Int> = emptyMap(), // QuestionId -> AnswerId
        val isLoading: Boolean = false,
        val resultScore: Int? = null,
        val resultMessage: String? = null
    ) : ViewState

    sealed class Event : ViewIntent {
        data class OnAnswerSelected(val questionId: Int, val answerId: Int) : Event()
        data object OnNextClicked : Event() // <--- Кнопка "Далее" (или "Завершить")
        data object OnBackClicked : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
    }
}