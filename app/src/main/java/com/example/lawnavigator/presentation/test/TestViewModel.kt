package com.example.lawnavigator.presentation.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.SubmitTestUseCase
import com.example.lawnavigator.domain.usecase.TestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


@HiltViewModel
class TestViewModel @Inject constructor(
    private val testUseCase: TestUseCase,
    private val submitTestUseCase: SubmitTestUseCase,
    private val getTestByLectureUseCase: com.example.lawnavigator.domain.usecase.GetTestByLectureUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TestContract.State, TestContract.Event, TestContract.Effect>() {

    // Читаем аргументы
    private val topicIdArg: Int = savedStateHandle["topicId"] ?: -1
    private val lectureIdArg: Int = savedStateHandle["lectureId"] ?: -1
    private var timerJob: Job? = null

    override fun createInitialState() = TestContract.State()

    init {
        loadTest()
    }

    override fun handleEvent(event: TestContract.Event) {
        when (event) {
            is TestContract.Event.OnAnswerSelected -> handleAnswerSelection(event.questionId, event.answerId)
            is TestContract.Event.OnNextClicked -> handleNextClick()
            is TestContract.Event.OnBackClicked -> setEffect { TestContract.Effect.NavigateBack }
            is TestContract.Event.OnReviewClicked -> {
                setState { copy(isReviewMode = true, resultScore = null, currentQuestionIndex = 0) }
            }
            is TestContract.Event.OnTimerTick -> {
                val current = currentState.timeLeft
                if (current != null && current > 0) {
                    setState { copy(timeLeft = current - 1) }
                    if (current - 1 == 0) {
                        setEvent(TestContract.Event.OnTimeExpired)
                    }
                }
            }
            is TestContract.Event.OnTimeExpired -> {
                stopTimer()
                submitTest()
            }
        }
    }

    private fun handleAnswerSelection(questionId: Int, answerId: Int) {
        if (currentState.isReviewMode) return

        val question = currentState.test?.questions?.find { it.id == questionId } ?: return
        val currentSelection = currentState.selectedAnswers[questionId] ?: emptySet()

        val newSelection = if (question.isMultipleChoice) {
            if (currentSelection.contains(answerId)) currentSelection - answerId else currentSelection + answerId
        } else {
            setOf(answerId)
        }

        val newMap = currentState.selectedAnswers.toMutableMap()
        newMap[questionId] = newSelection
        setState { copy(selectedAnswers = newMap) }
    }

    private fun handleNextClick() {
        val test = currentState.test ?: return

        if (currentState.isReviewMode) {
            if (currentState.currentQuestionIndex < test.questions.lastIndex) {
                setState { copy(currentQuestionIndex = currentState.currentQuestionIndex + 1) }
            } else {
                setEffect { TestContract.Effect.NavigateBack }
            }
            return
        }

        if (currentState.currentQuestionIndex < test.questions.lastIndex) {
            setState { copy(currentQuestionIndex = currentState.currentQuestionIndex + 1) }
        } else {
            submitTest()
        }
    }

    private fun loadTest() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            // Определяем, какой UseCase вызывать
            val result = if (lectureIdArg != -1) {
                // Если передан lectureId -> грузим тест лекции
                getTestByLectureUseCase(lectureIdArg)
            } else if (topicIdArg != -1) {
                // Если передан topicId -> грузим тест темы
                testUseCase.loadTest(topicIdArg)
            } else {
                Result.failure(Exception("No ID provided"))
            }

            result.onSuccess { test ->
                setState {
                    copy(
                        isLoading = false,
                        test = test,
                        timeLeft = if (test.timeLimit > 0) test.timeLimit else null
                    )
                }
                if (test.timeLimit > 0) {
                    startTimer()
                }
            }
                .onFailure { error ->
                    // Логируем ошибку, чтобы видеть в Logcat
                    android.util.Log.e("TestViewModel", "Error loading test: ${error.message}")
                    setState { copy(isLoading = false) }
                }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                setEvent(TestContract.Event.OnTimerTick)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun submitTest() {
        stopTimer() // Останавливаем таймер
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
                            correctAnswersMap = result.correctAnswers,
                            timeLeft = null // Прячем таймер
                        )
                    }
                }
                .onFailure { setState { copy(isLoading = false) } }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}