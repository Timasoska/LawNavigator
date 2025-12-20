package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class FlashcardDto(
    val questionId: Int,
    val text: String,
    val options: List<FlashcardOptionDto>
)

@Serializable
data class FlashcardOptionDto(
    val id: Int,
    val text: String,
    val isCorrect: Boolean = false // Бэкенд должен присылать это поле
)

@Serializable
data class ReviewFlashcardRequestDto(
    val questionId: Int,
    val quality: Int
)