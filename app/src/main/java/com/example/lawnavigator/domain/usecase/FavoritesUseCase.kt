package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class FavoritesUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    // Получить все избранные
    suspend fun getAll(): Result<List<Lecture>> = repository.getFavorites()

    // Добавить
    suspend fun add(lectureId: Int): Result<Unit> = repository.addToFavorites(lectureId)

    // Удалить
    suspend fun remove(lectureId: Int): Result<Unit> = repository.removeFromFavorites(lectureId)
}