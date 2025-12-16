package com.example.lawnavigator.presentation.test_creator

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestCreatorScreen(
    viewModel: TestCreatorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TestCreatorContract.Effect.NavigateBack -> onNavigateBack()
                is TestCreatorContract.Effect.ShowMessage -> Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (state.isQuestionDialogOpen) {
        QuestionEditorDialog(
            initialQuestion = state.editingQuestion,
            onDismiss = { viewModel.setEvent(TestCreatorContract.Event.OnCloseDialog) },
            onSave = { question -> viewModel.setEvent(TestCreatorContract.Event.OnSaveQuestion(question)) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Конструктор теста") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(TestCreatorContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEvent(TestCreatorContract.Event.OnSaveTestClicked) }) {
                        Icon(Icons.Default.Check, contentDescription = "Сохранить тест")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setEvent(TestCreatorContract.Event.OnAddQuestionClicked) }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить вопрос")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                // Настройки теста
                OutlinedTextField(
                    value = state.testDraft.title,
                    onValueChange = { viewModel.setEvent(TestCreatorContract.Event.OnTitleChanged(it)) },
                    label = { Text("Название теста") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = if (state.testDraft.timeLimitMinutes == 0) "" else state.testDraft.timeLimitMinutes.toString(),
                    onValueChange = { viewModel.setEvent(TestCreatorContract.Event.OnTimeLimitChanged(it)) },
                    label = { Text("Таймер (минут, 0 = безлимит)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Вопросы (${state.testDraft.questions.size}):", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(state.testDraft.questions) { question ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setEvent(TestCreatorContract.Event.OnEditQuestionClicked(question)) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = question.text, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Сложность: ${question.difficulty} | Ответы: ${question.answers.size}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { viewModel.setEvent(TestCreatorContract.Event.OnDeleteQuestionClicked(question.id)) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}