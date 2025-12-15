package com.example.lawnavigator.presentation.lectures_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturesListScreen(
    viewModel: LecturesListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLecture: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Лаунчер для выбора файла
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Читаем байты из URI
            val bytes = readBytesFromUri(context, it)
            val name = getFileName(context, it) ?: "New Lecture"
            if (bytes != null) {
                viewModel.setEvent(LecturesListContract.Event.OnFileSelected(bytes, name))
            } else {
                Toast.makeText(context, "Ошибка чтения файла", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LecturesListContract.Effect.NavigateBack -> onNavigateBack()
                is LecturesListContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.lectureId)
                is LecturesListContract.Effect.ShowMessage -> Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Лекции") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(LecturesListContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        // КНОПКА ЗАГРУЗКИ (Только для учителя)
        floatingActionButton = {
            if (state.isTeacher) {
                FloatingActionButton(
                    onClick = {
                        // Запускаем выбор .docx файлов
                        launcher.launch("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    }
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Add, contentDescription = "Добавить лекцию")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading && !state.isUploading) { // Не показываем общий лоадер при загрузке файла
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(state.lectures) { lecture ->
                        ListItem(
                            headlineContent = { Text(lecture.title) },
                            leadingContent = {
                                Icon(Icons.Default.Description, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                viewModel.setEvent(LecturesListContract.Event.OnLectureClicked(lecture.id))
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// Вспомогательная функция для чтения байтов
private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Получение имени файла (упрощенно)
private fun getFileName(context: Context, uri: Uri): String? {
    // В реальном проекте тут нужно делать запрос к ContentResolver для DISPLAY_NAME
    // Но для диплома можно просто вернуть заглушку или путь
    return uri.lastPathSegment ?: "Lecture.docx"
}