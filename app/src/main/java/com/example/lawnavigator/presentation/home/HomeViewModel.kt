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
            // И Refresh, и Retry вызывают загрузку данных
            is HomeContract.Event.OnRetryClicked -> loadData()
            is HomeContract.Event.OnRefresh -> loadData() // <--- ОБРАБОТКА

            is HomeContract.Event.OnDisciplineClicked -> {
                setEffect { HomeContract.Effect.NavigateToTopics(event.disciplineId) }
            }
            is HomeContract.Event.OnTeacherGroupsClicked -> {
                setEffect { HomeContract.Effect.NavigateToTeacherGroups }
            }
        }
    }

    private fun loadData() {
        // isLoading = true запускает спиннер в UI
        setState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // 1. Грузим список предметов
            getDisciplinesUseCase()
                .onSuccess { list ->
                    setState { copy(disciplines = list) }
                }
                .onFailure { error ->
                    setState { copy(error = error.message ?: "Ошибка загрузки") }
                }

            // 2. Грузим XP и Стрики (параллельно обновляем стейт)
            getEngagementStatusUseCase()
                .onSuccess { status ->
                    setState { copy(engagementStatus = status) }
                }

            // Выключаем спиннер
            setState { copy(isLoading = false) }
        }
    }
}