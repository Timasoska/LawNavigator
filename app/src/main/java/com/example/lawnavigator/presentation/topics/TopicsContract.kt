package com.example.lawnavigator.presentation.topics

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.Topic

class TopicsContract {

    data class State(
        val topics: List<Topic> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isTeacher: Boolean = false, // <--- НОВОЕ ПОЛЕ
        // --- НОВЫЕ ПОЛЯ ДЛЯ ДИАЛОГОВ ---
        val showTopicDialog: Boolean = false, // Диалог создания/редактирования
        val editingTopicId: Int? = null,      // Если null - создаем новую, иначе редактируем эту
        val topicNameInput: String = "",      // Ввод названия
        val showDeleteDialog: Boolean = false, // Диалог удаления
        val topicToDeleteId: Int? = null
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnRetryClicked : Event()
        data class OnTopicClicked(val topicId: Int) : Event()

        // <--- НОВОЕ СОБЫТИЕ: Нажали "Создать тест"
        data class OnCreateTestClicked(val topicId: Int) : Event()
        // --- НОВЫЕ СОБЫТИЯ ---
        data object OnAddTopicClicked : Event()                    // Нажали (+)
        data class OnEditTopicClicked(val topic: Topic) : Event()  // Нажали (карандаш)
        data class OnDeleteTopicClicked(val topicId: Int) : Event()// Нажали (корзина)

        data class OnTopicNameChanged(val name: String) : Event()  // Ввод текста
        data object OnSaveTopic : Event()                          // Нажали "Сохранить" в диалоге
        data object OnConfirmDeleteTopic : Event()                 // Нажали "Да" при удалении
        data object OnDismissDialogs : Event()                     // Закрыть любой диалог
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class NavigateToLecture(val topicId: Int) : Effect()

        // <--- НОВЫЙ ЭФФЕКТ: Переход в конструктор
        data class NavigateToTestCreator(val topicId: Int) : Effect()
    }
}