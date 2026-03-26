package com.example.lawnavigator.presentation.topics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.*

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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

    if (state.showTopicDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onDismissRequest = { viewModel.setEvent(TopicsContract.Event.OnDismissDialogs) },
            title = { Text(if (state.editingTopicId == null) "Новая тема" else "Изменить тему") },
            text = {
                OutlinedTextField(
                    value = state.topicNameInput,
                    onValueChange = { viewModel.setEvent(TopicsContract.Event.OnTopicNameChanged(it)) },
                    label = { Text("Название темы") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.setEvent(TopicsContract.Event.OnSaveTopic) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Сохранить", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnDismissDialogs) }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            onDismissRequest = { viewModel.setEvent(TopicsContract.Event.OnDismissDialogs) },
            title = { Text("Удалить тему?") },
            text = { Text("Будут удалены все лекции и тесты. Это действие нельзя отменить.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { viewModel.setEvent(TopicsContract.Event.OnConfirmDeleteTopic) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) { Text("Удалить", color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnDismissDialogs) }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Содержание", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isTeacher) {
                FloatingActionButton(
                    onClick = { viewModel.setEvent(TopicsContract.Event.OnAddTopicClicked) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, "Добавить")
                }
            }
        }
    ) { padding ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(TopicsContract.Event.OnRetryClicked) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when {
                state.isLoading && state.topics.isEmpty() -> LoadingScreen()
                state.error != null -> ErrorScreen(message = state.error ?: "Ошибка", onRetry = { viewModel.setEvent(TopicsContract.Event.OnRetryClicked) })
                state.topics.isEmpty() -> EmptyScreen(message = "Здесь пока нет тем")
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("УЧЕБНЫЙ ПЛАН", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        items(state.topics) { topic ->
                            StitchTopicCard(
                                topic = topic,
                                isTeacher = state.isTeacher,
                                onClick = { viewModel.setEvent(TopicsContract.Event.OnTopicClicked(topic.id)) },
                                onTestClick = { onNavigateToTest(topic.id) },
                                onEditClick = { viewModel.setEvent(TopicsContract.Event.OnEditTopicClicked(topic)) },
                                onDeleteClick = { viewModel.setEvent(TopicsContract.Event.OnDeleteTopicClicked(topic.id)) },
                                onCreateTestClick = { viewModel.setEvent(TopicsContract.Event.OnCreateTestClicked(topic.id)) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun StitchTopicCard(
    topic: com.example.lawnavigator.domain.model.Topic,
    isTeacher: Boolean,
    onClick: () -> Unit,
    onTestClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCreateTestClick: () -> Unit
) {
    val progressScore = topic.progress ?: 0
    val progressColor = if (progressScore >= 60) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = topic.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp)

                if (topic.progress != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("ОСВОЕНИЕ", color = progressColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("${topic.progress}%", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (topic.progress / 100f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceTint
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isTeacher) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, contentDescription = "Изменить", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onCreateTestClick, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Build, contentDescription = "Редактор теста", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onTestClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Пройти тест", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}