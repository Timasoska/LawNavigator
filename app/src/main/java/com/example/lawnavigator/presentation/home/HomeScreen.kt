package com.example.lawnavigator.presentation.home

import android.R.attr.clickable
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.domain.model.EngagementStatus
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox
import com.example.lawnavigator.presentation.components.EmptyScreen
import com.example.lawnavigator.presentation.components.ErrorScreen
import com.example.lawnavigator.presentation.components.LoadingScreen
import com.example.lawnavigator.presentation.components.UserAvatar
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTopics: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToTeacherGroups: () -> Unit,
    onNavigateToFlashcards: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeContract.Effect.NavigateToTopics -> onNavigateToTopics(effect.disciplineId)
                is HomeContract.Effect.NavigateToTeacherGroups -> onNavigateToTeacherGroups()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Дисциплины", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                actions = {
                    if (state.engagementStatus != null && state.engagementStatus!!.streak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp) // Уменьшили паддинги
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${state.engagementStatus!!.streak}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Убираем лишние отступы между иконками, делая их компактнее
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        IconButton(onClick = onNavigateToSearch) { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        IconButton(onClick = onNavigateToFlashcards) { Icon(Icons.Default.Style, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        IconButton(onClick = onNavigateToLeaderboard) { Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        IconButton(onClick = onNavigateToFavorites) { Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }

                        if (state.isTeacher) {
                            IconButton(onClick = { viewModel.setEvent(HomeContract.Event.OnTeacherGroupsClicked) }) {
                                Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Аватарка (сделаем чуть меньше)
                    Box(modifier = Modifier.clickable { onNavigateToProfile() }) {
                        UserAvatar(name = state.userName, size = 28.dp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }
    ) { paddingValues ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.setEvent(HomeContract.Event.OnRefresh) },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.disciplines.isEmpty() -> LoadingScreen()
                    state.error != null -> ErrorScreen(message = state.error ?: "Ошибка", onRetry = { viewModel.setEvent(HomeContract.Event.OnRetryClicked) })
                    else -> {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            if (state.engagementStatus != null) {
                                item { StitchDailyGoalCard(state.engagementStatus!!) }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ТЕКУЩАЯ ПРОГРАММА", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                                    // ДОБАВИЛИ ВЫПАДАЮЩЕЕ МЕНЮ
                                    Box {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = "Сортировка",
                                            tint = if (state.currentSort != SortType.NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { viewModel.setEvent(HomeContract.Event.OnFilterClick(true)) }
                                        )

                                        DropdownMenu(
                                            expanded = state.isFilterExpanded,
                                            onDismissRequest = { viewModel.setEvent(HomeContract.Event.OnFilterClick(false)) },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            SortType.values().forEach { sortType ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = sortType.title,
                                                            color = if (state.currentSort == sortType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                            fontWeight = if (state.currentSort == sortType) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = { viewModel.setEvent(HomeContract.Event.OnSortSelected(sortType)) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            items(state.disciplines) { discipline ->
                                StitchDisciplineCard(
                                    name = discipline.name,
                                    description = discipline.description,
                                    onClick = { viewModel.setEvent(HomeContract.Event.OnDisciplineClicked(discipline.id)) }
                                )
                            }
                            if (state.disciplines.isEmpty()) item { EmptyScreen(message = "Список дисциплин пуст") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StitchDailyGoalCard(status: EngagementStatus) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(6.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Ежедневная цель", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("Прогресс освоения", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, letterSpacing = 1.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(text = "${status.todayXp} / ${status.dailyGoalXp} XP", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(if (status.isDailyGoalReached) "Выполнено! 🎉" else "${(status.progress * 100).toInt()}%", color = if (status.isDailyGoalReached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { status.progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                color = if (status.isDailyGoalReached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun StitchDisciplineCard(name: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceTint, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
    }
}