package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Сценарий входа пользователя в систему.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return repository.login(email, password)
    }
}