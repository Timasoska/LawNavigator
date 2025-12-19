package com.example.lawnavigator.domain.model

/**
 * Чистая модель аналитики для отображения в UI.
 */
data class UserAnalytics(
    val testsPassed: Int,
    val averageScore: Double,
    val trend: Double,
    val history: List<Int>, // <--- ДОБАВЬ ЭТУ СТРОКУ
    val recommendations: List<Topic>, // Переиспользуем модель Topic
    val disciplines: List<DisciplineStat> = emptyList(), // <--- Добавили
    val groups: List<String>
)

data class DisciplineStat(
    val name: String,
    val score: Double,
    val trend: Double
)