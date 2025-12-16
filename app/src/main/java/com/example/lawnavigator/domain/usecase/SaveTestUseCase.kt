package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.TestDraft
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class SaveTestUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(testDraft: TestDraft): Result<Unit> {
        return repository.saveTest(testDraft)
    }
}