package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тест для проверки бизнес-логики входа.
 */
class LoginUseCaseTest {

    // Создаем "фейковый" репозиторий
    private val repository = mockk<AuthRepository>()
    // Создаем тестируемый класс
    private val loginUseCase = LoginUseCase(repository)

    @Test
    fun `invoke should call repository login and return success`() = runBlocking {
        // GIVEN (Дано)
        val email = "test@test.com"
        val password = "password"
        // Настраиваем мок: когда вызовут login, верни успех
        coEvery { repository.login(email, password) } returns Result.success(Unit)

        // WHEN (Когда)
        val result = loginUseCase(email, password)

        // THEN (Тогда)
        assertTrue(result.isSuccess)
        // Проверяем, что метод репозитория действительно был вызван
        coVerify { repository.login(email, password) }
    }
}