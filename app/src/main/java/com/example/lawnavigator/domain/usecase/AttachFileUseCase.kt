package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class AttachFileUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(lectureId: Int, bytes: ByteArray, name: String): Result<Unit> {
        return repository.attachFile(lectureId, bytes, name)
    }
}