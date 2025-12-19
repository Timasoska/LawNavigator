package com.example.lawnavigator.presentation.home

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetDisciplinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lawnavigator.data.local.TokenManager
import kotlinx.coroutines.flow.first

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDisciplinesUseCase: GetDisciplinesUseCase,
    private val tokenManager: TokenManager // <--- ИНЖЕКТ
) : BaseViewModel<HomeContract.State, HomeContract.Event, HomeContract.Effect>() {

    override fun createInitialState() = HomeContract.State()

    init {
        checkRole() // <--- Проверка
        loadDisciplines()
    }

    private fun checkRole() {
        viewModelScope.launch {
            val role = tokenManager.role.first()
            setState { copy(isTeacher = role == "teacher") }
        }
    }

    override fun handleEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.OnRetryClicked -> loadDisciplines()
            is HomeContract.Event.OnDisciplineClicked -> {
                setEffect { HomeContract.Effect.NavigateToTopics(event.disciplineId) }
            }
            // Обработка клика
            is HomeContract.Event.OnTeacherGroupsClicked -> {
                setEffect { HomeContract.Effect.NavigateToTeacherGroups }
            }
        }
    }

    private fun loadDisciplines() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = getDisciplinesUseCase()

            result.fold(
                onSuccess = { list ->
                    setState { copy(isLoading = false, disciplines = list) }
                },
                onFailure = { error ->
                    setState { copy(isLoading = false, error = error.message ?: "Ошибка загрузки") }
                }
            )
        }
    }
}