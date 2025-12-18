package com.example.lawnavigator.presentation.topics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.usecase.CreateTopicUseCase
import com.example.lawnavigator.domain.usecase.DeleteTopicUseCase
import com.example.lawnavigator.domain.usecase.GetTopicsUseCase
import com.example.lawnavigator.domain.usecase.UpdateTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val tokenManager: TokenManager, // <--- ИНЖЕКТ
    private val createTopicUseCase: CreateTopicUseCase,
    private val updateTopicUseCase: UpdateTopicUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase,
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
            // Диалоги
            is TopicsContract.Event.OnDismissDialogs -> { setState { copy(showTopicDialog = false, showDeleteDialog = false, editingTopicId = null, topicToDeleteId = null, topicNameInput = "") } }
            is TopicsContract.Event.OnTopicNameChanged -> { setState { copy(topicNameInput = event.name) } }
            // Создание
            is TopicsContract.Event.OnAddTopicClicked -> { setState { copy(showTopicDialog = true, editingTopicId = null, topicNameInput = "") } }
            // Редактирование
            is TopicsContract.Event.OnEditTopicClicked -> { setState { copy(showTopicDialog = true, editingTopicId = event.topic.id, topicNameInput = event.topic.name) } }
            // Удаление
            is TopicsContract.Event.OnDeleteTopicClicked -> { setState { copy(showDeleteDialog = true, topicToDeleteId = event.topicId) } }
            // Логика сохранения
            is TopicsContract.Event.OnSaveTopic -> saveTopic()
            is TopicsContract.Event.OnConfirmDeleteTopic -> deleteTopic()
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
    private fun saveTopic() {
        val name = currentState.topicNameInput
        val editId = currentState.editingTopicId

        if (name.isBlank()) return

        setState { copy(isLoading = true, showTopicDialog = false) } // Закрываем диалог и крутим лоадер

        viewModelScope.launch {
            val result = if (editId == null) {
                // Создание
                createTopicUseCase.createTopic(disciplineId, name)
            } else {
                // Обновление
                updateTopicUseCase.updateTopic(editId, name)
            }

            result.onSuccess {
                loadTopics() // Перезагружаем список
            }.onFailure {
                setState { copy(isLoading = false, error = it.message) }
            }
        }
    }
    private fun deleteTopic() {
        val id = currentState.topicToDeleteId ?: return
        setState { copy(isLoading = true, showDeleteDialog = false) }

        viewModelScope.launch {
            deleteTopicUseCase.deleteTopic(id)
                .onSuccess { loadTopics() }
                .onFailure {
                    setState { copy(isLoading = false, error = it.message) }
                }
        }
    }
}