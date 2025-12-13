package com.example.lawnavigator.presentation.utils

/**
 * Простая линейная регрессия для расчета тренда на клиенте.
 * Используется в интерактивном симуляторе оценок.
 */
fun calculateTrendLocal(scores: List<Int>): Double {
    if (scores.size < 2) return 0.0
    val n = scores.size.toDouble()
    var sumX = 0.0
    var sumY = 0.0
    var sumXY = 0.0
    var sumX2 = 0.0

    scores.forEachIndexed { index, score ->
        val x = index.toDouble()
        val y = score.toDouble()
        sumX += x
        sumY += y
        sumXY += x * y
        sumX2 += x * x
    }
    val denominator = n * sumX2 - sumX * sumX
    if (denominator == 0.0) return 0.0
    return (n * sumXY - sumX * sumY) / denominator
}