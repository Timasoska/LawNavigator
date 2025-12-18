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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturesListScreen(
    viewModel: LecturesListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLecture: (Int) -> Unit,
    onNavigateToTest: (Int) -> Unit,      // <--- Пройти тест
    onNavigateToCreateTest: (Int) -> Unit // <--- Создать тест
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
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
        floatingActionButton = {
            if (state.isTeacher) {
                FloatingActionButton(
                    onClick = {
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
        // Оборачиваем Box в CommonPullToRefreshBox
        CommonPullToRefreshBox(
            // Показывать спиннер, если грузимся, но НЕ если грузим файл (чтобы не путать)
            isRefreshing = state.isLoading && !state.isUploading,
            onRefresh = { viewModel.setEvent(LecturesListContract.Event.OnRefresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (state.isLoading && !state.isUploading) {
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
                                    viewModel.setEvent(
                                        LecturesListContract.Event.OnLectureClicked(
                                            lecture.id
                                        )
                                    )
                                },
                                // ВОТ ЗДЕСЬ КНОПКИ СБОКУ
                                trailingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        // --- ОТОБРАЖЕНИЕ БАЛЛОВ ---
                                        if (lecture.userScore != null) {
                                            // Выбираем цвет: Зеленый (>=60) или Желтый
                                            val scoreColor = if (lecture.userScore >= 60)
                                                Color(0xFF4CAF50) // Green
                                            else
                                                Color(0xFFFFC107) // Amber

                                            Text(
                                                text = "${lecture.userScore}%",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = scoreColor,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                        }

                                        // 1. УЧИТЕЛЬ: Гаечный ключ (Создать/Изменить тест)
                                        if (state.isTeacher) {
                                            IconButton(onClick = { onNavigateToCreateTest(lecture.id) }) {
                                                // Если тест уже есть - подсвечиваем primary цветом, иначе серым
                                                val tint =
                                                    if (lecture.hasTest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                Icon(
                                                    imageVector = Icons.Default.Build,
                                                    contentDescription = "Редактор теста",
                                                    tint = tint
                                                )
                                            }
                                        }

                                        // 2. КНОПКА PLAY (Видна всем, если тест существует)
                                        // Учитель тоже увидит её здесь и сможет пройти свой тест
                                        if (lecture.hasTest) {
                                            IconButton(onClick = { onNavigateToTest(lecture.id) }) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Пройти тест",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
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

private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    return uri.lastPathSegment?.substringAfterLast("/") ?: "Lecture.docx"
}