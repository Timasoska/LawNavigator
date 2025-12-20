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

@HiltViewModel
class TeacherGroupsViewModel @Inject constructor(
    private val getTeacherGroupsUseCase: GetTeacherGroupsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase, // <--- Инжект
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

            is TeacherGroupsContract.Event.OnCreateGroupClicked -> setState { copy(showCreateDialog = true, newGroupName = "") }
            is TeacherGroupsContract.Event.OnDismissDialog -> setState { copy(showCreateDialog = false, showDeleteDialog = false, groupIdToDelete = null) }
            is TeacherGroupsContract.Event.OnGroupNameChanged -> setState { copy(newGroupName = event.name) }

            is TeacherGroupsContract.Event.OnDropdownExpanded -> setState { copy(isDropdownExpanded = event.isExpanded) }
            is TeacherGroupsContract.Event.OnDisciplineSelected -> setState { copy(selectedDiscipline = event.discipline, isDropdownExpanded = false) }

            is TeacherGroupsContract.Event.OnConfirmCreateGroup -> createGroup()

            // --- ОБРАБОТКА УДАЛЕНИЯ ---
            is TeacherGroupsContract.Event.OnDeleteGroupClicked -> {
                setState { copy(showDeleteDialog = true, groupIdToDelete = event.groupId) }
            }
            is TeacherGroupsContract.Event.OnConfirmDeleteGroup -> deleteGroup()
        }
    }

    private fun createGroup() {
        val name = currentState.newGroupName
        val discipline = currentState.selectedDiscipline
        if (name.isBlank() || discipline == null) return

        setState { copy(isLoading = true, showCreateDialog = false) }
        viewModelScope.launch {
            createGroupUseCase.createGroup(name, discipline.id)
                .onSuccess { code ->
                    setEffect { TeacherGroupsContract.Effect.ShowMessage("Группа создана! Код: $code") }
                    loadGroups()
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    setEffect { TeacherGroupsContract.Effect.ShowMessage("Ошибка: ${it.message}") }
                }
        }
    }

    private fun deleteGroup() {
        val groupId = currentState.groupIdToDelete ?: return
        setState { copy(isLoading = true, showDeleteDialog = false) }

        viewModelScope.launch {
            deleteGroupUseCase(groupId)
                .onSuccess {
                    setEffect { TeacherGroupsContract.Effect.ShowMessage("Группа успешно удалена") }
                    loadGroups()
                }
                .onFailure { error ->
                    setState { copy(isLoading = false) }
                    setEffect { TeacherGroupsContract.Effect.ShowMessage("Ошибка при удалении: ${error.localizedMessage}") }
                }
        }
    }

    private fun loadGroups() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getTeacherGroupsUseCase.getTeacherGroups()
                .onSuccess { groups -> setState { copy(isLoading = false, groups = groups) } }
                .onFailure { setState { copy(isLoading = false, error = it.message) } }
        }
    }
}