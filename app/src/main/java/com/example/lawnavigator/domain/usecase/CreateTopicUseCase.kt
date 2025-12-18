package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class CreateTopicUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend fun createTopic(disciplineId: Int, name: String) =
        repository.createTopic(disciplineId, name)
}