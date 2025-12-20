package com.example.lawnavigator.domain.model

/**
 * Чистая модель аналитики для отображения в UI.
 */
data class UserGroup(val id: Int, val name: String)

data class UserAnalytics(
    val testsPassed: Int,
    val averageScore: Double,
    val trend: Double,
    val history: List<Int>,
    val recommendations: List<Topic>,
    val disciplines: List<DisciplineStat> = emptyList(),
    val groups: List<UserGroup> // Изменили тип
)

data class DisciplineStat(
    val name: String,
    val score: Double,
    val trend: Double
)