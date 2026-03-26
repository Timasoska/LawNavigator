package com.example.lawnavigator.presentation.flashcards

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Повторение") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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
                state.isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

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
                        Text("Все карточки на сегодня пройдены!", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Text("Вернуться", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                state.currentCard != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        AnimatedContent(
                            targetState = state.currentCard,
                            transitionSpec = {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            },
                            label = "CardTransition",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { targetCard ->
                            if (targetCard != null) {
                                FlipCard(
                                    card = targetCard,
                                    isFlipped = if (targetCard.id == state.currentCard?.id) state.isFlipped else false,
                                    onFlip = { viewModel.setEvent(FlashcardsContract.Event.OnCardFlip) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        AnimatedVisibility(
                            visible = state.isFlipped,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { viewModel.setEvent(FlashcardsContract.Event.OnRate(0)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f).padding(end = 8.dp).height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Забыл", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold) }

                                Button(
                                    onClick = { viewModel.setEvent(FlashcardsContract.Event.OnRate(3)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp).height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Норм", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) }

                                Button(
                                    onClick = { viewModel.setEvent(FlashcardsContract.Event.OnRate(5)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f).padding(start = 8.dp).height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Легко", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) }
                            }
                        }

                        if (!state.isFlipped) {
                            Text("Нажмите на карточку, чтобы проверить себя", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(48.dp))
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
                cameraDistance = 16f * density
            }
            .clickable(onClick = onFlip),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // FRONT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)))
                    .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.question,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 36.sp
                )
            }
        } else {
            // BACK
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = card.question,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    )

                    Text(
                        text = "Правильный ответ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val correctOptions = card.options.filter { it.isCorrect }

                    if (correctOptions.isNotEmpty()) {
                        correctOptions.forEach { option ->
                            Text(
                                text = option.text,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Text("Ответ не указан", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}