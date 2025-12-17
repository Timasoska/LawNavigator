package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.TestContent
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetTestByLectureUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(lectureId: Int): Result<TestContent> {
        return repository.getTestByLectureId(lectureId)
    }
}