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
        val isTeacher: Boolean = false // <--- НОВОЕ ПОЛЕ
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnRetryClicked : Event()
        data class OnTopicClicked(val topicId: Int) : Event()

        // <--- НОВОЕ СОБЫТИЕ: Нажали "Создать тест"
        data class OnCreateTestClicked(val topicId: Int) : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class NavigateToLecture(val topicId: Int) : Effect()

        // <--- НОВЫЙ ЭФФЕКТ: Переход в конструктор
        data class NavigateToTestCreator(val topicId: Int) : Effect()
    }
}