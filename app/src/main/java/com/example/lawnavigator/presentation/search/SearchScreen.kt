package com.example.lawnavigator.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLecture: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchContract.Effect.NavigateBack -> onNavigateBack()
                is SearchContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.lectureId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск материалов") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(SearchContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Поле поиска
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.setEvent(SearchContract.Event.OnQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Например: преступление") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setEvent(SearchContract.Event.OnQueryChanged("")) }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true
            )

            // Индикатор загрузки
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Список результатов
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (state.results.isEmpty() && state.query.length > 2 && !state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Ничего не найдено", color = Color.Gray)
                        }
                    }
                }

                items(state.results) { lecture ->
                    ListItem(
                        headlineContent = { Text(lecture.title) },
                        supportingContent = {
                            // Показываем кусочек текста для контекста
                            Text(
                                text = lecture.content.take(60).replace("\n", " ") + "...",
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .clickable {
                                viewModel.setEvent(SearchContract.Event.OnLectureClicked(lecture.id))
                            }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}