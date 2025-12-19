package com.example.lawnavigator.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
/*

@Serializable
data class TopicDto(
    @SerialName("id") val id: Int, // Явно говорим: бери из поля "id"
    val name: String,
    val disciplineId: Int
)*/


@Serializable
data class TopicDto(
    val id: Int,
    val name: String,
    val disciplineId: Int,
    val progress: Int? = null // <--- Int?
)