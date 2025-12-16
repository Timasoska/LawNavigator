package com.example.lawnavigator.presentation.topics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.usecase.GetTopicsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val tokenManager: TokenManager, // <--- ИНЖЕКТ
    savedStateHandle: SavedStateHandle // Сюда приходят аргументы навигации
) : BaseViewModel<TopicsContract.State, TopicsContract.Event, TopicsContract.Effect>() {

    // Достаем ID из аргументов навигации
    private val disciplineId: Int = checkNotNull(savedStateHandle["disciplineId"])

    override fun createInitialState() = TopicsContract.State()

    init {
        checkRole() // <--- Проверяем роль
        loadTopics()
    }

    private fun checkRole() {
        viewModelScope.launch {
            val role = tokenManager.role.first()
            setState { copy(isTeacher = role == "teacher") }
        }
    }

    override fun handleEvent(event: TopicsContract.Event) {
        when (event) {
            is TopicsContract.Event.OnBackClicked -> setEffect { TopicsContract.Effect.NavigateBack }
            is TopicsContract.Event.OnRetryClicked -> loadTopics() // <--- Добавь обработку
            // Клик по теме -> Список лекций (мы это меняли в прошлый раз)
            is TopicsContract.Event.OnTopicClicked -> setEffect { TopicsContract.Effect.NavigateToLecture(event.topicId) }
            // Клик по "Создать тест"
            is TopicsContract.Event.OnCreateTestClicked -> setEffect { TopicsContract.Effect.NavigateToTestCreator(event.topicId) } // Передаем ID
        }
    }

    private fun loadTopics() {
        // ВАЖНО: Сбрасываем ошибку перед началом загрузки!
        setState { copy(isLoading = true, error = null) }

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