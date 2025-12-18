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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
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
    onNavigateToTest: (Int) -> Unit,
    onNavigateToCreateTest: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TopicsContract.Effect.NavigateBack -> onNavigateBack()
                is TopicsContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.topicId)
                is TopicsContract.Effect.NavigateToTestCreator -> onNavigateToCreateTest(effect.topicId)
            }
        }
    }

    // --- ДИАЛОГ СОЗДАНИЯ / РЕДАКТИРОВАНИЯ ---
    if (state.showTopicDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(TopicsContract.Event.OnDismissDialogs) },
            title = { Text(if (state.editingTopicId == null) "Новая тема" else "Изменить тему") },
            text = {
                OutlinedTextField(
                    value = state.topicNameInput,
                    onValueChange = { viewModel.setEvent(TopicsContract.Event.OnTopicNameChanged(it)) },
                    label = { Text("Название") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.setEvent(TopicsContract.Event.OnSaveTopic) }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnDismissDialogs) }) {
                    Text("Отмена")
                }
            }
        )
    }

    // --- ДИАЛОГ УДАЛЕНИЯ ---
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(TopicsContract.Event.OnDismissDialogs) },
            title = { Text("Удалить тему?") },
            text = { Text("Внимание! Будут удалены все лекции и тесты внутри этой темы. Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.setEvent(TopicsContract.Event.OnConfirmDeleteTopic) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnDismissDialogs) }) {
                    Text("Отмена")
                }
            }
        )
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
        },
        // КНОПКА СОЗДАНИЯ ТЕМЫ (Только для учителя)
        floatingActionButton = {
            if (state.isTeacher) {
                FloatingActionButton(
                    onClick = { viewModel.setEvent(TopicsContract.Event.OnAddTopicClicked) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить тему")
                }
            }
        }
    ) { padding ->
        // Pull-to-Refresh обертка
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(TopicsContract.Event.OnRetryClicked) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when {
                // Если грузимся и список пуст - показываем крутилку по центру
                state.isLoading && state.topics.isEmpty() -> LoadingScreen()

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
                                headlineContent = { Text(topic.name) },
                                modifier = Modifier.clickable {
                                    viewModel.setEvent(TopicsContract.Event.OnTopicClicked(topic.id))
                                },
                                trailingContent = {
                                    Row {
                                        // 1. УЧИТЕЛЬ: Инструменты управления
                                        if (state.isTeacher) {
                                            // Редактировать (Карандаш)
                                            IconButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnEditTopicClicked(topic)) }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                                            }
                                            // Удалить (Корзина)
                                            IconButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnDeleteTopicClicked(topic.id)) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                            // Конструктор теста (Гаечный ключ)
                                            IconButton(onClick = {
                                                viewModel.setEvent(TopicsContract.Event.OnCreateTestClicked(topic.id))
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Build,
                                                    contentDescription = "Редактировать тест",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        // 2. ВСЕ: Кнопка "Пройти тест"
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