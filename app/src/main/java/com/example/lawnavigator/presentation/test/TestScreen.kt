package com.example.lawnavigator.presentation.test

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                title = { Text("Тестирование") },
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
            } else if (state.resultScore != null) {
                // ЭКРАН РЕЗУЛЬТАТА
                ResultView(score = state.resultScore!!, message = state.resultMessage ?: "", onBack = { viewModel.setEvent(TestContract.Event.OnBackClicked) })
            } else {
                // ЭКРАН ВОПРОСОВ
                state.test?.let { test ->
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        item {
                            Text(test.title, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        items(test.questions) { question ->
                            QuestionItem(
                                question = question, // <--- Передаем объект целиком
                                selectedAnswerId = state.selectedAnswers[question.id],
                                onSelect = { answerId -> viewModel.setEvent(TestContract.Event.OnAnswerSelected(question.id, answerId)) }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        item {
                            Button(
                                onClick = { viewModel.setEvent(TestContract.Event.OnSubmitClicked) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = state.selectedAnswers.size == test.questions.size
                            ) {
                                Text("Завершить тест")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionItem(
    question: Question, // <--- Только один параметр с данными
    selectedAnswerId: Int?,
    onSelect: (Int) -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Показываем сложность
            DifficultyBadge(level = question.difficulty)

            // Текст вопроса
            Text(text = question.text, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Ответы берем из самого вопроса
            question.answers.forEach { answer ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = (answer.id == selectedAnswerId), onClick = { onSelect(answer.id) })
                    Text(
                        text = answer.text,
                        modifier = Modifier.padding(start = 8.dp).clickable { onSelect(answer.id) }
                    )
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
        Text(text = "Ваш результат: $score%", style = MaterialTheme.typography.displayMedium, color = if (score >= 60) Color.Green else Color.Red)
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) { Text("Вернуться") }
    }
}

@Composable
fun DifficultyBadge(level: Int) {
    val (color, text) = when (level) {
        1 -> Color(0xFF4CAF50) to "Легкий"   // Зеленый
        2 -> Color(0xFFFFC107) to "Средний"  // Желтый
        3 -> Color(0xFFF44336) to "Сложный"  // Красный
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