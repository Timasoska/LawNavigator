package com.example.lawnavigator.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MiniTrendIndicator(trend: Double) {
    val isPositive = trend > 0
    val isNeutral = trend == 0.0

    val color = when {
        isPositive -> Color(0xFF4CAF50) // Green
        isNeutral -> Color.Gray
        else -> Color(0xFFF44336)       // Red
    }

    val icon = when {
        isPositive -> Icons.Default.KeyboardArrowUp
        isNeutral -> Icons.Default.Refresh
        else -> Icons.Default.KeyboardArrowDown
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(32.dp)
    )
    Text(
        text = String.format("%+.1f", trend), // Вывод: "+5.2" или "-1.0"
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold
    )
}