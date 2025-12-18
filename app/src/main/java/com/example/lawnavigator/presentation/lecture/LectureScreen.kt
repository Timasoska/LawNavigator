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
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import com.example.lawnavigator.BuildConfig // Убедись, что импортируется BuildConfig твоего пакета


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureScreen(
    viewModel: LectureViewModel = hiltViewModel(),
    searchQuery: String?,
    onNavigateBack: () -> Unit,
    onNavigateToTest: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Лаунчер для выбора файла (Для учителя)
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bytes = readBytesFromUri(context, it)
            val name = getFileName(context, it) ?: "File"
            if (bytes != null) {
                viewModel.setEvent(LectureContract.Event.OnAttachFileSelected(bytes, name))
            } else {
                Toast.makeText(context, "Ошибка чтения файла", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // АВТО-СКРОЛЛ
    LaunchedEffect(state.initialScrollIndex, searchQuery, state.lecture) {
        val currentLecture = state.lecture
        if (currentLecture != null && (state.initialScrollIndex > 0)) {
            delay(600)
            scrollState.animateScrollTo(state.initialScrollIndex)
            Toast.makeText(context, "Возвращаемся к месту чтения...", Toast.LENGTH_SHORT).show()
        }
        // Логику подсветки мы упростили/убрали по твоему желанию, оставили только скролл
    }

    fun saveAndExit() {
        if (!state.isEditing) viewModel.setEvent(LectureContract.Event.OnSaveProgress(scrollState.value))
        viewModel.setEvent(LectureContract.Event.OnBackClicked)
    }

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
                // Открытие файла в браузере (скачивание)
                is LectureContract.Effect.OpenUrl -> {
                    try {
                        val fullUrl = "${BuildConfig.BASE_URL.trimEnd('/')}${effect.url}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Диалог удаления
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(LectureContract.Event.OnDismissDeleteDialog) },
            title = { Text("Удалить лекцию?") },
            text = { Text("Это действие нельзя отменить.") },
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
                        IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnSaveEditsClicked) }) {
                            Icon(Icons.Default.Check, contentDescription = "Сохранить", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        // --- ИНСТРУМЕНТЫ УЧИТЕЛЯ ---
                        if (state.isTeacher) {
                            // 1. Скрепка (Прикрепить файл)
                            IconButton(onClick = { fileLauncher.launch("*/*") }) {
                                Icon(Icons.Default.AttachFile, contentDescription = "Прикрепить файл")
                            }
                            // 2. Редактировать
                            IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnEditClicked) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                            }
                            // 3. Удалить
                            IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnDeleteClicked) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        // --- ИНСТРУМЕНТЫ ДЛЯ ВСЕХ ---
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
                if (state.isEditing) {
                    // РЕДАКТИРОВАНИЕ
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
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start)
                        )
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                } else {
                    // ПРОСМОТР
                    state.lecture?.let { lecture ->

                        // Простая подсветка (код `...`)
                        val contentToDisplay = remember(lecture.content, searchQuery) {
                            simpleHighlight(lecture.content, searchQuery)
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
                            Spacer(modifier = Modifier.height(24.dp))

                            // --- СПИСОК ФАЙЛОВ (ВИДЕН ВСЕМ) ---
                            if (lecture.files.isNotEmpty()) {
                                Text(
                                    text = "Материалы к лекции:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                lecture.files.forEach { file ->
                                    Card(
                                        onClick = { viewModel.setEvent(LectureContract.Event.OnFileClicked(file.url)) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(text = file.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            // ----------------------------------

                            if (lecture.hasTest && !state.isEditing) {
                                Button(
                                    onClick = { onNavigateToTest(lecture.id) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Пройти тест по лекции")
                                }
                                Spacer(modifier = Modifier.height(48.dp))
                            }
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

private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (e: Exception) { e.printStackTrace(); null }
}

private fun getFileName(context: Context, uri: Uri): String? {
    return uri.lastPathSegment?.substringAfterLast("/") ?: "File"
}