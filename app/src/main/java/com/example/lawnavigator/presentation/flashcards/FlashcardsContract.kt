package com.example.lawnavigator.presentation.flashcards

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.Flashcard

class FlashcardsContract {
    data class State(
        val cards: List<Flashcard> = emptyList(),
        val currentIndex: Int = 0,
        val isFlipped: Boolean = false,
        val isLoading: Boolean = false,
        val isFinished: Boolean = false,
        val error: String? = null
    ) : ViewState {
        val currentCard: Flashcard? get() = cards.getOrNull(currentIndex)
        val progress: Float get() = if (cards.isNotEmpty()) (currentIndex.toFloat() / cards.size) else 0f
    }

    sealed class Event : ViewIntent {
        data object OnBackClicked : Event()
        data object OnCardFlip : Event()
        // Quality: 0 (Забыл), 3 (Норм), 5 (Легко)
        data class OnRate(val quality: Int) : Event()
        data object OnRetry : Event()
    }

    sealed class Effect : ViewSideEffect {
        data object NavigateBack : Effect()
        data class ShowMessage(val msg: String) : Effect()
    }
}