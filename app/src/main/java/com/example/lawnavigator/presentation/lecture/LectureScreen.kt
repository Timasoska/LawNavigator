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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.lawnavigator.BuildConfig // Убедись, что импортируется BuildConfig твоего пакета
import com.example.lawnavigator.presentation.theme.*


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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Редактирование" else "", maxLines = 1) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                navigationIcon = {
                    IconButton(onClick = { if (state.isEditing) viewModel.setEvent(LectureContract.Event.OnCancelEditClicked) else saveAndExit() }) {
                        Icon(if (state.isEditing) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnSaveEditsClicked) }) {
                            Icon(Icons.Default.Check, contentDescription = "Сохранить", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        if (state.isTeacher) {
                            IconButton(onClick = { fileLauncher.launch("*/*") }) { Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnEditClicked) }) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnDeleteClicked) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.tertiary) }
                        }
                        IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnFavoriteClicked) }) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (state.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant // Красное сердце
                            )
                        }
                    }
                }
            )
        },
        // ПЛАВАЮЩАЯ КНОПКА ТЕСТА ВНИЗУ
        bottomBar = {
            if (!state.isLoading && !state.isEditing && state.lecture?.hasTest == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background)))
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = { onNavigateToTest(state.lecture!!.id) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Пройти тест по лекции", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else {
                state.lecture?.let { lecture ->
                    val contentToDisplay = remember(lecture.content, searchQuery) { simpleHighlight(lecture.content, searchQuery) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = lecture.title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 34.sp)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Отрисовка Markdown (нужно, чтобы библиотека поддерживала цвет)
                        MarkdownText(
                            markdown = contentToDisplay,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        // ФАЙЛЫ
                        if (lecture.files.isNotEmpty()) {
                            Text("Дополнительные материалы", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
                            lecture.files.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { viewModel.setEvent(LectureContract.Event.OnFileClicked(file.url)) }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(file.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.outlineVariant)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Отступ под нижнюю кнопку
                        Spacer(modifier = Modifier.height(120.dp))
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

private fun readBytesFromUri(context: android.content.Context, uri: android.net.Uri): ByteArray? {
    return try { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } } catch (e: Exception) { null }
}

private fun getFileName(context: android.content.Context, uri: android.net.Uri): String? {
    return uri.lastPathSegment?.substringAfterLast("/") ?: "File"
}