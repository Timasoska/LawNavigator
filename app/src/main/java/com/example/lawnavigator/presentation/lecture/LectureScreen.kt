package com.example.lawnavigator.presentation.lecture

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureScreen(
    viewModel: LectureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // 1. Состояние скролла
    val scrollState = rememberScrollState()

    // АНИМИРОВАННЫЙ АВТО-СКРОЛЛ
    LaunchedEffect(state.initialScrollIndex) {
        if (state.initialScrollIndex > 0) {
            // 1. Ждем, пока Markdown отрендерится и займет высоту
            // Без задержки скролл может не сработать, если контент еще не появился
            kotlinx.coroutines.delay(600)

            // 2. Показываем уведомление
            Toast.makeText(context, "Возвращаемся к месту чтения... 📖", Toast.LENGTH_SHORT).show()

            // 3. Плавная прокрутка
            scrollState.animateScrollTo(
                value = state.initialScrollIndex,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 500, // 0.5 секунды
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
        }
    }

    // 3. Функция сохранения и выхода
    fun saveAndExit() {
        // Отправляем текущую позицию во ViewModel
        viewModel.setEvent(LectureContract.Event.OnSaveProgress(scrollState.value))
        // Инициируем выход
        viewModel.setEvent(LectureContract.Event.OnBackClicked)
    }

    // Перехват системной кнопки "Назад" (на телефоне)
    BackHandler {
        saveAndExit()
    }

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LectureContract.Effect.NavigateBack -> onNavigateBack()
                is LectureContract.Effect.ShowMessage -> Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.lecture?.title ?: "Лекция", maxLines = 1) },
                navigationIcon = {
                    // Кнопка "Назад" в AppBar тоже сохраняет прогресс
                    IconButton(onClick = { saveAndExit() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEvent(LectureContract.Event.OnFavoriteClicked) }) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                state.lecture?.let { lecture ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(scrollState) // <--- Привязываем скролл
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = lecture.title, style = MaterialTheme.typography.headlineSmall)

                        Spacer(modifier = Modifier.height(16.dp))

                        MarkdownText(
                            markdown = lecture.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Добавляем отступ снизу, чтобы было удобно читать конец
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}