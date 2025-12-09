package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetTopicsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(disciplineId: Int): Result<List<Topic>> {
        return repository.getTopics(disciplineId)
    }
}