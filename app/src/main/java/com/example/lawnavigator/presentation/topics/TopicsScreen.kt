package com.example.lawnavigator.presentation.topics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.presentation.components.EmptyScreen
import com.example.lawnavigator.presentation.components.ErrorScreen
import com.example.lawnavigator.presentation.components.LoadingScreen
import kotlinx.coroutines.flow.collectLatest

/**
 * Экран отображения списка тем по выбранной дисциплине.
 *
 * @param viewModel ViewModel, управляющая состоянием экрана.
 * @param onNavigateBack Колбэк для возврата на предыдущий экран.
 * @param onNavigateToLecture Колбэк для перехода к чтению лекции.
 * @param onNavigateToTest Колбэк для перехода к тесту по теме.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    viewModel: TopicsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLecture: (Int) -> Unit,
    onNavigateToTest: (Int) -> Unit // <--- Новый колбэк (принимает topicId)
) {
    val state by viewModel.state.collectAsState()

    // Обработка эффектов (навигация)
    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TopicsContract.Effect.NavigateBack -> onNavigateBack()
                is TopicsContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.topicId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Темы") },
                navigationIcon = {
                    // Кнопка Назад
                    IconButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                // 1. Загрузка
                state.isLoading -> LoadingScreen()

                // 2. Ошибка
                state.error != null -> {
                    ErrorScreen(
                        message = state.error ?: "Ошибка загрузки тем",
                        onRetry = { viewModel.setEvent(TopicsContract.Event.OnRetryClicked) }
                    )
                }

                // 3. Пусто
                state.topics.isEmpty() -> {
                    EmptyScreen(message = "В этой дисциплине пока нет тем")
                }

                // 4. Данные
                else -> {
                    LazyColumn {
                        items(state.topics) { topic ->
                            ListItem(
                                headlineContent = { Text(topic.name) },
                                // По клику на саму строку -> Лекция
                                modifier = Modifier.clickable {
                                    viewModel.setEvent(TopicsContract.Event.OnTopicClicked(topic.id))
                                },
                                // Иконка справа -> Тест
                                trailingContent = {
                                    IconButton(onClick = { onNavigateToTest(topic.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Пройти тест"
                                        )
                                    }
                                },
                                shadowElevation = 2.dp
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}