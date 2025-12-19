package com.example.lawnavigator.presentation.teacher_groups

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.CreateGroupUseCase
import com.example.lawnavigator.domain.usecase.GetTeacherGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lawnavigator.domain.usecase.GetDisciplinesUseCase // <--- Добавить

@HiltViewModel
class TeacherGroupsViewModel @Inject constructor(
    private val getTeacherGroupsUseCase: GetTeacherGroupsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val getDisciplinesUseCase: GetDisciplinesUseCase // <--- Инжект
) : BaseViewModel<TeacherGroupsContract.State, TeacherGroupsContract.Event, TeacherGroupsContract.Effect>() {

    override fun createInitialState() = TeacherGroupsContract.State()

    init {
        loadGroups()
        loadDisciplines() // <--- Грузим список предметов
    }

    private fun loadDisciplines() {
        viewModelScope.launch {
            getDisciplinesUseCase()
                .onSuccess { list ->
                    // Сразу выбираем первую дисциплину по умолчанию, чтобы не было null
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
            is TeacherGroupsContract.Event.OnDismissDialog -> setState { copy(showCreateDialog = false) }
            is TeacherGroupsContract.Event.OnGroupNameChanged -> setState { copy(newGroupName = event.name) }

            // Логика Dropdown
            is TeacherGroupsContract.Event.OnDropdownExpanded -> setState { copy(isDropdownExpanded = event.isExpanded) }
            is TeacherGroupsContract.Event.OnDisciplineSelected -> setState { copy(selectedDiscipline = event.discipline, isDropdownExpanded = false) }

            is TeacherGroupsContract.Event.OnConfirmCreateGroup -> createGroup()
        }
    }

    private fun createGroup() {
        val name = currentState.newGroupName
        val discipline = currentState.selectedDiscipline

        if (name.isBlank() || discipline == null) return

        setState { copy(isLoading = true, showCreateDialog = false) }
        viewModelScope.launch {
            // Используем ID выбранной дисциплины
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

    // loadGroups без изменений...
    private fun loadGroups() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getTeacherGroupsUseCase.getTeacherGroups()
                .onSuccess { groups -> setState { copy(isLoading = false, groups = groups) } }
                .onFailure { setState { copy(isLoading = false, error = it.message) } }
        }
    }
}