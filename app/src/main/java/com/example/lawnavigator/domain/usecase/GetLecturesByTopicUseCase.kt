package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetLecturesByTopicUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(topicId: Int): Result<List<Lecture>> {
        return repository.getLecturesByTopic(topicId)
    }
}