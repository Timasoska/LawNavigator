package com.example.lawnavigator.domain.usecase

import com.example.lawnavigator.data.local.TokenManager
import javax.inject.Inject

/**
 * Сценарий выхода из системы.
 * Удаляет токен с устройства.
 */
class LogoutUseCase @Inject constructor(
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke() {
        tokenManager.deleteToken()
    }
}