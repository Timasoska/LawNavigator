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

    @Test
    fun `invoke should return failure when repository returns error`() = runBlocking {
        // GIVEN
        val email = "test@test.com"
        val password = "wrong_password"
        val error = RuntimeException("Network Error")

        // Настраиваем мок: имитируем ошибку
        coEvery { repository.login(email, password) } returns Result.failure(error)

        // WHEN
        val result = loginUseCase(email, password)

        // THEN
        assertTrue(result.isFailure)
        // Можно проверить, что вернулась именно наша ошибка
        // assertEquals(error, result.exceptionOrNull())
    }

}