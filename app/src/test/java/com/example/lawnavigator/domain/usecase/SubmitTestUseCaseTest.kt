package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.data.dto.SubmitAnswerRequest
import com.example.lawnavigator.data.dto.TestResultDto
import com.example.lawnavigator.domain.repository.ContentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmitTestUseCaseTest {

    private lateinit var repository: ContentRepository
    private lateinit var submitTestUseCase: SubmitTestUseCase

    @Before
    fun setUp() {
        repository = mockk()
        submitTestUseCase = SubmitTestUseCase(repository)
    }

    @Test
    fun `test successful submission maps DTO to Domain model correctly`() = runTest {
        // УСТАНОВКА
        val userId = 1
        val testId = 10
        val rawAnswers = mapOf(
            1 to setOf(101), // Вопрос 1, выбран ответ 101
            2 to setOf(201, 202) // Вопрос 2 (множественный), выбраны 201 и 202
        )

        // То, что вернет сервер (DTO)
        val mockResponse = TestResultDto(
            score = 100,
            correctCount = 2,
            totalCount = 2,
            correctAnswers = mapOf(
                1 to listOf(101),
                2 to listOf(201, 202)
            )
        )

        // Ожидаем, что UseCase преобразует Map в List<SubmitAnswerRequest>
        val expectedRequests = listOf(
            SubmitAnswerRequest(1, 101),
            SubmitAnswerRequest(2, 201),
            SubmitAnswerRequest(2, 202)
        )

        coEvery { repository.submitTest(testId, expectedRequests) } returns Result.success(mockResponse)

        // ДЕЙСТВИЕ
        val result = submitTestUseCase(userId, testId, rawAnswers)

        // ПРОВЕРКА
        assertTrue(result.isSuccess)
        val domainModel = result.getOrNull()!!

        // Проверяем маппинг в Domain (TestResult)
        assertEquals(100, domainModel.score)
        assertEquals("Верно: 2 из 2", domainModel.message)
        assertEquals(listOf(101), domainModel.correctAnswers[1])

        // Проверяем, что репозиторий был вызван с правильными параметрами ровно 1 раз
        coVerify(exactly = 1) { repository.submitTest(testId, expectedRequests) }
    }

    @Test
    fun `test submission failure returns failure result`() = runTest {
        val testId = 10
        val rawAnswers = emptyMap<Int, Set<Int>>()

        val errorMessage = "Network Error"
        coEvery { repository.submitTest(testId, any()) } returns Result.failure(Exception(errorMessage))

        val result = submitTestUseCase(1, testId, rawAnswers)

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }
}