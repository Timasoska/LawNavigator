package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.TestDraft
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetAdminTestUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(topicId: Int): Result<TestDraft?> {
        return repository.getAdminTest(topicId)
    }
}