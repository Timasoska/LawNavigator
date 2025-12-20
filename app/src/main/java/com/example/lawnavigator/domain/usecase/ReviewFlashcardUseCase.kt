package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class ReviewFlashcardUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(questionId: Int, quality: Int): Result<Unit> =
        repository.reviewFlashcard(questionId, quality)
}