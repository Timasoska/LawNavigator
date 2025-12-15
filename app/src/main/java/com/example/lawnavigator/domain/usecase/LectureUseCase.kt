package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.model.LectureProgress
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class LectureUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend fun getLecture(id: Int): Result<Lecture> = repository.getLecture(id)

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean): Result<Unit> =
        repository.toggleFavorite(id, isFavorite)

    // Возвращаем Domain Model
    suspend fun getProgress(id: Int): Result<LectureProgress> =
        repository.getLectureProgress(id)

    // Принимаем Domain Model (или параметры для его создания)
    // Для удобства UI можно оставить метод, принимающий Int, и внутри создавать модель
    suspend fun saveProgress(id: Int, index: Int, quote: String? = null): Result<Unit> {
        val progress = LectureProgress(scrollIndex = index, quote = quote)
        return repository.saveLectureProgress(id, progress)
    }
}