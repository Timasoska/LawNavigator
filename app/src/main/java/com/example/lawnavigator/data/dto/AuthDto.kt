package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val name: String,
    val inviteCode: String? = null
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val role: String,
    val name: String
)
/*
@Serializable
data class AuthRequestDto(
    val email: String,
    val password: String,
    val name: String = "User", // Значение по умолчанию для Login (там имя не нужно)
    val inviteCode: String? = null // <--- Добавили код приглашения для регистрации
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val role: String, // <--- Добавили роль
    val name: String // <--- Добавили
)*/
