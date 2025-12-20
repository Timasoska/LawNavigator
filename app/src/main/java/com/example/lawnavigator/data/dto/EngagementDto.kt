package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class EngagementStatusDto(
    val streak: Int,
    val todayXp: Int,
    val dailyGoalXp: Int,
    val totalXp: Int,
    val isDailyGoalReached: Boolean
)

@Serializable
data class AddXpRequestDto(
    val amount: Int,
    val source: String
)