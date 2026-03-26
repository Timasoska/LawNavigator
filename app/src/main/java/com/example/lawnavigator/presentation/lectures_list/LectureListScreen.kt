package com.example.lawnavigator.presentation.lectures_list

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lawnavigator.presentation.components.CommonPullToRefreshBox
import com.example.lawnavigator.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturesListScreen(
    viewModel: LecturesListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLecture: (Int) -> Unit,
    onNavigateToTest: (Int) -> Unit,
    onNavigateToCreateTest: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bytes = readBytesFromUri(context, it)
            val name = getFileName(context, it) ?: "New Lecture"
            if (bytes != null) viewModel.setEvent(LecturesListContract.Event.OnFileSelected(bytes, name))
            else Toast.makeText(context, "Ошибка чтения файла", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LecturesListContract.Effect.NavigateBack -> onNavigateBack()
                is LecturesListContract.Effect.NavigateToLecture -> onNavigateToLecture(effect.lectureId)
                is LecturesListContract.Effect.ShowMessage -> Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Материалы темы", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                navigationIcon = {
                    IconButton(onClick = { viewModel.setEvent(LecturesListContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isTeacher) {
                FloatingActionButton(
                    onClick = { launcher.launch("application/vnd.openxmlformats-officedocument.wordprocessingml.document") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (state.isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primaryContainer)
                    else Icon(Icons.Default.Add, "Добавить лекцию")
                }
            }
        }
    ) { padding ->
        CommonPullToRefreshBox(
            isRefreshing = state.isLoading && !state.isUploading,
            onRefresh = { viewModel.setEvent(LecturesListContract.Event.OnRefresh) },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && !state.isUploading && state.lectures.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.lectures) { lecture ->
                            StitchLectureCard(
                                lecture = lecture,
                                isTeacher = state.isTeacher,
                                onClick = { viewModel.setEvent(LecturesListContract.Event.OnLectureClicked(lecture.id)) },
                                onTestClick = { onNavigateToTest(lecture.id) },
                                onCreateTestClick = { onNavigateToCreateTest(lecture.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun StitchLectureCard(
    lecture: com.example.lawnavigator.domain.model.Lecture,
    isTeacher: Boolean,
    onClick: () -> Unit,
    onTestClick: () -> Unit,
    onCreateTestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onClick).padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceTint, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = lecture.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
        }

        if (lecture.hasTest || isTeacher || lecture.userScore != null) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Оценка
                if (lecture.userScore != null) {
                    val color = if (lecture.userScore >= 60) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiary
                    Text("Результат: ${lecture.userScore}%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("Тест не пройден", color = MaterialTheme.colorScheme.outlineVariant, fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTeacher) {
                        IconButton(onClick = onCreateTestClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Build, contentDescription = "Редактор", tint = if (lecture.hasTest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (lecture.hasTest) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onTestClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Пройти", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? = try { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } } catch (e: Exception) { null }
private fun getFileName(context: Context, uri: Uri): String? = uri.lastPathSegment?.substringAfterLast("/") ?: "Lecture.docx"