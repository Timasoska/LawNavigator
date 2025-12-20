package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.TopicStat
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetDisciplineDetailsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(disciplineId: Int): Result<List<TopicStat>> {
        return repository.getDisciplineDetails(disciplineId)
    }
}