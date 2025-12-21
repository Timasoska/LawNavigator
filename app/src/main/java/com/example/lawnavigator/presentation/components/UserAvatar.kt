package com.example.lawnavigator.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun UserAvatar(
    name: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    // Кодируем имя для URL (пробелы -> %20 и т.д.)
    val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())

    // Используем стиль "initials" (красивые буквы на цветном фоне)
    // Можно поменять на "pixel-art", "lorelei" и т.д.
    val url = "https://api.dicebear.com/7.x/initials/svg?seed=$encodedName&backgroundColor=5c6bc0,ef5350,ffa726&textColor=ffffff"

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .decoderFactory(SvgDecoder.Factory()) // Важно для SVG!
            .crossfade(true)
            .build(),
        contentDescription = "Avatar",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}