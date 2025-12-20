package com.example.lawnavigator.presentation.teacher_groups

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.CreateGroupUseCase
import com.example.lawnavigator.domain.usecase.DeleteGroupUseCase
import com.example.lawnavigator.domain.usecase.GetTeacherGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lawnavigator.domain.usecase.GetDisciplinesUseCase // <--- Добавить
import com.example.lawnavigator.domain.usecase.UpdateGroupUseCase

@HiltViewModel
class TeacherGroupsViewModel @Inject constructor(
    private val getTeacherGroupsUseCase: GetTeacherGroupsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val getDisciplinesUseCase: GetDisciplinesUseCase
) : BaseViewModel<TeacherGroupsContract.State, TeacherGroupsContract.Event, TeacherGroupsContract.Effect>() {

    override fun createInitialState() = TeacherGroupsContract.State()

    init {
        loadGroups()
        loadDisciplines()
    }

    private fun loadDisciplines() {
        viewModelScope.launch {
            getDisciplinesUseCase()
                .onSuccess { list ->
                    setState { copy(availableDisciplines = list, selectedDiscipline = list.firstOrNull()) }
                }
        }
    }

    override fun handleEvent(event: TeacherGroupsContract.Event) {
        when (event) {
            is TeacherGroupsContract.Event.OnBackClicked -> setEffect { TeacherGroupsContract.Effect.NavigateBack }
            is TeacherGroupsContract.Event.OnRefresh -> loadGroups()
            is TeacherGroupsContract.Event.OnGroupClicked -> setEffect { TeacherGroupsContract.Effect.NavigateToAnalytics(event.groupId) }

            // Логика открытия диалогов (Исправлены имена параметров)
            is TeacherGroupsContract.Event.OnCreateGroupClicked -> {
                setState { copy(showGroupDialog = true, isEditing = false, groupNameInput = "", editingGroupId = null) }
            }
            is TeacherGroupsContract.Event.OnEditGroupClicked -> {
                setState { copy(showGroupDialog = true, isEditing = true, groupNameInput = event.group.name, editingGroupId = event.group.id) }
            }
            is TeacherGroupsContract.Event.OnDismissDialog -> {
                setState { copy(showGroupDialog = false, showDeleteDialog = false, groupNameInput = "", editingGroupId = null) }
            }
            is TeacherGroupsContract.Event.OnGroupNameChanged -> {
                setState { copy(groupNameInput = event.name) }
            }
            is TeacherGroupsContract.Event.OnConfirmSaveGroup -> saveGroup()

            // Выпадающий список
            is TeacherGroupsContract.Event.OnDropdownExpanded -> setState { copy(isDropdownExpanded = event.isExpanded) }
            is TeacherGroupsContract.Event.OnDisciplineSelected -> setState { copy(selectedDiscipline = event.discipline, isDropdownExpanded = false) }

            // Удаление
            is TeacherGroupsContract.Event.OnDeleteGroupClicked -> {
                setState { copy(showDeleteDialog = true, groupIdToDelete = event.groupId) }
            }
            is TeacherGroupsContract.Event.OnConfirmDeleteGroup -> deleteGroup()
        }
    }

    private fun saveGroup() {
        val name = currentState.groupNameInput
        val disciplineId = currentState.selectedDiscipline?.id

        if (name.isBlank()) return

        setState { copy(isLoading = true, showGroupDialog = false) }

        viewModelScope.launch {
            val result = if (currentState.isEditing) {
                // Редактирование существующей
                updateGroupUseCase(currentState.editingGroupId!!, name)
            } else {
                // Создание новой (disciplineId обязателен)
                if (disciplineId == null) {
                    setState { copy(isLoading = false) }
                    return@launch
                }
                createGroupUseCase.createGroup(name, disciplineId).map { Unit }
            }

            result.onSuccess {
                val msg = if (currentState.isEditing) "Название группы обновлено" else "Группа успешно создана"
                setEffect { TeacherGroupsContract.Effect.ShowMessage(msg) }
                loadGroups()
            }.onFailure {
                setState { copy(isLoading = false) }
                setEffect { TeacherGroupsContract.Effect.ShowMessage("Ошибка: ${it.message}") }
            }
        }
    }

    private fun deleteGroup() {
        val id = currentState.groupIdToDelete ?: return
        setState { copy(isLoading = true, showDeleteDialog = false) }

        viewModelScope.launch {
            deleteGroupUseCase(id)
                .onSuccess {
                    setEffect { TeacherGroupsContract.Effect.ShowMessage("Группа удалена") }
                    loadGroups()
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    setEffect { TeacherGroupsContract.Effect.ShowMessage("Не удалось удалить группу") }
                }
        }
    }

    private fun loadGroups() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getTeacherGroupsUseCase.getTeacherGroups()
                .onSuccess { groups ->
                    setState { copy(isLoading = false, groups = groups) }
                }
                .onFailure {
                    setState { copy(isLoading = false, error = it.message) }
                }
        }
    }
}