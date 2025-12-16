package com.example.lawnavigator.presentation.test_creator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lawnavigator.domain.model.AnswerDraft
import com.example.lawnavigator.domain.model.QuestionDraft

@Composable
fun QuestionEditorDialog(
    initialQuestion: QuestionDraft?, // null если новый
    onDismiss: () -> Unit,
    onSave: (QuestionDraft) -> Unit
) {
    // Локальное состояние диалога
    var text by remember { mutableStateOf(initialQuestion?.text ?: "") }
    var difficulty by remember { mutableIntStateOf(initialQuestion?.difficulty ?: 1) }
    var isMultiple by remember { mutableStateOf(initialQuestion?.isMultipleChoice ?: false) }

    // Список ответов (используем mutableStateList для реактивности)
    val answers = remember {
        mutableStateListOf<AnswerDraft>().apply {
            addAll(initialQuestion?.answers ?: emptyList())
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Почти на весь экран
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (initialQuestion == null) "Новый вопрос" else "Редактирование вопроса",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Текст вопроса
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Текст вопроса") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Сложность
                Text("Сложность: $difficulty")
                Slider(
                    value = difficulty.toFloat(),
                    onValueChange = { difficulty = it.toInt() },
                    valueRange = 1f..3f,
                    steps = 1
                )

                // Множественный выбор
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMultiple, onCheckedChange = { isMultiple = it })
                    Text("Множественный выбор")
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // СПИСОК ОТВЕТОВ
                Text("Варианты ответов:", style = MaterialTheme.typography.titleMedium)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(answers) { answer ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = answer.isCorrect,
                                onCheckedChange = { isChecked ->
                                    val index = answers.indexOf(answer)
                                    answers[index] = answer.copy(isCorrect = isChecked)
                                }
                            )
                            OutlinedTextField(
                                value = answer.text,
                                onValueChange = { newText ->
                                    val index = answers.indexOf(answer)
                                    answers[index] = answer.copy(text = newText)
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Ответ") }
                            )
                            IconButton(onClick = { answers.remove(answer) }) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = { answers.add(AnswerDraft(text = "", isCorrect = false)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Добавить вариант")
                        }
                    }
                }

                // Кнопки Сохранить / Отмена
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (text.isNotBlank() && answers.isNotEmpty()) {
                            onSave(
                                QuestionDraft(
                                    id = initialQuestion?.id ?: java.util.UUID.randomUUID().toString(),
                                    text = text,
                                    difficulty = difficulty,
                                    isMultipleChoice = isMultiple,
                                    answers = answers.toList()
                                )
                            )
                        }
                    }) { Text("Сохранить") }
                }
            }
        }
    }
}