package com.example.lawnavigator.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll


/**
 * Обертка для реализации Pull-to-Refresh.
 * Использует нативный компонент Material3.
 *
 * @param isRefreshing - показывает ли сейчас спиннер
 * @param onRefresh - функция, которая вызывается, когда потянули вниз
 * @param content - контент (LazyColumn)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        content = content
    )
}