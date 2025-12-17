package com.example.lawnavigator.domain.model

import java.util.UUID

/**
 * Модель-черновик для создания/редактирования теста.
 * Хранится в памяти, пока не будет отправлена на сервер.
 */
data class TestDraft(
    val topicId: Int? = null,
    val lectureId: Int? = null, // <--- ДОБАВЬ ЭТО, ЕСЛИ НЕТ
    val id: Int? = null,
    val title: String,
    val timeLimitMinutes: Int,
    val questions: List<QuestionDraft> = emptyList()
)

/**
 * Модель-черновик для вопроса.
 * Используем UUID для уникальности в UI, пока нет серверного ID.
 */
data class QuestionDraft(
    val id: String = UUID.randomUUID().toString(), // Уникальный ID для UI
    val text: String,
    val difficulty: Int, // 1 (Easy), 2 (Medium), 3 (Hard)
    val isMultipleChoice: Boolean,
    val answers: List<AnswerDraft> = emptyList()
)

/**
 * Модель-черновик для варианта ответа.
 * Используем UUID для уникальности в UI.
 */
data class AnswerDraft(
    val id: String = UUID.randomUUID().toString(), // Уникальный ID для UI
    val text: String,
    val isCorrect: Boolean
)