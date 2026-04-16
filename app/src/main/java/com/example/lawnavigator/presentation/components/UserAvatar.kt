package com.example.lawnavigator.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun UserAvatar(
    name: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    // Кодируем имя для URL
    val encodedName = try {
        URLEncoder.encode(name.ifBlank { "User" }, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
        "User"
    }

    // ИСПОЛЬЗУЕМ PNG, чтобы всегда работало
    val url = "https://api.dicebear.com/7.x/initials/png?seed=$encodedName&backgroundColor=5c6bc0,ef5350,ffa726&textColor=ffffff"

    // Обернем в Box с заданным размером и фоном.
    // Если картинка не загрузится (нет интернета), будет виден серый круг с иконкой.
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Запасная иконка (видна, пока грузится картинка или если ошибка)
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(size * 0.6f) // Иконка чуть меньше кружка
        )

        // Основная картинка (наложится поверх иконки)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                // Убрали crossfade(true), так как он может баговать на мелких размерах
                .build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}