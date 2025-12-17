package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DisciplineDto(
    val id: Int,
    val name: String,
    val description: String
)