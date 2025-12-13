package com.example.lawnavigator.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ScoreChart(
    scores: List<Int>,
    modifier: Modifier = Modifier,
    graphColor: Color = MaterialTheme.colorScheme.primary
) {
    if (scores.isEmpty()) return

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val maxScore = 100f

            // Расстояние между точками по X
            val stepX = if (scores.size > 1) width / (scores.size - 1) else 0f

            val path = Path()

            scores.forEachIndexed { index, score ->
                // Координата X
                val x = index * stepX
                // Координата Y (инвертируем, т.к. 0 сверху)
                val y = height - (score / maxScore * height)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Рисуем точку
                drawCircle(
                    color = graphColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Рисуем линию
            drawPath(
                path = path,
                color = graphColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // (Опционально) Можно добавить градиент под графиком
        }
    }
}