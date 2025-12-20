package com.example.lawnavigator.presentation.teacher_groups

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGroupsScreen(
    viewModel: TeacherGroupsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAnalytics: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TeacherGroupsContract.Effect.NavigateBack -> onNavigateBack()
                is TeacherGroupsContract.Effect.NavigateToAnalytics -> onNavigateToAnalytics(effect.groupId)
                is TeacherGroupsContract.Effect.ShowMessage -> Toast.makeText(context, effect.msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- ДИАЛОГ СОЗДАНИЯ / РЕДАКТИРОВАНИЯ ---
    if (state.showGroupDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(TeacherGroupsContract.Event.OnDismissDialog) },
            title = { Text(if (state.isEditing) "Редактировать название" else "Новая группа") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.groupNameInput,
                        onValueChange = { viewModel.setEvent(TeacherGroupsContract.Event.OnGroupNameChanged(it)) },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!state.isEditing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ExposedDropdownMenuBox(
                            expanded = state.isDropdownExpanded,
                            onExpandedChange = { viewModel.setEvent(TeacherGroupsContract.Event.OnDropdownExpanded(it)) }
                        ) {
                            OutlinedTextField(
                                value = state.selectedDiscipline?.name ?: "Выберите предмет",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Предмет") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = state.isDropdownExpanded,
                                onDismissRequest = { viewModel.setEvent(TeacherGroupsContract.Event.OnDropdownExpanded(false)) }
                            ) {
                                state.availableDisciplines.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item.name) },
                                        onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnDisciplineSelected(item)) }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnConfirmSaveGroup) }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnDismissDialog) }) {
                    Text("Отмена")
                }
            }
        )
    }

    // --- ДИАЛОГ УДАЛЕНИЯ ---
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(TeacherGroupsContract.Event.OnDismissDialog) },
            title = { Text("Удалить группу?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnConfirmDeleteGroup) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnDismissDialog) }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои группы") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnCreateGroupClicked) }) {
                Icon(Icons.Default.Add, "Создать")
            }
        }
    ) { padding ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(TeacherGroupsContract.Event.OnRefresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(state.groups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.setEvent(TeacherGroupsContract.Event.OnGroupClicked(group.id)) },
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

                                // КНОПКА РЕДАКТИРОВАНИЯ
                                IconButton(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnEditGroupClicked(group)) }) {
                                    Icon(Icons.Default.Edit, "Edit", tint = Color.Gray)
                                }

                                // КНОПКА УДАЛЕНИЯ
                                IconButton(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnDeleteGroupClicked(group.id)) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Text("Предмет: ${group.disciplineName}")
                            Text("Студентов: ${group.studentCount}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                                Text("Код: ${group.inviteCode}", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}