package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class UploadDocxUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(topicId: Int, title: String, bytes: ByteArray): Result<Unit> {
        return repository.uploadDocx(topicId, title, bytes)
    }
}