package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class DeleteLectureUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> = repository.deleteLecture(id)
}