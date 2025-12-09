package com.example.lawnavigator.domain.model

data class Lecture(
    val id: Int,
    val title: String,
    val content: String,
    val topicId: Int,
    val isFavorite: Boolean = false // Будем хранить состояние избранного в UI
)