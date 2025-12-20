package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class AddXpUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    /**
     * @param amount Количество опыта
     * @param source Источник ("test", "lecture", "flashcards")
     */
    suspend operator fun invoke(amount: Int, source: String): Result<Unit> {
        return repository.addXp(amount, source)
    }
}