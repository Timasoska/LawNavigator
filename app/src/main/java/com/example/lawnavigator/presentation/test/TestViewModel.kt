package com.example.lawnavigator.presentation.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.TestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val testUseCase: TestUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TestContract.State, TestContract.Event, TestContract.Effect>() {

    private val topicId: Int = checkNotNull(savedStateHandle["topicId"])

    override fun createInitialState() = TestContract.State()

    init {
        loadTest()
    }

    override fun handleEvent(event: TestContract.Event) {
        when (event) {
            is TestContract.Event.OnAnswerSelected -> {
                val newMap = currentState.selectedAnswers.toMutableMap()
                newMap[event.questionId] = event.answerId
                setState { copy(selectedAnswers = newMap) }
            }
            is TestContract.Event.OnSubmitClicked -> submitTest()
            is TestContract.Event.OnBackClicked -> setEffect { TestContract.Effect.NavigateBack }
        }
    }

    private fun loadTest() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            testUseCase.loadTest(topicId)
                .onSuccess { test -> setState { copy(isLoading = false, test = test) } }
                .onFailure { setState { copy(isLoading = false) } } // Обработать ошибку
        }
    }

    private fun submitTest() {
        val testId = currentState.test?.id ?: return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            testUseCase.submit(testId, currentState.selectedAnswers)
                .onSuccess { result ->
                    setState { copy(isLoading = false, resultScore = result.score, resultMessage = result.message) }
                }
                .onFailure { setState { copy(isLoading = false) } }
        }
    }
}