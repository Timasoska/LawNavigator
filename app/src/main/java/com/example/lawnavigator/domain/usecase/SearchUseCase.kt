package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(query: String): Result<List<Lecture>> {
        if (query.isBlank()) return Result.success(emptyList())
        return repository.searchLectures(query)
    }
}