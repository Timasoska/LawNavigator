package com.example.lawnavigator.presentation.leaderboard

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetLeaderboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : BaseViewModel<LeaderboardContract.State, LeaderboardContract.Event, LeaderboardContract.Effect>() {

    override fun createInitialState() = LeaderboardContract.State()

    init {
        loadData()
    }

    override fun handleEvent(event: LeaderboardContract.Event) {
        when (event) {
            is LeaderboardContract.Event.OnBackClicked -> setEffect { LeaderboardContract.Effect.NavigateBack }
            is LeaderboardContract.Event.OnRetryClicked -> loadData()
        }
    }

    private fun loadData() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getLeaderboardUseCase()
                .onSuccess { users ->
                    setState { copy(isLoading = false, users = users) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                }
        }
    }
}