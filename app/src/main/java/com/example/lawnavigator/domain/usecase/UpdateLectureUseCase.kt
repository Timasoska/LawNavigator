package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class UpdateLectureUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(id: Int, title: String, content: String): Result<Unit> {
        return repository.updateLecture(id, title, content)
    }
}