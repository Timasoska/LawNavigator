package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.TestContent
import com.example.lawnavigator.domain.model.TestResult
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class TestUseCase @Inject constructor(private val repository: ContentRepository) {
    suspend fun loadTest(topicId: Int): Result<TestContent> = repository.getTest(topicId)
}