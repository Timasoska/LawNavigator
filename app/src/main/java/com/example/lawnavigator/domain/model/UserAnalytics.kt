package com.example.lawnavigator.domain.model

/**
 * Чистая модель аналитики для отображения в UI.
 */
data class UserAnalytics(
    val testsPassed: Int,
    val averageScore: Double,
    val trend: Double,
    val recommendations: List<Topic>, // Переиспользуем модель Topic
    val disciplines: List<DisciplineStat> = emptyList(), // <--- Добавили
)

data class DisciplineStat(
    val name: String,
    val score: Double,
    val trend: Double
)