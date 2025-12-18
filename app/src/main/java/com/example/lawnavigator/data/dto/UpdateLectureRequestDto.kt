package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLectureRequestDto(
    val title: String,
    val content: String
)