package com.example.lawnavigator.data.repository

import com.example.lawnavigator.data.api.AuthApi
import com.example.lawnavigator.data.dto.AuthRequestDto
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Реализация репозитория авторизации.
 * Отвечает за выполнение сетевых запросов и сохранение полученного токена.
 *
 * @param api Интерфейс Retrofit для общения с сервером.
 * @param tokenManager Класс для сохранения JWT токена в DataStore.
 */

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(AuthRequestDto(email, password))
            // Сохраняем token, role И name
            tokenManager.saveAuthData(response.token, response.role, response.name)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun register(email: String, password: String, name: String, inviteCode: String?): Result<Unit> {
        return try {
            val response = api.register(AuthRequestDto(email, password, name, inviteCode))
            tokenManager.saveAuthData(response.token, response.role, response.name)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}