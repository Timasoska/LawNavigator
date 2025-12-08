package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDto(
    val token: String
)