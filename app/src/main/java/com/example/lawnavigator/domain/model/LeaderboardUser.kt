package com.example.lawnavigator.domain.model

data class LeaderboardUser(
    val email: String,
    val score: Double,
    val rank: Int // Место в рейтинге (1, 2, 3...)
)