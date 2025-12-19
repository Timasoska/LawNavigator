package com.example.lawnavigator.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class TopicDto(
    val id: Int,
    val name: String,
    val disciplineId: Int,
    val progress: Int? = null // <--- Int?
)