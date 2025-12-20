package com.example.lawnavigator.presentation.teacher_groups.analytics

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
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
    onNavigateBack: () -> Unit,
    // ДОБАВЛЯЕМ ПАРАМЕТР НАВИГАЦИИ
    onNavigateToStudentReport: (Int, Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is GroupAnalyticsContract.Effect.NavigateBack -> onNavigateBack()
                is GroupAnalyticsContract.Effect.ShowMessage -> Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
                // ОБРАБОТКА ЭФФЕКТА ПЕРЕХОДА
                is GroupAnalyticsContract.Effect.NavigateToStudentReport -> {
                    onNavigateToStudentReport(effect.groupId, effect.studentId)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика группы") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(GroupAnalyticsContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
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

                items(state.students) { student ->
                    StudentCard(
                        student = student,
                        // ВЫЗЫВАЕМ СОБЫТИЕ ПРИ КЛИКЕ НА КАРТОЧКУ
                        onCardClick = {
                            viewModel.setEvent(GroupAnalyticsContract.Event.OnStudentClicked(student.studentId))
                        },
                        onRemoveClick = {
                            viewModel.setEvent(GroupAnalyticsContract.Event.OnRemoveStudentClicked(student.studentId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentCard(
    student: StudentRiskDto,
    onCardClick: () -> Unit, // <--- ДОБАВЛЕН ПАРАМЕТР
    onRemoveClick: () -> Unit
) {
    val riskColor = when (student.riskLevel) {
        "RED" -> Color(0xFFE53935)
        "YELLOW" -> Color(0xFFFFB300)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCardClick() }, // <--- ТЕПЕРЬ ОШИБКИ НЕТ
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(16.dp).background(riskColor, CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.email, fontWeight = FontWeight.Bold)
                Text(text = "Ср. балл: ${String.format("%.1f", student.averageScore)}")
            }
            MiniTrendIndicator(trend = student.trend)
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Close, contentDescription = "Исключить", tint = Color.Gray)
            }
        }
    }
}