package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class DeleteTopicUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend fun deleteTopic(topicId: Int) = repository.deleteTopic(topicId)
}