package com.example.lawnavigator.presentation.teacher_groups

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.CreateGroupUseCase
import com.example.lawnavigator.domain.usecase.GetTeacherGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherGroupsViewModel @Inject constructor(
    private val getTeacherGroupsUseCase: GetTeacherGroupsUseCase,
    private val createGroupUseCase: CreateGroupUseCase
) : BaseViewModel<TeacherGroupsContract.State, TeacherGroupsContract.Event, TeacherGroupsContract.Effect>() {

    override fun createInitialState() = TeacherGroupsContract.State()

    init {
        loadGroups()
    }

    override fun handleEvent(event: TeacherGroupsContract.Event) {
        when (event) {
            is TeacherGroupsContract.Event.OnBackClicked -> setEffect { TeacherGroupsContract.Effect.NavigateBack }
            is TeacherGroupsContract.Event.OnRefresh -> loadGroups()

            is TeacherGroupsContract.Event.OnGroupClicked -> setEffect { TeacherGroupsContract.Effect.NavigateToAnalytics(event.groupId) }

            // Диалог
            is TeacherGroupsContract.Event.OnCreateGroupClicked -> setState { copy(showCreateDialog = true, newGroupName = "") }
            is TeacherGroupsContract.Event.OnDismissDialog -> setState { copy(showCreateDialog = false) }
            is TeacherGroupsContract.Event.OnGroupNameChanged -> setState { copy(newGroupName = event.name) }
            is TeacherGroupsContract.Event.OnConfirmCreateGroup -> createGroup()
        }
    }

    private fun loadGroups() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getTeacherGroupsUseCase.getTeacherGroups()
                .onSuccess { groups -> setState { copy(isLoading = false, groups = groups) } }
                .onFailure { setState { copy(isLoading = false, error = it.message) } }
        }
    }

    private fun createGroup() {
        val name = currentState.newGroupName
        if (name.isBlank()) return

        setState { copy(isLoading = true, showCreateDialog = false) }
        viewModelScope.launch {
            // Для диплома дисциплину можно захардкодить (1) или выбирать.
            // Пока 1.
            createGroupUseCase.createGroup(name, 1)
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
}