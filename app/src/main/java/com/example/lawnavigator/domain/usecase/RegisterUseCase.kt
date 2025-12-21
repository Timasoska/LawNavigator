package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Сценарий регистрации нового пользователя.
 */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String, inviteCode: String?): Result<Unit> {
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Пароль слишком короткий (минимум 6 символов)"))
        }
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Введите ваше имя"))
        }
        return repository.register(email, password, name, inviteCode)
    }
}