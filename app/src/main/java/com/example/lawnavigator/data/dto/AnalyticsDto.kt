package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO для получения общей статистики студента.
 * Приходит с бэкенда: GET /api/analytics/progress
 */
@Serializable
data class ProgressDto(
    val testsPassed: Int,
    val averageScore: Double,
    val history: List<Int> = emptyList(), // <--- ДОБАВЬ ЭТУ СТРОКУ
    val trend: Double, // <--- Добавили
    val disciplines: List<DisciplineStatDto> = emptyList()
)

@Serializable
data class DisciplineStatDto(
    val id: Int,
    val name: String,
    val averageScore: Double,
    val trend: Double
)