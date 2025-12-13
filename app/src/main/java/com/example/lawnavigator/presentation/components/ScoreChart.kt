package com.example.lawnavigator.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

@Composable
fun ScoreChart(
    scores: List<Int>,
    modifier: Modifier = Modifier,
    graphColor: Color = MaterialTheme.colorScheme.primary
) {
    if (scores.isEmpty()) return

    // Анимация: прогресс от 0.0 до 1.0
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(scores) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. ШКАЛА Y (статичная) ---
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

        // --- 2. ГРАФИК (Анимированный) ---
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val maxScore = 100f

                val stepX = if (scores.size > 1) width / (scores.size - 1) else 0f

                // Вычисляем координаты всех точек
                val points = scores.mapIndexed { index, score ->
                    val x = index * stepX
                    val y = height - (score / maxScore * height)
                    Offset(x, y)
                }

                // 1. Рисуем сетку
                val gridLines = listOf(0f, height / 2, height)
                gridLines.forEach { y ->
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                if (points.isEmpty()) return@Canvas

                // 2. Создаем ПЛАВНЫЙ путь (Математика Безье)
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]

                        val controlPoint1 = Offset((p1.x + p2.x) / 2f, p1.y)
                        val controlPoint2 = Offset((p1.x + p2.x) / 2f, p2.y)

                        cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                    }
                }

                // 3. Создаем путь для заливки
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }

                val currentWidth = width * animationProgress.value

                // Рисуем в обрезанной области (Анимация)
                withTransform({
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = currentWidth,
                        bottom = height
                    )
                }) {
                    // Заливка
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(graphColor.copy(alpha = 0.4f), Color.Transparent),
                            endY = height
                        )
                    )

                    // Линия
                    drawPath(
                        path = path,
                        color = graphColor,
                        style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }

                // 4. Рисуем точки
                points.forEach { point ->
                    if (point.x <= currentWidth) {
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = point
                        )
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
}