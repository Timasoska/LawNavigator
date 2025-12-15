package com.example.lawnavigator.presentation.lectures_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetLecturesByTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LecturesListViewModel @Inject constructor(
    private val getLecturesByTopicUseCase: GetLecturesByTopicUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<LecturesListContract.State, LecturesListContract.Event, LecturesListContract.Effect>() {

    private val topicId: Int = checkNotNull(savedStateHandle["topicId"])

    override fun createInitialState() = LecturesListContract.State()

    init {
        loadLectures()
    }

    override fun handleEvent(event: LecturesListContract.Event) {
        when (event) {
            is LecturesListContract.Event.OnBackClicked -> setEffect { LecturesListContract.Effect.NavigateBack }
            is LecturesListContract.Event.OnLectureClicked -> setEffect { LecturesListContract.Effect.NavigateToLecture(event.lectureId) }
        }
    }

    private fun loadLectures() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getLecturesByTopicUseCase(topicId)
                .onSuccess { list ->
                    setState { copy(isLoading = false, lectures = list) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                }
        }
    }
}