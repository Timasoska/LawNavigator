package com.example.lawnavigator.presentation.teacher_groups.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.presentation.components.*
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.example.lawnavigator.domain.model.TopicStat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentReportScreen(
    viewModel: StudentReportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            if (effect is StudentReportContract.Effect.NavigateBack) onNavigateBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(state.report?.email?.substringBefore("@") ?: "Отчет студента", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(StudentReportContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(StudentReportContract.Event.OnRefresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            state.report?.let { report ->
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        Text("Динамика оценок:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        ScoreChart(
                            scores = report.history,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Средний балл: ${report.averageScore.toInt()}%", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                    Text("Тренд: ${String.format("%+.2f", report.trend)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                                MiniTrendIndicator(trend = report.trend)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Успеваемость по темам и лекциям:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Используем отдельный компонент для раскрывающейся карточки
                    items(report.topicStats) { topic ->
                        ExpandableTopicCard(topic)
                    }
                }
            }
        }
    }
}


@Composable
fun ExpandableTopicCard(topic: TopicStat) {
    var expanded by remember { mutableStateOf(false) }
    val topicColor = if (topic.averageScore >= 60) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiary

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ЗАГОЛОВОК ТЕМЫ
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(topic.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // ПРОГРЕСС ТЕМЫ
            LinearProgressIndicator(
                progress = { (topic.averageScore / 100).toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = topicColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Итог темы: ${topic.averageScore.toInt()}%", style = MaterialTheme.typography.labelSmall, color = topicColor, fontWeight = FontWeight.Bold)
                Text("Попыток: ${topic.attemptsCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // РАСКРЫВАЮЩИЙСЯ СПИСОК ЛЕКЦИЙ
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (topic.lectures.isEmpty()) {
                        Text("Нет лекций с тестами", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        topic.lectures.forEach { lecture ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${lecture.title}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                val scoreText = if (lecture.score != null) "${lecture.score}%" else "—"
                                val scoreColor = if (lecture.score != null && lecture.score >= 60) Color(0xFF4CAF50)
                                else if (lecture.score != null) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.outlineVariant

                                Box(
                                    modifier = Modifier.background(scoreColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(scoreText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = scoreColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}