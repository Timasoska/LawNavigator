package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class JoinGroupUseCase @Inject constructor(private val repo: ContentRepository) {
    suspend fun joinGroup(code: String) = repo.joinGroup(code)
}