package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SaveTopicRequestDto(
    val disciplineId: Int,
    val name: String
)

@Serializable
data class UpdateTopicRequestDto(
    val name: String
)