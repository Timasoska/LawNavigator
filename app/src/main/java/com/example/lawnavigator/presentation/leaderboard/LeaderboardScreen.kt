package com.example.lawnavigator.presentation.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.lawnavigator.domain.model.LeaderboardUser
import com.example.lawnavigator.presentation.components.ErrorScreen
import com.example.lawnavigator.presentation.components.LoadingScreen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LeaderboardContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Таблица лидеров") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(LeaderboardContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> LoadingScreen()
                state.error != null -> ErrorScreen(
                    message = state.error ?: "Ошибка",
                    onRetry = { viewModel.setEvent(LeaderboardContract.Event.OnRetryClicked) }
                )
                else -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(state.users) { user ->
                            LeaderboardItem(user)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(user: LeaderboardUser) {
    val rankColor = when (user.rank) {
        1 -> Color(0xFFFFD700) // Золото
        2 -> Color(0xFFC0C0C0) // Серебро
        3 -> Color(0xFFCD7F32) // Бронза
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кружочек с местом
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${user.rank}",
                    fontWeight = FontWeight.Bold,
                    color = if (user.rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Показываем email до "собаки"
                Text(
                    text = user.email.substringBefore("@"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Рейтинг: ${user.score}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            if (user.rank == 1) {
                Text("👑", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}