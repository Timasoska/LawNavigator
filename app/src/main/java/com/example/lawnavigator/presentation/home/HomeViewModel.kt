package com.example.lawnavigator.presentation.home

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetDisciplinesUseCase
import com.example.lawnavigator.domain.usecase.GetEngagementStatusUseCase // <--- Импорт
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lawnavigator.data.local.TokenManager
import kotlinx.coroutines.flow.first

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDisciplinesUseCase: GetDisciplinesUseCase,
    private val getEngagementStatusUseCase: GetEngagementStatusUseCase,
    private val tokenManager: TokenManager
) : BaseViewModel<HomeContract.State, HomeContract.Event, HomeContract.Effect>() {

    override fun createInitialState() = HomeContract.State()

    init {
        checkRole()
        loadData()
        observeUserName() // <--- ЗАПУСКАЕМ НАБЛЮДЕНИЕ ЗА ИМЕНЕМ
    }

    private fun observeUserName() {
        viewModelScope.launch {
            tokenManager.userName.collect { name ->
                setState { copy(userName = name) }
            }
        }
    }

    private fun checkRole() {
        viewModelScope.launch {
            val role = tokenManager.role.first()
            setState { copy(isTeacher = role == "teacher") }
        }
    }

    override fun handleEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.OnRetryClicked -> loadData()
            is HomeContract.Event.OnRefresh -> loadData()

            is HomeContract.Event.OnDisciplineClicked -> {
                setEffect { HomeContract.Effect.NavigateToTopics(event.disciplineId) }
            }
            is HomeContract.Event.OnTeacherGroupsClicked -> {
                setEffect { HomeContract.Effect.NavigateToTeacherGroups }
            }
            // ФИЛЬТРАЦИЯ
            is HomeContract.Event.OnFilterClick -> {
                setState { copy(isFilterExpanded = event.isExpanded) }
            }
            is HomeContract.Event.OnSortSelected -> {
                setState { copy(currentSort = event.sortType, isFilterExpanded = false) }
                applySort()
            }
        }
    }

    private fun loadData() {
        setState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            getDisciplinesUseCase()
                .onSuccess { list ->
                    // Сохраняем и оригинальный, и текущий список
                    setState { copy(originalDisciplines = list, disciplines = list) }
                    applySort() // Сразу применяем текущую сортировку
                }
                .onFailure { error ->
                    setState { copy(error = error.message ?: "Ошибка загрузки") }
                }

            getEngagementStatusUseCase().onSuccess { status ->
                setState { copy(engagementStatus = status) }
            }

            setState { copy(isLoading = false) }
        }
    }

    private fun applySort() {
        val sortedList = when (currentState.currentSort) {
            SortType.NONE -> currentState.originalDisciplines
            SortType.A_Z -> currentState.originalDisciplines.sortedBy { it.name }
            SortType.Z_A -> currentState.originalDisciplines.sortedByDescending { it.name }
        }
        setState { copy(disciplines = sortedList) }
    }
}