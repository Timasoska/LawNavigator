package com.example.lawnavigator.presentation.test

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.domain.model.Question
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lawnavigator.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    viewModel: TestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest {
            if (it is TestContract.Effect.NavigateBack) onNavigateBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            state.isReviewMode -> "Анализ ошибок"
                            state.resultScore != null -> "Результат"
                            else -> "Вопрос ${state.currentQuestionIndex + 1} из ${state.test?.questions?.size ?: 0}"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(TestContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    state.timeLeft?.let { seconds ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .background(if (seconds < 60) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (seconds < 60) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formatTime(seconds),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (seconds < 60) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else if (state.resultScore != null) {
                StitchResultView(
                    score = state.resultScore!!,
                    message = state.resultMessage ?: "",
                    onBack = { viewModel.setEvent(TestContract.Event.OnBackClicked) },
                    onReview = { viewModel.setEvent(TestContract.Event.OnReviewClicked) }
                )
            } else {
                state.test?.let { test ->
                    val currentQuestion = test.questions.getOrNull(state.currentQuestionIndex)

                    if (currentQuestion != null) {
                        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {

                            // Прогресс-бар
                            LinearProgressIndicator(
                                progress = { (state.currentQuestionIndex + 1) / test.questions.size.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceTint
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            AnimatedContent(
                                targetState = currentQuestion,
                                transitionSpec = {
                                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> -width } + fadeOut()
                                    )
                                },
                                label = "QuestionAnimation"
                            ) { question ->
                                StitchQuestionCard(
                                    question = question,
                                    selectedAnswerIds = state.selectedAnswers[question.id] ?: emptySet(),
                                    isReviewMode = state.isReviewMode,
                                    correctAnswerIds = state.correctAnswersMap[question.id] ?: emptyList(),
                                    onAnswerSelect = { id -> viewModel.setEvent(TestContract.Event.OnAnswerSelected(question.id, id)) }
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            val isAnswerSelected = !state.selectedAnswers[currentQuestion.id].isNullOrEmpty()
                            val isLastQuestion = state.currentQuestionIndex == test.questions.size - 1

                            Button(
                                onClick = { viewModel.setEvent(TestContract.Event.OnNextClicked) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer, // Белый/светлый текст
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                enabled = isAnswerSelected || state.isReviewMode
                            ) {
                                val text = if (state.isReviewMode) {
                                    if (isLastQuestion) "Завершить просмотр" else "Следующий вопрос"
                                } else {
                                    if (isLastQuestion) "Завершить тест" else "Далее"
                                }
                                Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StitchQuestionCard(
    question: Question,
    selectedAnswerIds: Set<Int>,
    isReviewMode: Boolean,
    correctAnswerIds: List<Int>,
    onAnswerSelect: (Int) -> Unit
) {
    Column {
        // Тег сложности
        val (difColor, difText) = when (question.difficulty) {
            1 -> Color(0xFF4CAF50) to "БАЗОВЫЙ"
            2 -> MaterialTheme.colorScheme.secondary to "ПРОДВИНУТЫЙ"
            3 -> MaterialTheme.colorScheme.tertiary to "СЛОЖНЫЙ"
            else -> MaterialTheme.colorScheme.outlineVariant to "НЕИЗВЕСТНО"
        }
        Text(text = difText, color = difColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.background(difColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = question.text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 34.sp)

        if (question.isMultipleChoice) {
            Text(text = "Выберите несколько вариантов", fontSize = 12.sp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        question.answers.forEach { answer ->
            val isSelected = selectedAnswerIds.contains(answer.id)

            var borderColor = MaterialTheme.colorScheme.surfaceTint
            var containerColor = MaterialTheme.colorScheme.surface
            var textColor = MaterialTheme.colorScheme.onSurfaceVariant

            if (isReviewMode) {
                val isCorrect = correctAnswerIds.contains(answer.id)
                if (isCorrect) {
                    borderColor = Color(0xFF4CAF50)
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    textColor = Color(0xFF4CAF50)
                } else if (isSelected) {
                    borderColor = MaterialTheme.colorScheme.tertiary
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    textColor = MaterialTheme.colorScheme.tertiary
                }
            } else {
                if (isSelected) {
                    borderColor = MaterialTheme.colorScheme.primary
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    textColor = MaterialTheme.colorScheme.primary
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor)
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !isReviewMode) { onAnswerSelect(answer.id) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Кастомный радиобаттон/чекбокс
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(2.dp, if (isSelected || (isReviewMode && correctAnswerIds.contains(answer.id))) borderColor else MaterialTheme.colorScheme.outlineVariant, if (question.isMultipleChoice) RoundedCornerShape(4.dp) else CircleShape)
                            .background(if (isSelected || (isReviewMode && correctAnswerIds.contains(answer.id))) borderColor else Color.Transparent, if (question.isMultipleChoice) RoundedCornerShape(4.dp) else CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected || (isReviewMode && correctAnswerIds.contains(answer.id))) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = answer.text, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor)
                }
            }
        }
    }
}

@Composable
fun StitchResultView(score: Int, message: String, onBack: () -> Unit, onReview: () -> Unit) {
    val isSuccess = score >= 60
    val color = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiary

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(160.dp).background(color.copy(alpha = 0.1f), CircleShape).border(4.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$score%", fontSize = 48.sp, fontWeight = FontWeight.Black, color = color)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = if (isSuccess) "Отличный результат!" else "Нужно повторить", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onReview,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            // ДОБАВИЛ цвет текста
            Text("Анализ ошибок", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            // ДОБАВИЛ цвет текста
            Text("Завершить", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}