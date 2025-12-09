package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.repository.ContentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetDisciplinesUseCaseTest {

    private val repository = mockk<ContentRepository>()
    private val useCase = GetDisciplinesUseCase(repository)

    @Test
    fun `invoke should return disciplines list on success`() = runBlocking {
        // GIVEN
        val mockData = listOf(
            Discipline(1, "Уголовное право", "Описание"),
            Discipline(2, "Гражданское право", "Описание")
        )
        coEvery { repository.getDisciplines() } returns Result.success(mockData)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(mockData, result.getOrNull())
        coVerify { repository.getDisciplines() }
    }

    @Test
    fun `invoke should return failure on repository error`() = runBlocking {
        // GIVEN
        val error = RuntimeException("Server Error")
        coEvery { repository.getDisciplines() } returns Result.failure(error)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}