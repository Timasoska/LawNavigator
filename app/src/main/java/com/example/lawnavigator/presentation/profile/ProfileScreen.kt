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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.lawnavigator.presentation.components.UserAvatar
import com.example.lawnavigator.presentation.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToTopic: (Int) -> Unit,
    onNavigateToDisciplineDetails: (Int, String) -> Unit
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

    if (state.showMembersDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) },
            title = { Text("Участники группы", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(state.groupMembers) { email ->
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(email, color = MaterialTheme.colorScheme.onSurface) },
                            leadingContent = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) }) {
                    Text("Закрыть", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (state.showJoinGroupDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) },
            title = { Text("Вступить в группу", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text("Введите код приглашения:", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Button(onClick = { viewModel.setEvent(ProfileContract.Event.OnConfirmJoinGroup) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Text("Вступить", color = MaterialTheme.colorScheme.onPrimaryContainer) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnDismissDialog) }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Профиль", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEvent(ProfileContract.Event.OnLogoutClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Выход", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. ПРОФИЛЬ И СТРИК
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            // АВАТАРКА (используем имя из состояния)
                            UserAvatar(name = state.userName, size = 64.dp)

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                // ИМЯ (Крупно)
                                Text(
                                    text = state.userName,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // РОЛЬ (Динамически из состояния)
                                val roleText = if (state.userRole == "teacher") "Преподаватель" else "Студент"
                                val roleColor = if (state.userRole == "teacher") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

                                Text(
                                    text = roleText,
                                    color = roleColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // 2. ВСТУПИТЬ В ГРУППУ
                item {
                    Button(
                        onClick = { viewModel.setEvent(ProfileContract.Event.OnJoinGroupClicked) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.Default.GroupAdd, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Вступить в группу по коду", fontWeight = FontWeight.Bold)
                    }
                }

                // 3. МОИ ГРУППЫ
                item {
                    state.analytics?.let { analytics ->
                        if (analytics.groups.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(20.dp)
                            ) {
                                Text("МОИ ГРУППЫ", color = MaterialTheme.colorScheme.outlineVariant, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                                analytics.groups.forEach { group ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { viewModel.setEvent(ProfileContract.Event.OnGroupClicked(group.id)) }.padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = group.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. СТАТИСТИКА И СИМУЛЯТОР
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (isSimulationMode) MaterialTheme.colorScheme.tertiaryContainer.copy(0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(0.1f)).border(1.dp, if (isSimulationMode) MaterialTheme.colorScheme.tertiary.copy(0.3f) else MaterialTheme.colorScheme.primary.copy(0.3f), RoundedCornerShape(16.dp)).padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(if (isSimulationMode) "СИМУЛЯТОР" else "ПРОГРЕСС", color = if (isSimulationMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Switch(checked = isSimulationMode, onCheckedChange = { isSimulationMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.background, checkedTrackColor = MaterialTheme.colorScheme.tertiary))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Средний балл", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                Text("${String.format("%.1f", displayAvg)}%", fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Пройдено тестов: $displayPassedTests", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)

                                // --- ВОЗВРАЩЕННЫЙ БЛОК ПРОГНОЗА ---
                                val prediction = (displayAvg + displayTrend).coerceIn(0.0, 100.0)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.QueryStats, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Прогноз: ${String.format("%.1f", prediction)}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSimulationMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                    )
                                }
                                // ----------------------------------
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Тренд (МНК)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                                MiniTrendIndicator(trend = displayTrend)
                            }
                        }
                        if (isSimulationMode) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Добавить оценку: ${simulatedScore.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Slider(value = simulatedScore, onValueChange = { simulatedScore = it }, valueRange = 0f..100f, steps = 19, colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.tertiary, activeTrackColor = MaterialTheme.colorScheme.tertiary))
                        }
                    }
                }

                // ВЫБОР ТЕМЫ
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(20.dp)
                    ) {
                        Text("ОФОРМЛЕНИЕ", color = MaterialTheme.colorScheme.outlineVariant, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeButton("Авто", Icons.Default.SettingsBrightness, state.themeMode == ThemeMode.SYSTEM, { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.SYSTEM)) }, Modifier.weight(1f))
                            ThemeButton("Светлая", Icons.Default.LightMode, state.themeMode == ThemeMode.LIGHT, { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.LIGHT)) }, Modifier.weight(1f))
                            ThemeButton("Темная", Icons.Default.DarkMode, state.themeMode == ThemeMode.DARK, { viewModel.setEvent(ProfileContract.Event.OnThemeChanged(ThemeMode.DARK)) }, Modifier.weight(1f))
                        }
                    }
                }

                item {
                    if (displayHistory.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ДИНАМИКА",
                                color = MaterialTheme.colorScheme.outlineVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            // --- ЛЕГЕНДА ГРАФИКА ---
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Реальность", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Симулятор", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            }
                        }

                        ScoreChart(
                            scores = displayHistory,
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            graphColor = if (isSimulationMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 6. ПРЕДМЕТЫ
                val disciplines = state.analytics?.disciplines ?: emptyList()
                if (disciplines.isNotEmpty()) {
                    item { Text("ПО ДИСЦИПЛИНАМ", color = MaterialTheme.colorScheme.outlineVariant, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 0.dp, top = 8.dp)) }
                    items(disciplines) { disc ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable { onNavigateToDisciplineDetails(disc.id, disc.name) }.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = disc.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { (disc.score / 100).toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = if (disc.score >= 60) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiary,
                                    trackColor = MaterialTheme.colorScheme.surfaceTint
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            MiniTrendIndicator(trend = disc.trend)
                        }
                    }
                }

                // --- ВОЗВРАЩЕННЫЙ БЛОК РЕКОМЕНДАЦИЙ (ЗАПАДАЮЩИЕ ТЕМЫ) ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "РЕКОМЕНДУЕТСЯ ПОВТОРИТЬ",
                        color = MaterialTheme.colorScheme.outlineVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                val recs = state.analytics?.recommendations ?: emptyList()
                if (recs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("У вас нет западающих тем! 🎉", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(recs) { topic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f))
                                .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .clickable { viewModel.setEvent(ProfileContract.Event.OnRecommendationClicked(topic.id)) }
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PriorityHigh, contentDescription = "Внимание", tint = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topic.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Низкий результат или резкий спад",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
                // --------------------------------------------------------

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun ThemeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceTint
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}