package com.example.lawnavigator.presentation.teacher_groups


import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.data.dto.TeacherGroupDto

class TeacherGroupsContract {
    data class State(
        val groups: List<TeacherGroupDto> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,

        // Диалог создания
        val showCreateDialog: Boolean = false,
        val newGroupName: String = "",
        val selectedDisciplineId: Int = 1 // Пока хардкод или загрузим список дисциплин
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnRefresh : Event()

        // Создание группы
        data object OnCreateGroupClicked : Event()
        data object OnDismissDialog : Event()
        data class OnGroupNameChanged(val name: String) : Event()
        data object OnConfirmCreateGroup : Event()

        // Переход к аналитике
        data class OnGroupClicked(val groupId: Int) : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class NavigateToAnalytics(val groupId: Int) : Effect()
        data class ShowMessage(val msg: String) : Effect()
    }
}