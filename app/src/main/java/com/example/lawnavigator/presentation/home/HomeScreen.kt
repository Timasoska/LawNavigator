package com.example.lawnavigator.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.lawnavigator.presentation.components.EmptyScreen
import com.example.lawnavigator.presentation.components.ErrorScreen
import com.example.lawnavigator.presentation.components.LoadingScreen
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTopics: (Int) -> Unit,
    onNavigateToProfile: () -> Unit, // <--- Добавляем новый колбэк
    onNavigateToSearch: () -> Unit, // <--- 1. ДОБАВЛЯЕМ ЭТОТ ПАРАМЕТР
    onNavigateToFavorites: () -> Unit // <--- 1. НОВЫЙ ПАРАМЕТР
) {
    val state by viewModel.state.collectAsState()

    // Обработка навигации
    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeContract.Effect.NavigateToTopics -> onNavigateToTopics(effect.disciplineId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Дисциплины") },
                actions = {
                    // Поиск
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    }

                    // ИЗБРАННОЕ (СЕРДЕЧКО)
                    IconButton(onClick = onNavigateToFavorites) { // <--- 2. ВЫЗОВ
                        Icon(Icons.Default.Favorite, contentDescription = "Избранное")
                    }

                    // Профиль
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 1. Загрузка
                state.isLoading -> LoadingScreen()

                // 2. Ошибка
                state.error != null -> {
                    ErrorScreen(
                        message = state.error ?: "Неизвестная ошибка",
                        onRetry = { viewModel.setEvent(HomeContract.Event.OnRetryClicked) }
                    )
                }

                // 3. Пустой список (если вдруг с сервера пришел пустой массив)
                state.disciplines.isEmpty() -> {
                    EmptyScreen(message = "Список дисциплин пуст")
                }

                // 4. Контент
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.disciplines) { discipline ->
                            DisciplineCard(
                                name = discipline.name,
                                description = discipline.description,
                                onClick = { viewModel.setEvent(HomeContract.Event.OnDisciplineClicked(discipline.id)) }
                            )
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