package com.example.lawnavigator.presentation.lectures_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturesListScreen(
    viewModel: LecturesListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLecture: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LecturesListContract.Effect.NavigateBack -> onNavigateBack()
                is LecturesListContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.lectureId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Лекции") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(LecturesListContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(state.lectures) { lecture ->
                        ListItem(
                            headlineContent = { Text(lecture.title) },
                            leadingContent = {
                                Icon(Icons.Default.Description, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                viewModel.setEvent(LecturesListContract.Event.OnLectureClicked(lecture.id))
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}