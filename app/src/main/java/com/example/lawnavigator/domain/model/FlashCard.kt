package com.example.lawnavigator.domain.model

data class Flashcard(
    val id: Int,
    val question: String,
    val options: List<FlashcardOption>
)

data class FlashcardOption(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)