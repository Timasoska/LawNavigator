package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.AuthRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private val registerUseCase = RegisterUseCase(repository)

    @Test
    fun `invoke should return failure if password is too short`() = runBlocking {
        // GIVEN
        val email = "new@test.com"
        val shortPassword = "123" // Меньше 6 символов

        // WHEN
        val result = registerUseCase(email, shortPassword)

        // THEN
        assertTrue(result.isFailure)

        // ВАЖНО: Проверяем, что метод репозитория ВООБЩЕ НЕ ВЫЗЫВАЛСЯ.
        // Мы должны были отсечь ошибку до обращения к сети.
        coVerify(exactly = 0) { repository.register(any(), any()) }
    }
}