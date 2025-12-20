package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.StudentDetailedReport
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetStudentReportUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(groupId: Int, studentId: Int): Result<StudentDetailedReport> {
        return repository.getStudentReport(groupId, studentId)
    }
}