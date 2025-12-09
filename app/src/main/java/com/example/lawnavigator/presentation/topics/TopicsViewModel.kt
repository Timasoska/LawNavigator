package com.example.lawnavigator.presentation.topics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetTopicsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val getTopicsUseCase: GetTopicsUseCase,
    savedStateHandle: SavedStateHandle // Сюда приходят аргументы навигации
) : BaseViewModel<TopicsContract.State, TopicsContract.Event, TopicsContract.Effect>() {

    // Достаем ID из аргументов навигации
    private val disciplineId: Int = checkNotNull(savedStateHandle["disciplineId"])

    override fun createInitialState() = TopicsContract.State()

    init {
        loadTopics()
    }

    override fun handleEvent(event: TopicsContract.Event) {
        when (event) {
            is TopicsContract.Event.OnBackClicked -> setEffect { TopicsContract.Effect.NavigateBack }
            is TopicsContract.Event.OnTopicClicked -> setEffect { TopicsContract.Effect.NavigateToLecture(event.topicId) }
        }
    }

    private fun loadTopics() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getTopicsUseCase(disciplineId)
                .onSuccess { topics ->
                    setState { copy(isLoading = false, topics = topics) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                }
        }
    }
}