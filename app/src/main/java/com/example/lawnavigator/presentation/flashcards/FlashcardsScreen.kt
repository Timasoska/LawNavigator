package com.example.lawnavigator.presentation.flashcards

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: FlashcardsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            if (effect is FlashcardsContract.Effect.NavigateBack) onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Повторение") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(FlashcardsContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()

                state.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${state.error}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.setEvent(FlashcardsContract.Event.OnRetry) }) {
                            Text("Повторить")
                        }
                    }
                }

                state.isFinished -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Все карточки на сегодня пройдены!", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Вернуться")
                        }
                    }
                }

                // Логика отображения карточек
                state.currentCard != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Прогресс бар
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // --- АНИМИРОВАННЫЙ КОНТЕЙНЕР ---
                        AnimatedContent(
                            targetState = state.currentCard,
                            transitionSpec = {
                                // Новая карточка выезжает справа (+FadeIn)
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    // Старая уезжает влево (+FadeOut)
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            },
                            label = "CardTransition",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { targetCard ->
                            // Важно: мы используем targetCard (аргумент лямбды), а не state.currentCard.
                            // Это позволяет AnimatedContent держать на экране "старую" карточку во время анимации.
                            if (targetCard != null) {
                                FlipCard(
                                    card = targetCard,
                                    // Если это ТЕКУЩАЯ карточка в стейте, берем стейт isFlipped.
                                    // Если это СТАРАЯ (улетающая) карточка, она должна оставаться перевернутой (true),
                                    // чтобы пользователь не видел, как она "захлопывается" в полете.
                                    // Но так как ViewModel сбрасывает isFlipped сразу, нам нужно небольшое условие.
                                    // Впрочем, для простоты: новая карточка всегда false, старая — визуально исчезает.
                                    isFlipped = if (targetCard.id == state.currentCard?.id) state.isFlipped else false,
                                    onFlip = { viewModel.setEvent(FlashcardsContract.Event.OnCardFlip) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // КНОПКИ ОЦЕНКИ (Показываем только если перевернуто)
                        // Анимируем появление кнопок (фейд)
                        AnimatedVisibility(
                            visible = state.isFlipped,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { viewModel.setEvent(FlashcardsContract.Event.OnRate(0)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                                ) { Text("Забыл") }

                                Button(
                                    onClick = { viewModel.setEvent(FlashcardsContract.Event.OnRate(3)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF176))
                                ) { Text("Норм", color = Color.Black) }

                                Button(
                                    onClick = { viewModel.setEvent(FlashcardsContract.Event.OnRate(5)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
                                ) { Text("Легко") }
                            }
                        }

                        if (!state.isFlipped) {
                            Text("Нажмите на карточку, чтобы проверить себя", color = Color.Gray)
                            Spacer(modifier = Modifier.height(48.dp)) // Место под кнопки, чтобы контент не прыгал
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlipCard(
    card: com.example.lawnavigator.domain.model.Flashcard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400), label = "flip"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(onClick = onFlip),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // FRONT
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = card.question,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            // BACK
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column {
                        Text(
                            text = card.question,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Правильный ответ:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val correctOptions = card.options.filter { it.isCorrect }

                        if (correctOptions.isNotEmpty()) {
                            correctOptions.forEach { option ->
                                Text(
                                    text = option.text,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            }
                        } else {
                            Text("Ответ не указан", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}