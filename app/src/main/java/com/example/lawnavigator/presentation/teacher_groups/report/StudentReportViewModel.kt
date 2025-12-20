package com.example.lawnavigator.presentation.teacher_groups.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetStudentReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentReportViewModel @Inject constructor(
    private val getStudentReportUseCase: GetStudentReportUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<StudentReportContract.State, StudentReportContract.Event, StudentReportContract.Effect>() {

    override fun createInitialState() = StudentReportContract.State()

    init {
        loadReport()
    }

    override fun handleEvent(event: StudentReportContract.Event) {
        when (event) {
            is StudentReportContract.Event.OnBackClicked -> setEffect { StudentReportContract.Effect.NavigateBack }
            is StudentReportContract.Event.OnRefresh -> loadReport()
        }
    }

    private fun loadReport() {
        val groupId = savedStateHandle.get<Int>("groupId") ?: return
        val studentId = savedStateHandle.get<Int>("studentId") ?: return

        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getStudentReportUseCase(groupId, studentId)
                .onSuccess { data ->
                    setState { copy(isLoading = false, report = data) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.localizedMessage) }
                }
        }
    }
}