package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetDisciplinesUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(): Result<List<Discipline>> {
        return repository.getDisciplines()
    }
}