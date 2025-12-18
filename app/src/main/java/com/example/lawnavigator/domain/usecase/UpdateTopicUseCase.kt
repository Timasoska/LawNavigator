package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class UpdateTopicUseCase @Inject constructor(
    private val repository: ContentRepository
)
{
    suspend fun updateTopic(topicId: Int, name: String) = repository.updateTopic(topicId, name)
}