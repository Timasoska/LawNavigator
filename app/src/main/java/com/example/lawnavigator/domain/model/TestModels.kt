package com.example.lawnavigator.domain.model

data class TestContent(
    val id: Int,
    val title: String,
    val questions: List<Question>
)

data class Question(
    val id: Int,
    val text: String,
    val difficulty: Int, // <--- Добавили
    val isMultipleChoice: Boolean, // <--- Добавили
    val answers: List<Answer>
)

data class Answer(
    val id: Int,
    val text: String
)

data class TestResult(
    val score: Int,
    val message: String,
    val correctAnswers: Map<Int, List<Int>> // <--- ДОБАВЬ ЭТО ПОЛЕ
)