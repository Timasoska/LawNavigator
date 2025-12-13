package com.example.lawnavigator.presentation.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.presentation.components.EmptyScreen
import com.example.lawnavigator.presentation.components.ErrorScreen
import com.example.lawnavigator.presentation.components.LoadingScreen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLecture: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is FavoritesContract.Effect.NavigateBack -> onNavigateBack()
                is FavoritesContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.lectureId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(FavoritesContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 1. Загрузка
                state.isLoading -> LoadingScreen()

                // 2. Ошибка
                state.error != null -> {
                    ErrorScreen(
                        message = state.error ?: "Ошибка загрузки",
                        onRetry = { viewModel.setEvent(FavoritesContract.Event.OnRetryClicked) } // <--- Правильный ивент
                    )
                }

                // 3. Пусто
                state.favorites.isEmpty() -> {
                    EmptyScreen(
                        message = "У вас пока нет избранных лекций",
                        icon = Icons.Default.Favorite // Можно использовать сердечко
                    )
                }

                // 4. Список
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.favorites) { lecture ->
                            ListItem(
                                headlineContent = { Text(lecture.title) },
                                supportingContent = {
                                    Text(
                                        text = lecture.content.take(50).replace("\n", " ") + "...",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.clickable {
                                    viewModel.setEvent(FavoritesContract.Event.OnLectureClicked(lecture.id))
                                },
                                trailingContent = {
                                    // Кнопка удаления
                                    IconButton(onClick = {
                                        viewModel.setEvent(FavoritesContract.Event.OnRemoveClicked(lecture.id))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Удалить",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}