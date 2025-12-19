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

    // --- СОСТОЯНИЕ СИМУЛЯТОРА ---
    var isSimulationMode by remember { mutableStateOf(false) }
    var simulatedScore by remember { mutableFloatStateOf(80f) }

    // Подготовка данных
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

    // --- ДИАЛОГ ВСТУПЛЕНИЯ В ГРУППУ ---
    if (state.showJoinGroupDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) },
            title = { Text("Вступить в группу") },
            text = {
                Column {
                    Text("Введите код приглашения, который дал преподаватель:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.inviteCodeInput,
                        onValueChange = { viewModel.setEvent(ProfileContract.Event.OnInviteCodeChanged(it)) },
                        label = { Text("Код (например: A1B2C3)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.setEvent(ProfileContract.Event.OnConfirmJoinGroup) }) {
                    Text("Вступить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnLogoutClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Выход")
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

                // 0. КНОПКА ВСТУПЛЕНИЯ В ГРУППУ (НОВОЕ)
                item {
                    Button(
                        onClick = { viewModel.setEvent(ProfileContract.Event.OnJoinGroupClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Вступить в группу по коду")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // 0.5. СПИСОК МОИХ ГРУПП
                item {
                    // ИСПРАВЛЕНИЕ: Используем safe call (?.) и let
                    // Внутри блока переменная будет называться 'analytics' (или it) и она точно не null
                    state.analytics?.let { analytics ->
                        if (analytics.groups.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.GroupAdd, // Или Icons.Default.Group если есть
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Мои группы:",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Теперь обращаемся к локальной копии 'analytics'
                                    analytics.groups.forEach { groupName ->
                                        Text(
                                            text = "• $groupName",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // 1. КАРТОЧКА СТАТИСТИКИ
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSimulationMode)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (isSimulationMode) "Симулятор оценок" else "Ваш прогресс",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Switch(
                                    checked = isSimulationMode,
                                    onCheckedChange = { isSimulationMode = it }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Пройдено тестов: $displayPassedTests")
                                    Text("Средний балл: ${String.format("%.1f", displayAvg)}")

                                    val prediction = (displayAvg + displayTrend).coerceIn(0.0, 100.0)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Прогноз: ${String.format("%.1f", prediction)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSimulationMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                    )
                                }
                                TrendIndicator(trend = displayTrend)
                            }

                            if (isSimulationMode) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("След. оценка: ${simulatedScore.toInt()}", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = simulatedScore,
                                    onValueChange = { simulatedScore = it },
                                    valueRange = 0f..100f,
                                    steps = 19
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 2. ТЕМА ОФОРМЛЕНИЯ
                item {
                    Text("Оформление:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ThemeOption("Системная", state.themeMode == ThemeMode.SYSTEM) { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.SYSTEM)) }
                            ThemeOption("Светлая ☀️", state.themeMode == ThemeMode.LIGHT) { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.LIGHT)) }
                            ThemeOption("Темная 🌑", state.themeMode == ThemeMode.DARK) { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.DARK)) }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 3. ГРАФИК
                item {
                    Text("Динамика оценок:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (displayHistory.isNotEmpty()) {
                        ScoreChart(
                            scores = displayHistory,
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            graphColor = if (isSimulationMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text("Нет данных", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 4. УСПЕВАЕМОСТЬ ПО ПРЕДМЕТАМ
                item {
                    Text("По предметам:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val disciplines = state.analytics?.disciplines ?: emptyList()
                items(disciplines) { disc ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = disc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                val prediction = (disc.score + disc.trend).coerceIn(0.0, 100.0)
                                Text(text = "Балл: ${disc.score.toInt()}  •  Прогноз: ${prediction.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (disc.score / 100).toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                                    color = if (disc.score >= 60) Color(0xFF4CAF50) else Color(0xFFFFC107),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                MiniTrendIndicator(trend = disc.trend)
                            }
                        }
                    }
                }

                // 5. РЕКОМЕНДАЦИИ
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Рекомендации:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val recs = state.analytics?.recommendations ?: emptyList()
                if (recs.isEmpty()) {
                    item { Text("Нет рекомендаций 🎉", color = Color.Gray) }
                } else {
                    items(recs) { topic ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setEvent(ProfileContract.Event.OnRecommendationClicked(topic.id)) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = topic.name,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(50.dp)) }
            }
        }
    }
}