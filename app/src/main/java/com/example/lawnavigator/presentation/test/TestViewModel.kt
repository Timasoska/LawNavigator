package com.example.lawnavigator.presentation.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.SubmitTestUseCase
import com.example.lawnavigator.domain.usecase.TestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val testUseCase: TestUseCase,
    private val submitTestUseCase: SubmitTestUseCase, // <--- ДОБАВЬ ЭТО
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TestContract.State, TestContract.Event, TestContract.Effect>() {

    private val topicId: Int = checkNotNull(savedStateHandle["topicId"])


    override fun createInitialState() = TestContract.State()

    init {
        loadTest()
    }

    override fun handleEvent(event: TestContract.Event) {
        when (event) {
            is TestContract.Event.OnAnswerSelected -> handleAnswerSelection(event.questionId, event.answerId)
            is TestContract.Event.OnNextClicked -> handleNextClick()
            is TestContract.Event.OnBackClicked -> setEffect { TestContract.Effect.NavigateBack }
            // Новое событие: переход в режим просмотра ошибок
            is TestContract.Event.OnReviewClicked -> {
                setState { copy(isReviewMode = true, resultScore = null, currentQuestionIndex = 0) }
            }
        }
    }

    private fun handleAnswerSelection(questionId: Int, answerId: Int) {
        // Если мы в режиме просмотра, менять ответы нельзя
        if (currentState.isReviewMode) return

        val question = currentState.test?.questions?.find { it.id == questionId } ?: return
        val currentSelection = currentState.selectedAnswers[questionId] ?: emptySet()

        val newSelection = if (question.isMultipleChoice) {
            // Логика ЧЕКБОКСА (Множественный выбор)
            if (currentSelection.contains(answerId)) {
                currentSelection - answerId // Если уже выбран -> убираем
            } else {
                currentSelection + answerId // Если не выбран -> добавляем
            }
        } else {
            // Логика РАДИОКНОПКИ (Одиночный выбор)
            setOf(answerId)
        }

        val newMap = currentState.selectedAnswers.toMutableMap()
        newMap[questionId] = newSelection
        setState { copy(selectedAnswers = newMap) }
    }

    private fun handleNextClick() {
        val test = currentState.test ?: return

        // В режиме просмотра кнопка "Далее" просто листает, а в конце выходит
        if (currentState.isReviewMode) {
            if (currentState.currentQuestionIndex < test.questions.lastIndex) {
                setState { copy(currentQuestionIndex = currentState.currentQuestionIndex + 1) }
            } else {
                setEffect { TestContract.Effect.NavigateBack }
            }
            return
        }

        // Обычный режим прохождения
        if (currentState.currentQuestionIndex < test.questions.lastIndex) {
            setState { copy(currentQuestionIndex = currentState.currentQuestionIndex + 1) }
        } else {
            submitTest()
        }
    }

    private fun loadTest() {
        android.util.Log.d("TestDebug", "Запрос теста для topicId: $topicId") // <--- ЛОГ 1

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            testUseCase.loadTest(topicId)
                .onSuccess { test ->
                    android.util.Log.d("TestDebug", "Успех! Получен тест: ${test.title}, вопросов: ${test.questions.size}") // <--- ЛОГ 2
                    setState { copy(isLoading = false, test = test) } }
                .onFailure { error ->
                    android.util.Log.e("TestDebug", "Ошибка загрузки: ${error.message}", error) // <--- ЛОГ 3
                    setState { copy(isLoading = false) } }
        }
    }

    private fun submitTest() {
        val testId = currentState.test?.id ?: return
        setState { copy(isLoading = true) }

        viewModelScope.launch {
            submitTestUseCase(0, testId, currentState.selectedAnswers)
                .onSuccess { result ->
                    setState {
                        copy(
                            isLoading = false,
                            resultScore = result.score,
                            resultMessage = result.message,
                            // Сохраняем правильные ответы для подсветки ошибок
                            correctAnswersMap = result.correctAnswers
                        )
                    }
                }
                .onFailure { setState { copy(isLoading = false) } }
        }
    }
}