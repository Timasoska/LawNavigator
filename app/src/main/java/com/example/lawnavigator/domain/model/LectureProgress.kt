package com.example.lawnavigator.domain.model

/**
 * Чистая модель прогресса чтения.
 * Используется внутри бизнес-логики приложения.
 */
data class LectureProgress(
    val scrollIndex: Int,
    val quote: String? = null // Оставляем на будущее, если будем выделять текст
)