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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            when {
                                state.isReviewMode -> "Просмотр ошибок"
                                state.resultScore != null -> "Результат"
                                else -> "Вопрос ${state.currentQuestionIndex + 1} из ${state.test?.questions?.size ?: 0}"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(TestContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // ТАЙМЕР СПРАВА
                actions = {
                    state.timeLeft?.let { seconds ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (seconds < 60) Color.Red else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTime(seconds),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (seconds < 60) Color.Red else MaterialTheme.colorScheme.onSurface
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
            }
            else if (state.resultScore != null) {
                ResultView(
                    score = state.resultScore!!,
                    message = state.resultMessage ?: "",
                    onBack = { viewModel.setEvent(TestContract.Event.OnBackClicked) },
                    onReview = { viewModel.setEvent(TestContract.Event.OnReviewClicked) }
                )
            }
            else {
                state.test?.let { test ->
                    val currentQuestion = test.questions.getOrNull(state.currentQuestionIndex)

                    if (currentQuestion != null) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {

                            LinearProgressIndicator(
                                progress = { (state.currentQuestionIndex + 1) / test.questions.size.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            AnimatedContent(
                                targetState = currentQuestion,
                                transitionSpec = {
                                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> -width } + fadeOut()
                                },
                                label = "QuestionAnimation"
                            ) { question ->
                                QuestionCard(
                                    question = question,
                                    selectedAnswerIds = state.selectedAnswers[question.id] ?: emptySet(),
                                    isReviewMode = state.isReviewMode,
                                    correctAnswerIds = state.correctAnswersMap[question.id] ?: emptyList(),
                                    onAnswerSelect = { id ->
                                        viewModel.setEvent(TestContract.Event.OnAnswerSelected(question.id, id))
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            val isAnswerSelected = !state.selectedAnswers[currentQuestion.id].isNullOrEmpty()
                            val isLastQuestion = state.currentQuestionIndex == test.questions.size - 1

                            Button(
                                onClick = { viewModel.setEvent(TestContract.Event.OnNextClicked) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                enabled = isAnswerSelected || state.isReviewMode
                            ) {
                                val text = if (state.isReviewMode) {
                                    if (isLastQuestion) "Завершить просмотр" else "Следующий вопрос"
                                } else {
                                    if (isLastQuestion) "Завершить тест" else "Далее"
                                }
                                Text(text)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    selectedAnswerIds: Set<Int>,
    isReviewMode: Boolean,
    correctAnswerIds: List<Int>,
    onAnswerSelect: (Int) -> Unit
) {
    Column {
        DifficultyBadge(level = question.difficulty)

        if (question.isMultipleChoice) {
            Text(
                text = "(Множественный выбор)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(text = question.text, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        question.answers.forEach { answer ->
            val isSelected = selectedAnswerIds.contains(answer.id)

            var borderColor = MaterialTheme.colorScheme.outlineVariant
            var containerColor = MaterialTheme.colorScheme.surface

            if (isReviewMode) {
                val isCorrect = correctAnswerIds.contains(answer.id)
                if (isCorrect) {
                    borderColor = Color(0xFF4CAF50)
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
                } else if (isSelected) {
                    borderColor = Color(0xFFF44336)
                    containerColor = Color(0xFFF44336).copy(alpha = 0.15f)
                }
            } else {
                if (isSelected) {
                    borderColor = MaterialTheme.colorScheme.primary
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(if (isSelected || (isReviewMode && correctAnswerIds.contains(answer.id))) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !isReviewMode) { onAnswerSelect(answer.id) },
                colors = CardDefaults.cardColors(containerColor = containerColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (question.isMultipleChoice) {
                        Checkbox(checked = isSelected, onCheckedChange = null)
                    } else {
                        RadioButton(selected = isSelected, onClick = null)
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = answer.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun ResultView(score: Int, message: String, onBack: () -> Unit, onReview: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$score%",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = if (score >= 60) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
        Text(text = "Ваш результат", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = onReview, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Посмотреть ошибки")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Вернуться к темам")
        }
    }
}

@Composable
fun DifficultyBadge(level: Int) {
    val (color, text) = when (level) {
        1 -> Color(0xFF4CAF50) to "Легкий"
        2 -> Color(0xFFFFC107) to "Средний"
        3 -> Color(0xFFF44336) to "Сложный"
        else -> Color.Gray to "Неизвестно"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Форматирование секунд в MM:SS
fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}