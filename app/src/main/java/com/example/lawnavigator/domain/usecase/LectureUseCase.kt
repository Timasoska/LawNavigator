package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class LectureUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend fun getLecture(id: Int): Result<Lecture> = repository.getLecture(id)

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean): Result<Unit> =
        repository.toggleFavorite(id, isFavorite)
}