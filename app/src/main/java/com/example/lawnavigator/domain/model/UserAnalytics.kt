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
    val groups: List<UserGroup>
)

data class DisciplineStat(
    val id: Int, // Обязательно Int для навигации
    val name: String,
    val score: Double,
    val trend: Double
)

data class TopicStat(
    val topicId: Int,
    val name: String,
    val averageScore: Double,
    val attemptsCount: Int,
    val lastScore: Int?
)

data class StudentDetailedReport(
    val email: String,
    val averageScore: Double,
    val trend: Double,
    val topicStats: List<TopicStat>,
    val history: List<Int>
)