package com.example.lawnavigator.presentation.test

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
                                questionText = question.text,
                                answers = question.answers,
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
    questionText: String,
    answers: List<com.example.lawnavigator.domain.model.Answer>,
    selectedAnswerId: Int?,
    onSelect: (Int) -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = questionText, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            answers.forEach { answer ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = (answer.id == selectedAnswerId), onClick = { onSelect(answer.id) })
                    Text(text = answer.text, modifier = Modifier.padding(start = 8.dp))
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