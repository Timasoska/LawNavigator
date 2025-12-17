package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.TestDraft
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class GetAdminTestUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    // Теперь принимаем два nullable аргумента
    suspend operator fun invoke(topicId: Int?, lectureId: Int?): Result<TestDraft?> {
        return if (topicId != null) {
            repository.getAdminTest(topicId)
        } else if (lectureId != null) {
            repository.getAdminTestByLecture(lectureId)
        } else {
            // Если ничего не передали - просто возвращаем "нет теста"
            Result.success(null)
        }
    }
}