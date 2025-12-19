package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class DeleteGroupUseCase @Inject constructor(private val repository: ContentRepository) {
    suspend operator fun invoke(groupId: Int) = repository.deleteGroup(groupId)
}