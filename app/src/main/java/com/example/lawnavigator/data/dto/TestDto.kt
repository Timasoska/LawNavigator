package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TestDto(
    val id: Int,
    val title: String,
    val questions: List<QuestionDto>
)

@Serializable
data class QuestionDto(
    val id: Int,
    val text: String,
    val difficulty: Int, // <--- Добавили
    val answers: List<AnswerDto>
)

@Serializable
data class AnswerDto(
    val id: Int,
    val text: String
)

@Serializable
data class SubmitAnswerRequest(
    val questionId: Int,
    val answerId: Int
)

@Serializable
data class TestResultDto(
    val score: Int,
    val correctCount: Int,
    val totalCount: Int
)