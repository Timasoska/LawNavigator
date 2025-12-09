package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TopicDto(
    val id: Int,
    val name: String,
    val disciplineId: Int
)