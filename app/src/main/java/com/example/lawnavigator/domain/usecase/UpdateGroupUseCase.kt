package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class UpdateGroupUseCase @Inject constructor(private val repository: ContentRepository) {
    suspend operator fun invoke(groupId: Int, name: String) = repository.updateGroup(groupId, name)
}