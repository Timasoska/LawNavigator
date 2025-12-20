package com.example.lawnavigator.presentation.profile.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetDisciplineDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для детальной аналитики дисциплины.
 * Исправлена проблема инициализации SavedStateHandle через перенос логики в блок init.
 */
@HiltViewModel
class DisciplineDetailsViewModel @Inject constructor(
    private val getDisciplineDetailsUseCase: GetDisciplineDetailsUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<DisciplineDetailsContract.State, DisciplineDetailsContract.Event, DisciplineDetailsContract.Effect>() {

    // Состояние инициализируется пустым
    override fun createInitialState() = DisciplineDetailsContract.State()

    init {
        // Извлекаем данные только когда конструктор дочернего класса полностью отработал
        val disciplineId = savedStateHandle.get<Int>("disciplineId") ?: -1
        val disciplineName = savedStateHandle.get<String>("disciplineName") ?: "Детали"

        android.util.Log.d("DisciplineVM", "Init with ID: $disciplineId, Name: $disciplineName")

        // Обновляем состояние данными из навигации
        setState { copy(disciplineName = disciplineName) }

        if (disciplineId != -1) {
            loadDetails(disciplineId)
        }
    }

    override fun handleEvent(event: DisciplineDetailsContract.Event) {
        when (event) {
            is DisciplineDetailsContract.Event.OnBackClicked -> setEffect { DisciplineDetailsContract.Effect.NavigateBack }
            is DisciplineDetailsContract.Event.OnRefresh -> {
                val id = savedStateHandle.get<Int>("disciplineId") ?: -1
                if (id != -1) loadDetails(id)
            }
        }
    }

    private fun loadDetails(id: Int) {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getDisciplineDetailsUseCase(id)
                .onSuccess { stats ->
                    setState { copy(isLoading = false, topics = stats) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.localizedMessage) }
                }
        }
    }
}