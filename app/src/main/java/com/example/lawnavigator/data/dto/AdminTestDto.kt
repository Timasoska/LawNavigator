package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO для отправки полного теста на сервер для создания/обновления.
 */
@Serializable
data class SaveTestRequestDto(
    val topicId: Int? = null,   // <--- СТАЛ Int?
    val lectureId: Int? = null, // <--- НОВОЕ ПОЛЕ Int?
    val title: String,
    val timeLimit: Int,
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

/**
 * DTO для получения теста для редактирования (Ответ сервера)
 */
@Serializable
data class AdminTestResponseDto(
    val id: Int,
    val topicId: Int?,   // <--- СТАЛ Int?
    val lectureId: Int?, // <--- НОВОЕ ПОЛЕ Int?
    val title: String,
    val timeLimit: Int,
    val questions: List<SaveQuestionRequestDto>
)