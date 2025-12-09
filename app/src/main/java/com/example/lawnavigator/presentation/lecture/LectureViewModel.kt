package com.example.lawnavigator.presentation.lecture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.LectureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureViewModel @Inject constructor(
    private val lectureUseCase: LectureUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<LectureContract.State, LectureContract.Event, LectureContract.Effect>() {

    // В AppNavigation мы назовем аргумент "lectureId" (не забудь!)
    private val lectureId: Int = checkNotNull(savedStateHandle["lectureId"])

    override fun createInitialState() = LectureContract.State()

    init {
        loadLecture()
    }

    override fun handleEvent(event: LectureContract.Event) {
        when (event) {
            is LectureContract.Event.OnBackClicked -> setEffect { LectureContract.Effect.NavigateBack }
            is LectureContract.Event.OnFavoriteClicked -> toggleFavorite()
        }
    }

    private fun loadLecture() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            lectureUseCase.getLecture(lectureId)
                .onSuccess { lecture ->
                    setState { copy(isLoading = false, lecture = lecture) }
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    setEffect { LectureContract.Effect.ShowMessage("Ошибка загрузки") }
                }
        }
    }

    private fun toggleFavorite() {
        val newStatus = !currentState.isFavorite
        setState { copy(isFavorite = newStatus) } // Сразу меняем UI для отзывчивости

        viewModelScope.launch {
            lectureUseCase.toggleFavorite(lectureId, newStatus)
                .onSuccess {
                    val msg = if (newStatus) "Добавлено в избранное" else "Удалено из избранного"
                    setEffect { LectureContract.Effect.ShowMessage(msg) }
                }
                .onFailure {
                    // Если ошибка - откатываем состояние
                    setState { copy(isFavorite = !newStatus) }
                    setEffect { LectureContract.Effect.ShowMessage("Ошибка сети") }
                }
        }
    }
}