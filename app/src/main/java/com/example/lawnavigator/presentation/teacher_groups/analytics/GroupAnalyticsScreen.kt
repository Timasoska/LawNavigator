package com.example.lawnavigator.presentation.teacher_groups.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.data.dto.StudentRiskDto
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox
import com.example.lawnavigator.presentation.components.MiniTrendIndicator
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupAnalyticsScreen(
    viewModel: GroupAnalyticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is GroupAnalyticsContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика группы") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(GroupAnalyticsContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(GroupAnalyticsContract.Event.OnRefresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    Text("Студенты в зоне риска:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ИСПОЛЬЗУЕМ СПИСОК СТУДЕНТОВ ИЗ STATE
                items(state.students) { student ->
                    StudentCard(
                        student = student,
                        onRemoveClick = {
                            // Вызываем событие удаления (нужно добавить его в контракт, если нет)
                            viewModel.setEvent(GroupAnalyticsContract.Event.OnRemoveStudentClicked(student.studentId))
                        }
                    )
                }

                if (state.students.isEmpty() && !state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                            Text("В группе пока нет студентов", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCard(
    student: StudentRiskDto,
    onRemoveClick: () -> Unit
) {
    val riskColor = when (student.riskLevel) {
        "RED" -> Color(0xFFE53935)   // Красный
        "YELLOW" -> Color(0xFFFFB300) // Желтый
        else -> Color(0xFF43A047)     // Зеленый
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Индикатор риска (Кружочек)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(riskColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.email, fontWeight = FontWeight.Bold)
                Text(
                    text = "Ср. балл: ${String.format("%.1f", student.averageScore)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Тренд (Стрелочка)
            MiniTrendIndicator(trend = student.trend)

            Spacer(modifier = Modifier.width(8.dp))

            // Кнопка удаления
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Close, contentDescription = "Исключить", tint = Color.Gray)
            }
        }
    }
}