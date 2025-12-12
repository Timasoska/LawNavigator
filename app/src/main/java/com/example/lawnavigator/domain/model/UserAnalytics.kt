package com.example.lawnavigator.domain.model

/**
 * Чистая модель аналитики для отображения в UI.
 */
data class UserAnalytics(
    val testsPassed: Int,
    val averageScore: Double,
    val trend: Double,
    val recommendations: List<Topic> // Переиспользуем модель Topic
)