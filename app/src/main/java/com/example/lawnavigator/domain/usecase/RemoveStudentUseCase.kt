package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class RemoveStudentUseCase @Inject constructor(private val repository: ContentRepository) {
    suspend operator fun invoke(groupId: Int, studentId: Int) =
        repository.removeStudent(groupId, studentId) // <--- Убедись, что в ContentRepository метод называется именно так
}