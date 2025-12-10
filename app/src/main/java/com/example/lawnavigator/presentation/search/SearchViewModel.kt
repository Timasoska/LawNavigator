package com.example.lawnavigator.presentation.search

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase
) : BaseViewModel<SearchContract.State, SearchContract.Event, SearchContract.Effect>() {

    private var searchJob: Job? = null

    override fun createInitialState() = SearchContract.State()

    override fun handleEvent(event: SearchContract.Event) {
        when (event) {
            is SearchContract.Event.OnQueryChanged -> {
                setState { copy(query = event.query) }
                performSearch(event.query)
            }
            is SearchContract.Event.OnLectureClicked -> {
                setEffect { SearchContract.Effect.NavigateToLecture(event.lectureId) }
            }
            is SearchContract.Event.OnBackClicked -> {
                setEffect { SearchContract.Effect.NavigateBack }
            }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Ждем полсекунды после ввода
            if (query.length > 2) {
                setState { copy(isLoading = true) }
                searchUseCase(query)
                    .onSuccess { list ->
                        setState { copy(isLoading = false, results = list) }
                    }
                    .onFailure {
                        setState { copy(isLoading = false) }
                    }
            } else {
                setState { copy(results = emptyList()) }
            }
        }
    }
}