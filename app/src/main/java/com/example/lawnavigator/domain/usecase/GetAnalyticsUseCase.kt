package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject


class GetAnalyticsUseCase @Inject constructor(private val repo: ContentRepository) {

    suspend fun getAnalytics(groupId: Int) = repo.getGroupAnalytics(groupId)

}