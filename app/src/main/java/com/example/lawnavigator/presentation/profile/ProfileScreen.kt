package com.example.lawnavigator.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox
import com.example.lawnavigator.presentation.components.MiniTrendIndicator
import com.example.lawnavigator.presentation.components.ScoreChart
import com.example.lawnavigator.presentation.components.ThemeOption
import com.example.lawnavigator.presentation.components.TrendIndicator
import com.example.lawnavigator.presentation.theme.ThemeMode
import com.example.lawnavigator.presentation.utils.calculateTrendLocal
import kotlinx.coroutines.flow.collectLatest
import android.widget.Toast
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToTopic: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var isSimulationMode by remember { mutableStateOf(false) }
    var simulatedScore by remember { mutableFloatStateOf(80f) }

    val realHistory = state.analytics?.history ?: emptyList()
    val displayHistory = remember(realHistory, isSimulationMode, simulatedScore) {
        if (isSimulationMode) realHistory + simulatedScore.toInt() else realHistory
    }
    val displayAvg = if (displayHistory.isNotEmpty()) displayHistory.average() else 0.0
    val displayTrend = calculateTrendLocal(displayHistory)
    val displayPassedTests = (state.analytics?.testsPassed ?: 0) + (if (isSimulationMode) 1 else 0)

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileContract.Effect.NavigateToLogin -> onNavigateToLogin()
                is ProfileContract.Effect.NavigateBack -> onNavigateBack()
                is ProfileContract.Effect.NavigateToTopic -> onNavigateToTopic(effect.topicId)
                is ProfileContract.Effect.ShowMessage -> Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- ДИАЛОГ СПИСКА УЧАСТНИКОВ (НОВОЕ) ---
    if (state.showMembersDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) },
            title = { Text("Участники группы") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(state.groupMembers) { email ->
                        ListItem(
                            headlineContent = { Text(email) },
                            leadingContent = { Icon(Icons.Default.Person, null) }
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Диалог вступления в группу
    if (state.showJoinGroupDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) },
            title = { Text("Вступить в группу") },
            text = {
                Column {
                    Text("Введите код приглашения:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.inviteCodeInput,
                        onValueChange = { viewModel.setEvent(ProfileContract.Event.OnInviteCodeChanged(it)) },
                        label = { Text("Код") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.setEvent(ProfileContract.Event.OnConfirmJoinGroup) }) { Text("Вступить") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnLogoutClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Выход")
                    }
                }
            )
        }
    ) { padding ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(ProfileContract.Event.OnRefresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Button(
                        onClick = { viewModel.setEvent(ProfileContract.Event.OnJoinGroupClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.GroupAdd, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Вступить в группу по коду")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    state.analytics?.let { analytics ->
                        if (analytics.groups.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Мои группы (нажми для списка):", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    analytics.groups.forEach { group ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.setEvent(ProfileContract.Event.OnGroupClicked(group.id)) }, // Теперь ID реальный
                                            color = Color.Transparent
                                        ) {
                                            Text(
                                                text = "• ${group.name}",
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSimulationMode) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(if (isSimulationMode) "Симулятор оценок" else "Ваш прогресс", style = MaterialTheme.typography.titleLarge)
                                Switch(checked = isSimulationMode, onCheckedChange = { isSimulationMode = it })
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Пройдено тестов: $displayPassedTests")
                                    Text("Средний балл: ${String.format("%.1f", displayAvg)}")
                                    val prediction = (displayAvg + displayTrend).coerceIn(0.0, 100.0)
                                    Text("Прогноз: ${String.format("%.1f", prediction)}", fontWeight = FontWeight.Bold)
                                }
                                TrendIndicator(trend = displayTrend)
                            }
                            if (isSimulationMode) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("След. оценка: ${simulatedScore.toInt()}")
                                Slider(value = simulatedScore, onValueChange = { simulatedScore = it }, valueRange = 0f..100f, steps = 19)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text("Оформление:", style = MaterialTheme.typography.titleMedium)
                    Card(modifier = Modifier.padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ThemeOption("Системная", state.themeMode == ThemeMode.SYSTEM) { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.SYSTEM)) }
                            ThemeOption("Светлая ☀️", state.themeMode == ThemeMode.LIGHT) { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.LIGHT)) }
                            ThemeOption("Темная 🌑", state.themeMode == ThemeMode.DARK) { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.DARK)) }
                        }
                    }
                }

                item {
                    Text("Динамика оценок:", style = MaterialTheme.typography.titleMedium)
                    if (displayHistory.isNotEmpty()) {
                        ScoreChart(scores = displayHistory, modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp))
                    }
                }

                val disciplines = state.analytics?.disciplines ?: emptyList()
                items(disciplines) { disc ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(disc.name, fontWeight = FontWeight.Bold)
                                LinearProgressIndicator(progress = { (disc.score / 100).toFloat() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                            }
                            MiniTrendIndicator(trend = disc.trend)
                        }
                    }
                }

                item { Text("Рекомендации:", modifier = Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleMedium) }
                val recs = state.analytics?.recommendations ?: emptyList()
                items(recs) { topic ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewModel.setEvent(ProfileContract.Event.OnRecommendationClicked(topic.id)) }) {
                        Text(topic.name, modifier = Modifier.padding(16.dp))
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}