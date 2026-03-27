package com.example.lawnavigator.presentation.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateTrendLocalTest {

    @Test
    fun `calculateTrendLocal returns 0 when less than 2 elements`() {
        val emptyList = emptyList<Int>()
        val singleElement = listOf(85)

        assertEquals(0.0, calculateTrendLocal(emptyList), 0.01)
        assertEquals(0.0, calculateTrendLocal(singleElement), 0.01)
    }

    @Test
    fun `calculateTrendLocal returns positive trend for increasing scores`() {
        val scores = listOf(50, 60, 70, 80, 90)
        val trend = calculateTrendLocal(scores)

        assertTrue("Trend should be positive", trend > 0)
        // Для этого идеального линейного роста тренд равен 10.0
        assertEquals(10.0, trend, 0.01)
    }

    @Test
    fun `calculateTrendLocal returns negative trend for decreasing scores`() {
        val scores = listOf(90, 80, 70, 60, 50)
        val trend = calculateTrendLocal(scores)

        assertTrue("Trend should be negative", trend < 0)
        // Для этого идеального спада тренд равен -10.0
        assertEquals(-10.0, trend, 0.01)
    }

    @Test
    fun `calculateTrendLocal returns 0 for flat scores`() {
        val scores = listOf(75, 75, 75, 75, 75)
        val trend = calculateTrendLocal(scores)

        assertEquals(0.0, trend, 0.01)
    }
}