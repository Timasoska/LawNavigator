package com.example.lawnavigator.presentation.teacher_groups.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetAnalyticsUseCase
import com.example.lawnavigator.domain.usecase.RemoveStudentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupAnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val removeStudentUseCase: RemoveStudentUseCase, // <--- Внедряем UseCase
    savedStateHandle: SavedStateHandle
) : BaseViewModel<GroupAnalyticsContract.State, GroupAnalyticsContract.Event, GroupAnalyticsContract.Effect>() {

    private val groupId: Int = checkNotNull(savedStateHandle["groupId"])

    override fun createInitialState() = GroupAnalyticsContract.State()

    init {
        loadData()
    }

    override fun handleEvent(event: GroupAnalyticsContract.Event) {
        when (event) {
            is GroupAnalyticsContract.Event.OnBackClicked -> setEffect { GroupAnalyticsContract.Effect.NavigateBack }
            is GroupAnalyticsContract.Event.OnRefresh -> loadData()
            is GroupAnalyticsContract.Event.OnRemoveStudentClicked -> removeStudent(event.studentId) // <--- Обрабатываем нажатие
        }
    }

    private fun loadData() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getAnalyticsUseCase.getAnalytics(groupId)
                .onSuccess { list ->
                    setState { copy(isLoading = false, students = list) }
                }
                .onFailure {
                    setState { copy(isLoading = false, error = it.message) }
                }
        }
    }

    private fun removeStudent(studentId: Int) {
        // Логируем для отладки
        android.util.Log.d("AnalyticsVM", "Removing student $studentId from group $groupId")

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            removeStudentUseCase(groupId, studentId)
                .onSuccess {
                    setEffect { GroupAnalyticsContract.Effect.ShowMessage("Студент исключен из группы") }
                    loadData() // Перезагружаем список, чтобы студент исчез из UI
                }
                .onFailure { error ->
                    setState { copy(isLoading = false) }
                    setEffect { GroupAnalyticsContract.Effect.ShowMessage("Ошибка: ${error.localizedMessage}") }
                }
        }
    }
}