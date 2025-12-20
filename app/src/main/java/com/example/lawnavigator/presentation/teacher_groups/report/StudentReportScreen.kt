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
        topBar = {
            TopAppBar(
                title = { Text(state.report?.email ?: "Отчет студента") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(StudentReportContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
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
                    // 1. График истории
                    item {
                        Text("Динамика оценок:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ScoreChart(
                            scores = report.history,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 2. Общие цифры
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Средний балл: ${report.averageScore.toInt()}%")
                                    Text("Тренд: ${String.format("%+.2f", report.trend)}")
                                }
                                TrendIndicator(trend = report.trend)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Успеваемость по темам:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 3. Список тем
                    items(report.topicStats) { topic ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(topic.name, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (topic.averageScore / 100).toFloat() },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (topic.averageScore >= 60) Color(0xFF4CAF50) else Color(0xFFFFC107)
                                )
                                Text(
                                    "Ср. балл: ${topic.averageScore.toInt()}% | Попыток: ${topic.attemptsCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}