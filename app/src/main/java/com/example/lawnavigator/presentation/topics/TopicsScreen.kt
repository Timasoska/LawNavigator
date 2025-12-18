package com.example.lawnavigator.presentation.topics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
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
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox
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
    onNavigateToLecture: (Int) -> Unit, // Это ведет на список лекций
    onNavigateToTest: (Int) -> Unit,
    onNavigateToCreateTest: (Int) -> Unit // <--- Новый колбэк
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TopicsContract.Effect.NavigateBack -> onNavigateBack()
                is TopicsContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.topicId)
                // Обработка перехода в конструктор
                is TopicsContract.Effect.NavigateToTestCreator -> onNavigateToCreateTest(effect.topicId) // Вызываем навигацию с ID
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Темы") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(TopicsContract.Event.OnRetryClicked) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when {
                state.isLoading -> LoadingScreen()

                state.error != null -> {
                    ErrorScreen(
                        message = state.error ?: "Ошибка загрузки тем",
                        onRetry = { viewModel.setEvent(TopicsContract.Event.OnRetryClicked) }
                    )
                }
                state.topics.isEmpty() -> {
                    EmptyScreen(message = "В этой дисциплине пока нет тем")
                }
                else -> {
                    LazyColumn {
                        items(state.topics) { topic ->
                            ListItem(
                                headlineContent = {  Text("${topic.name}")},
                                modifier = Modifier.clickable {
                                    viewModel.setEvent(TopicsContract.Event.OnTopicClicked(topic.id))
                                },
                                trailingContent = {
                                    Row {
                                        // 1. Кнопка "Конструктор" (ТОЛЬКО ДЛЯ УЧИТЕЛЯ)
                                        if (state.isTeacher) {
                                            IconButton(onClick = {
                                                viewModel.setEvent(TopicsContract.Event.OnCreateTestClicked(topic.id))
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Build,
                                                    contentDescription = "Редактировать тест"
                                                )
                                            }
                                        }

                                        // 2. Кнопка "Пройти тест" (Для всех)
                                        IconButton(onClick = { onNavigateToTest(topic.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Пройти тест"
                                            )
                                        }
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