package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO для отправки полного теста на сервер для создания/обновления.
 */
@Serializable
data class SaveTestRequestDto(
    val topicId: Int,
    val title: String,
    val timeLimit: Int, // В секундах
    val questions: List<SaveQuestionRequestDto>
)

@Serializable
data class SaveQuestionRequestDto(
    val text: String,
    val difficulty: Int,
    val isMultipleChoice: Boolean,
    val answers: List<SaveAnswerRequestDto>
)

@Serializable
data class SaveAnswerRequestDto(
    val text: String,
    val isCorrect: Boolean
)