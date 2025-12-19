package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(private val repo: ContentRepository) {

    suspend fun createGroup(name: String, disciplineId: Int) = repo.createGroup(name, disciplineId)

}