package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO для получения общей статистики студента.
 * Приходит с бэкенда: GET /api/analytics/progress
 */

@Serializable
data class UserGroupShortDto(val id: Int, val name: String)

@Serializable
data class ProgressDto(
    val testsPassed: Int,
    val averageScore: Double,
    val trend: Double,
    val disciplines: List<DisciplineStatDto> = emptyList(),
    val history: List<Int> = emptyList(),
    val groups: List<UserGroupShortDto> = emptyList() // Изменили со String на объект
)

@Serializable
data class DisciplineStatDto(
    val id: Int,
    val name: String,
    val averageScore: Double,
    val trend: Double
)