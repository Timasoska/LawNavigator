package com.example.lawnavigator.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox // <--- ВЕРНУЛИ ИМПОРТ
import com.example.lawnavigator.presentation.components.DailyGoalCard
import com.example.lawnavigator.presentation.components.EmptyScreen
import com.example.lawnavigator.presentation.components.ErrorScreen
import com.example.lawnavigator.presentation.components.LoadingScreen
import com.example.lawnavigator.presentation.components.StreakBadge
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTopics: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToTeacherGroups: () -> Unit,
    onNavigateToFlashcards: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeContract.Effect.NavigateToTopics -> onNavigateToTopics(effect.disciplineId)
                is HomeContract.Effect.NavigateToTeacherGroups -> onNavigateToTeacherGroups()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Дисциплины") },
                actions = {
                    if (state.engagementStatus != null && state.engagementStatus!!.streak > 0) {
                        StreakBadge(streak = state.engagementStatus!!.streak)
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    }
                    IconButton(onClick = onNavigateToFlashcards) {
                        Icon(Icons.Default.Style, contentDescription = "Карточки")
                    }
                    IconButton(onClick = onNavigateToLeaderboard) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Рейтинг")
                    }
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(Icons.Default.Favorite, contentDescription = "Избранное")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                    if (state.isTeacher) {
                        IconButton(onClick = { viewModel.setEvent(HomeContract.Event.OnTeacherGroupsClicked) }) {
                            Icon(Icons.Default.Group, contentDescription = "Мои группы")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // --- ОБЕРТКА PULL TO REFRESH ---
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(HomeContract.Event.OnRefresh) },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            // Внутренний контент
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Показываем лоадер по центру только если список пуст (первый запуск)
                    state.isLoading && state.disciplines.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    state.error != null -> {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            ErrorScreen(
                                message = state.error ?: "Неизвестная ошибка",
                                onRetry = { viewModel.setEvent(HomeContract.Event.OnRetryClicked) }
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Виджет цели
                            if (state.engagementStatus != null) {
                                item {
                                    DailyGoalCard(status = state.engagementStatus!!)
                                }
                            }

                            // Список дисциплин
                            items(state.disciplines) { discipline ->
                                DisciplineCard(
                                    name = discipline.name,
                                    description = discipline.description,
                                    onClick = { viewModel.setEvent(HomeContract.Event.OnDisciplineClicked(discipline.id)) }
                                )
                            }

                            if (state.disciplines.isEmpty()) {
                                item {
                                    EmptyScreen(message = "Список дисциплин пуст")
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
fun DisciplineCard(name: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}