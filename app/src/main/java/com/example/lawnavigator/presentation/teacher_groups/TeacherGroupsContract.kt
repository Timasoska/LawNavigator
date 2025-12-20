package com.example.lawnavigator.presentation.teacher_groups


import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.data.dto.TeacherGroupDto
import com.example.lawnavigator.domain.model.Discipline

class TeacherGroupsContract {
    data class State(
        val groups: List<TeacherGroupDto> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,

        // --- ДЛЯ ДИАЛОГА СОЗДАНИЯ ---
        val showCreateDialog: Boolean = false,
        val newGroupName: String = "",

        // Выбор дисциплины
        val availableDisciplines: List<Discipline> = emptyList(),
        val selectedDiscipline: Discipline? = null,
        val isDropdownExpanded: Boolean = false,

        // --- ДЛЯ ДИАЛОГА УДАЛЕНИЯ (НОВОЕ) ---
        val showDeleteDialog: Boolean = false,
        val groupIdToDelete: Int? = null
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnRefresh : Event()
        data class OnGroupClicked(val groupId: Int) : Event()

        // Диалог создания
        data object OnCreateGroupClicked : Event()
        data object OnDismissDialog : Event()
        data class OnGroupNameChanged(val name: String) : Event()
        data object OnConfirmCreateGroup : Event()

        // Выпадающий список
        data class OnDisciplineSelected(val discipline: Discipline) : Event()
        data class OnDropdownExpanded(val isExpanded: Boolean) : Event()

        // --- УДАЛЕНИЕ (НОВОЕ) ---
        data class OnDeleteGroupClicked(val groupId: Int) : Event()
        data object OnConfirmDeleteGroup : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class NavigateToAnalytics(val groupId: Int) : Effect()
        data class ShowMessage(val msg: String) : Effect()
    }
}