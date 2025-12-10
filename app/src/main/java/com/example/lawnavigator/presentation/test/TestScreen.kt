package com.example.lawnavigator.presentation.test

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
                    Text(
                        if (state.resultScore != null) "Результат"
                        // Показываем "Вопрос 1 из 5"
                        else "Вопрос ${state.currentQuestionIndex + 1} из ${state.test?.questions?.size ?: 0}"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(TestContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                // ЭКРАН РЕЗУЛЬТАТА
                ResultView(
                    score = state.resultScore!!,
                    message = state.resultMessage ?: "",
                    onBack = { viewModel.setEvent(TestContract.Event.OnBackClicked) }
                )
            }
            else {
                // ЭКРАН ВОПРОСА С АНИМАЦИЕЙ
                state.test?.let { test ->
                    val currentQuestion = test.questions.getOrNull(state.currentQuestionIndex)

                    if (currentQuestion != null) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {

                            // Прогресс бар
                            LinearProgressIndicator(
                                progress = { (state.currentQuestionIndex + 1) / test.questions.size.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Анимированная смена вопроса
                            AnimatedContent(
                                targetState = currentQuestion,
                                transitionSpec = {
                                    // Новый выезжает справа, старый уезжает влево
                                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> -width } + fadeOut()
                                },
                                label = "QuestionAnimation"
                            ) { question ->
                                QuestionCard(
                                    question = question,
                                    selectedAnswerId = state.selectedAnswers[question.id],
                                    onAnswerSelect = { id ->
                                        viewModel.setEvent(TestContract.Event.OnAnswerSelected(question.id, id))
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Кнопка "Далее" или "Завершить"
                            val isAnswerSelected = state.selectedAnswers.containsKey(currentQuestion.id)
                            val isLastQuestion = state.currentQuestionIndex == test.questions.size - 1

                            Button(
                                onClick = { viewModel.setEvent(TestContract.Event.OnNextClicked) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                enabled = isAnswerSelected // Кнопка активна только если выбран ответ
                            ) {
                                Text(if (isLastQuestion) "Завершить тест" else "Далее")
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
    selectedAnswerId: Int?,
    onAnswerSelect: (Int) -> Unit
) {
    Column {
        // Наш красивый бейдж сложности
        DifficultyBadge(level = question.difficulty)

        Text(text = question.text, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        question.answers.forEach { answer ->
            val isSelected = answer.id == selectedAnswerId
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable { onAnswerSelect(answer.id) },
                colors = CardDefaults.cardColors(containerColor = containerColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null // Клик обрабатывается карточкой
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = answer.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun ResultView(score: Int, message: String, onBack: () -> Unit) {
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
        Button(onClick = onBack) { Text("Вернуться к темам") }
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