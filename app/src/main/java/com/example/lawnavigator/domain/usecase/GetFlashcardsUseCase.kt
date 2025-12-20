package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Flashcard
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetFlashcardsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(): Result<List<Flashcard>> = repository.getDueFlashcards()
}