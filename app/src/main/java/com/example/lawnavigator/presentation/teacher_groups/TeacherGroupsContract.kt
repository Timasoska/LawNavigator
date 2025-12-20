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

        // Универсальные поля для диалога (Создание и Редактирование)
        val showGroupDialog: Boolean = false,
        val isEditing: Boolean = false,
        val editingGroupId: Int? = null,
        val groupNameInput: String = "",

        // Поля выбора дисциплины
        val availableDisciplines: List<Discipline> = emptyList(),
        val selectedDiscipline: Discipline? = null,
        val isDropdownExpanded: Boolean = false,

        // Поля удаления
        val showDeleteDialog: Boolean = false,
        val groupIdToDelete: Int? = null
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnRefresh : Event()
        data class OnGroupClicked(val groupId: Int) : Event()

        // События диалога
        data object OnCreateGroupClicked : Event()
        data class OnEditGroupClicked(val group: TeacherGroupDto) : Event()
        data object OnDismissDialog : Event()
        data class OnGroupNameChanged(val name: String) : Event()
        data object OnConfirmSaveGroup : Event() // Универсальное событие сохранения

        // Выпадающий список
        data class OnDisciplineSelected(val discipline: Discipline) : Event()
        data class OnDropdownExpanded(val isExpanded: Boolean) : Event()

        // Удаление
        data class OnDeleteGroupClicked(val groupId: Int) : Event()
        data object OnConfirmDeleteGroup : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class NavigateToAnalytics(val groupId: Int) : Effect()
        data class ShowMessage(val msg: String) : Effect()
    }
}