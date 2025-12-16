package com.example.lawnavigator.presentation.lecture

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureScreen(
    viewModel: LectureViewModel = hiltViewModel(),
    searchQuery: String?,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isHighlightVisible by remember { mutableStateOf(false) }
    var hasScrolledToSearchQuery by remember { mutableStateOf(false) }

    // АВТО-СКРОЛЛ И ПОДСВЕТКА
    LaunchedEffect(state.initialScrollIndex, searchQuery, state.lecture) {
        val currentLecture = state.lecture
        if (currentLecture != null && (state.initialScrollIndex > 0 || (searchQuery != null && !hasScrolledToSearchQuery))) {
            if (searchQuery != null) isHighlightVisible = true
            delay(600)
            val targetScrollPosition = if (state.initialScrollIndex > 0) state.initialScrollIndex
            else if (searchQuery != null && !hasScrolledToSearchQuery) {
                val index = currentLecture.content.lowercase().indexOf(searchQuery.lowercase())
                if (index != -1) (index * 1.5).toInt() else 0
            } else 0

            if (targetScrollPosition > 0) {
                scrollState.animateScrollTo(value = targetScrollPosition, animationSpec = androidx.compose.animation.core.tween(1500))
                if (isHighlightVisible) { delay(2000); isHighlightVisible = false }
                hasScrolledToSearchQuery = true
            }
        }
    }

    // Логика выхода
    fun saveAndExit() {
        if (!state.isEditing) viewModel.setEvent(LectureContract.Event.OnSaveProgress(scrollState.value))
        viewModel.setEvent(LectureContract.Event.OnBackClicked)
    }

    // УМНЫЙ BACK HANDLER
    // 1. Если открыт диалог удаления -> закрываем диалог
    // 2. Если режим редактирования -> отменяем редактирование
    // 3. Иначе -> сохраняем позицию и выходим
    BackHandler {
        when {
            state.showDeleteDialog -> viewModel.setEvent(LectureContract.Event.OnDismissDeleteDialog)
            state.isEditing -> viewModel.setEvent(LectureContract.Event.OnCancelEditClicked)
            else -> saveAndExit()
        }
    }

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LectureContract.Effect.NavigateBack -> onNavigateBack()
                is LectureContract.Effect.ShowMessage -> Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ДИАЛОГ УДАЛЕНИЯ (Вынесли наверх для чистоты)
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(LectureContract.Event.OnDismissDeleteDialog) },
            title = { Text("Удалить лекцию?") },
            text = { Text("Это действие нельзя отменить. Лекция будет удалена навсегда.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.setEvent(LectureContract.Event.OnConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(LectureContract.Event.OnDismissDeleteDialog) }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Редактирование" else state.lecture?.title ?: "Лекция", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isEditing) viewModel.setEvent(LectureContract.Event.OnCancelEditClicked)
                        else saveAndExit()
                    }) {
                        Icon(if (state.isEditing) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        // Кнопка СОХРАНИТЬ
                        IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnSaveEditsClicked) }) {
                            Icon(Icons.Default.Check, contentDescription = "Сохранить", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        // ДЛЯ УЧИТЕЛЯ
                        if (state.isTeacher) {
                            IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnEditClicked) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                            }
                            IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnDeleteClicked) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        // ЛАЙК
                        IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnFavoriteClicked) }) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {

                // === РЕЖИМ РЕДАКТИРОВАНИЯ ===
                if (state.isEditing) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = state.editedTitle,
                            onValueChange = { viewModel.setEvent(LectureContract.Event.OnTitleChanged(it)) },
                            label = { Text("Заголовок") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.editedContent,
                            onValueChange = { viewModel.setEvent(LectureContract.Event.OnContentChanged(it)) },
                            label = { Text("Содержание (Markdown)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start)
                        )
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                // === РЕЖИМ ПРОСМОТРА ===
                else {
                    state.lecture?.let { lecture ->
                        val contentToDisplay = remember(lecture.content, searchQuery, isHighlightVisible) {
                            if (isHighlightVisible) simpleHighlight(lecture.content, searchQuery) else lecture.content
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(scrollState)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = lecture.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            MarkdownText(
                                markdown = contentToDisplay,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun simpleHighlight(markdown: String, query: String?): String {
    if (query.isNullOrBlank()) return markdown
    val escapedQuery = Regex.escape(query)
    return markdown.replace(Regex(escapedQuery, RegexOption.IGNORE_CASE), "`$0`")
}