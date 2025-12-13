package com.example.lawnavigator.data.dto


import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardItemDto(
    val email: String,
    val score: Double,
    val testsPassed: Int
)