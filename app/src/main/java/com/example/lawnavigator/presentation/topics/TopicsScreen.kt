package com.example.lawnavigator.presentation.topics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    viewModel: TopicsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLecture: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TopicsContract.Effect.NavigateBack -> onNavigateBack()
                is TopicsContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.topicId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Темы") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(TopicsContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    items(state.topics) { topic ->
                        ListItem(
                            headlineContent = { Text(topic.name) },
                            modifier = Modifier.clickable {
                                viewModel.setEvent(TopicsContract.Event.OnTopicClicked(topic.id))
                            },
                            shadowElevation = 2.dp
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}