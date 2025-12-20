package com.example.lawnavigator.presentation.profile.details

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
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox
import com.example.lawnavigator.presentation.components.LoadingScreen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplineDetailsScreen(
    viewModel: DisciplineDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DisciplineDetailsContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.disciplineName) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(DisciplineDetailsContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(DisciplineDetailsContract.Event.OnRefresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            if (state.isLoading && state.topics.isEmpty()) {
                LoadingScreen()
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        Text(
                            "Успеваемость по темам:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items(state.topics) { topic ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(topic.name, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ср. балл: ${topic.averageScore.toInt()}%", style = MaterialTheme.typography.bodySmall)
                                    Text("Попыток: ${topic.attemptsCount}", style = MaterialTheme.typography.bodySmall)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { (topic.averageScore / 100).toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = if (topic.averageScore >= 60) Color(0xFF4CAF50) else Color(0xFFFFC107)
                                )

                                topic.lastScore?.let { last ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Последний результат: $last%",
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
}