package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetTeacherGroupsUseCase @Inject constructor(private val repo: ContentRepository) {

    suspend fun getTeacherGroups() = repo.getTeacherGroups()

}