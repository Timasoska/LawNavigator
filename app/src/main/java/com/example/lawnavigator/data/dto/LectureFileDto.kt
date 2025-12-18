package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LectureFileDto(
    val id: Int,
    val title: String,
    val url: String
)