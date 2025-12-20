package com.example.lawnavigator.presentation.flashcards

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.AddXpUseCase
import com.example.lawnavigator.domain.usecase.GetFlashcardsUseCase
import com.example.lawnavigator.domain.usecase.ReviewFlashcardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashcardsViewModel @Inject constructor(
    private val getFlashcardsUseCase: GetFlashcardsUseCase,
    private val reviewFlashcardUseCase: ReviewFlashcardUseCase,
    private val addXpUseCase: AddXpUseCase // <--- ИНЖЕКТ
) : BaseViewModel<FlashcardsContract.State, FlashcardsContract.Event, FlashcardsContract.Effect>() {

    override fun createInitialState() = FlashcardsContract.State()

    init {
        loadCards()
    }

    override fun handleEvent(event: FlashcardsContract.Event) {
        when (event) {
            is FlashcardsContract.Event.OnBackClicked -> setEffect { FlashcardsContract.Effect.NavigateBack }
            is FlashcardsContract.Event.OnRetry -> loadCards()
            is FlashcardsContract.Event.OnCardFlip -> setState { copy(isFlipped = !isFlipped) }
            is FlashcardsContract.Event.OnRate -> processRate(event.quality)
        }
    }

    private fun loadCards() {
        setState { copy(isLoading = true, error = null, isFinished = false, currentIndex = 0) }
        viewModelScope.launch {
            getFlashcardsUseCase()
                .onSuccess { list ->
                    if (list.isEmpty()) {
                        setState { copy(isLoading = false, isFinished = true) }
                    } else {
                        setState { copy(isLoading = false, cards = list) }
                    }
                }
                .onFailure {
                    setState { copy(isLoading = false, error = it.localizedMessage) }
                }
        }
    }

    private fun processRate(quality: Int) {
        val card = currentState.currentCard ?: return

        viewModelScope.launch {
            reviewFlashcardUseCase(card.id, quality)
        }

        val nextIndex = currentState.currentIndex + 1
        if (nextIndex >= currentState.cards.size) {
            // --- СЕССИЯ ЗАВЕРШЕНА ---
            // Начисляем бонус за прохождение повторений
            viewModelScope.launch {
                addXpUseCase(50, "flashcards_session")
            }

            setState { copy(isFinished = true, isFlipped = false) }
        } else {
            setState { copy(currentIndex = nextIndex, isFlipped = false) }
        }
    }
}