package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.data.dto.SubmitAnswerRequest
import com.example.lawnavigator.domain.model.TestResult
import com.example.lawnavigator.domain.repository.ContentRepository
import javax.inject.Inject

class SubmitTestUseCase @Inject constructor(private val repository: ContentRepository) {

    suspend operator fun invoke(userId: Int, testId: Int, answers: Map<Int, Set<Int>>): Result<TestResult> {

        // Превращаем Map<QuestionId, Set<AnswerId>> в List<SubmitAnswerRequest>
        val requestList = answers.flatMap { (qId, aIds) ->
            aIds.map { aId -> SubmitAnswerRequest(qId, aId) }
        }

        // Вызываем репозиторий и маппим результат DTO -> Domain
        return repository.submitTest(testId, requestList).map { dto ->
            TestResult(
                score = dto.score,
                message = "Верно: ${dto.correctCount} из ${dto.totalCount}",
                correctAnswers = dto.correctAnswers
            )
        }
    }
}