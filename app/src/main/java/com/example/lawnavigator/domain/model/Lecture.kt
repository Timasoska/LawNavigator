package com.example.lawnavigator.domain.model

data class Lecture(
    val id: Int,
    val title: String,
    val content: String,
    val topicId: Int,
    val isFavorite: Boolean = false,
    val hasTest: Boolean = false, // <--- НОВОЕ ПОЛЕ
    val files: List<LectureFile> = emptyList() // <--- Убедись, что это есть
)