package com.example.lawnavigator.presentation.teacher_groups.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupAnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
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
        }
    }

    private fun loadData() {
        setState { copy(isLoading = true) }
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
}