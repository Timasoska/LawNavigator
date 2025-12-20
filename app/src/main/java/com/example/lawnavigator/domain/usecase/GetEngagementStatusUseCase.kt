package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.EngagementStatus
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetEngagementStatusUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(): Result<EngagementStatus> = repository.getEngagementStatus()
}