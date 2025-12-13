package com.example.lawnavigator.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.components.ScoreChart
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    // 1. ДОБАВИЛИ НОВЫЙ КОЛБЭК
    onNavigateToTopic: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileContract.Effect.NavigateToLogin -> onNavigateToLogin()
                is ProfileContract.Effect.NavigateBack -> onNavigateBack()
                // 2. ОБРАБАТЫВАЕМ ЭФФЕКТ ПЕРЕХОДА
                is ProfileContract.Effect.NavigateToTopic -> onNavigateToTopic(effect.topicId)
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnLogoutClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Выход")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ваш прогресс", style = MaterialTheme.typography.titleLarge)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Левая часть: Цифры
                                Column {
                                    Text("Пройдено тестов: ${state.analytics?.testsPassed ?: 0}")
                                    Text("Средний балл: ${state.analytics?.averageScore ?: 0.0}")

                                    // --- НОВАЯ СТРОКА: МАТЕМАТИЧЕСКИЙ ПРОГНОЗ ---
                                    state.analytics?.let { analytics ->
                                        val prediction = (analytics.averageScore + analytics.trend).coerceIn(0.0, 100.0)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Прогноз: ${String.format("%.1f", prediction)}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Правая часть: Тренд (Стрелка)
                                state.analytics?.let { analytics ->
                                    TrendIndicator(trend = analytics.trend)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. ГРАФИК (Вставляем сюда!)
                    Text("Динамика оценок:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val history = state.analytics?.history ?: emptyList()
                    if (history.isNotEmpty()) {
                        ScoreChart(
                            scores = history,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp) // Высота графика
                        )
                    } else {
                        Text("Пока нет данных для графика", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Успеваемость по предметам:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    state.analytics?.disciplines?.forEach { disc ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(disc.name, style = MaterialTheme.typography.bodyMedium)
                                Text("${disc.score.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Прогресс-бар
                            LinearProgressIndicator(
                                progress = { (disc.score / 100).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp)),
                                color = if (disc.score >= 60) Color(0xFF4CAF50) else Color(0xFFFFC107),
                            )
                        }
                    }

                    Text("Рекомендуем повторить:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val recs = state.analytics?.recommendations ?: emptyList()
                    if (recs.isEmpty()) {
                        Text("У вас нет задолженностей! 🎉", color = Color.Gray)
                    } else {
                        LazyColumn {
                            items(recs) { topic ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        // 3. ДОБАВИЛИ КЛИК ПО КАРТОЧКЕ
                                        .clickable {
                                            viewModel.setEvent(ProfileContract.Event.OnRecommendationClicked(topic.id))
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Text(
                                        text = topic.name,
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendIndicator(trend: Double) {
    val isPositive = trend > 0
    val isNeutral = trend == 0.0

    val color = when {
        isPositive -> Color(0xFF4CAF50)
        isNeutral -> Color.Gray
        else -> Color(0xFFF44336)
    }

    val icon = when {
        isPositive -> Icons.Default.KeyboardArrowUp
        isNeutral -> Icons.Default.Refresh
        else -> Icons.Default.KeyboardArrowDown
    }

    val text = when {
        isPositive -> "Рост"
        isNeutral -> "Стабильно"
        else -> "Спад"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

