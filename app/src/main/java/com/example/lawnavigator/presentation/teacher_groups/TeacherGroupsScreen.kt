package com.example.lawnavigator.presentation.teacher_groups

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Диалог создания
    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(TeacherGroupsContract.Event.OnDismissDialog) },
            title = { Text("Новая группа") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.newGroupName,
                        onValueChange = { viewModel.setEvent(TeacherGroupsContract.Event.OnGroupNameChanged(it)) },
                        label = { Text("Название группы") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- ВЫПАДАЮЩИЙ СПИСОК ---
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
            },
            confirmButton = {
                Button(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnConfirmCreateGroup) }) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(TeacherGroupsContract.Event.OnDismissDialog) }) {
                    Text("Отмена")
                }
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
                Icon(Icons.Default.Add, "Создать группу")
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
                                Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Предмет: ${group.disciplineName}")
                            Text("Студентов: ${group.studentCount}")
                            Spacer(modifier = Modifier.height(8.dp))

                            // Код приглашения (Крупно)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Код: ${group.inviteCode}",
                                    modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}