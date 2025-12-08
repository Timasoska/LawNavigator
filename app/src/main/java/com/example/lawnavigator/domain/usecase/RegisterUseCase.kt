package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Сценарий регистрации нового пользователя.
 */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Запускает процесс регистрации.
     * Оператор invoke позволяет вызывать класс как функцию: registerUseCase(...)
     */
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        // Здесь можно добавить валидацию (например, длина пароля > 6) перед отправкой
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Пароль слишком короткий"))
        }
        return repository.register(email, password)
    }
}