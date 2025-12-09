package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LectureDto(
    val id: Int,
    val title: String,
    val content: String,
    val topicId: Int
)