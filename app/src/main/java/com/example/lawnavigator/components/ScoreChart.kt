package com.example.lawnavigator.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. ШКАЛА (Y-axis) ---
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text("100", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text("50", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text("0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- 2. ГРАФИК ---
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val maxScore = 100f

                // Если точек мало, распределяем их равномерно
                val stepX = if (scores.size > 1) width / (scores.size - 1) else 0f

                // Подготовка координат
                val points = scores.mapIndexed { index, score ->
                    val x = index * stepX
                    val y = height - (score / maxScore * height)
                    Offset(x, y)
                }

                // 1. Рисуем сетку (пунктирные линии)
                val gridLines = listOf(0f, height / 2, height) // Верх, Центр, Низ
                gridLines.forEach { y ->
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // 2. Путь для линии
                val path = Path().apply {
                    points.forEachIndexed { index, point ->
                        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                    }
                }

                // 3. Путь для градиента (заливка под графиком)
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height) // Вниз вправо
                    lineTo(0f, height)    // Вниз влево
                    close()
                }

                // Рисуем заливку (Градиент сверху вниз)
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            graphColor.copy(alpha = 0.4f), // Полупрозрачный цвет сверху
                            Color.Transparent              // Прозрачный снизу
                        ),
                        endY = height
                    )
                )

                // Рисуем саму линию
                drawPath(
                    path = path,
                    color = graphColor,
                    style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )

                // 4. Рисуем точки
                points.forEach { point ->
                    // Белая подложка (обводка)
                    drawCircle(
                        color = Color.White, // Или цвет фона (MaterialTheme.colorScheme.surface)
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    // Цветная точка
                    drawCircle(
                        color = graphColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }
    }
}