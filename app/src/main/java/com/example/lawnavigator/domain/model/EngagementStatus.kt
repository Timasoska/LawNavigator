package com.example.lawnavigator.domain.model

data class EngagementStatus(
    val streak: Int,
    val todayXp: Int,
    val dailyGoalXp: Int,
    val totalXp: Int,
    val isDailyGoalReached: Boolean
) {
    // Вспомогательное свойство для прогресс-бара (от 0.0 до 1.0)
    val progress: Float
        get() = if (dailyGoalXp > 0) (todayXp.toFloat() / dailyGoalXp).coerceIn(0f, 1f) else 0f
}