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

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            // При регистрации с телефона мы пока не передаем inviteCode,
            // поэтому роль по умолчанию будет "student", которую вернет сервер.
            val request = AuthRequestDto(email, password)

            val response = api.register(request)

            // СОХРАНЯЕМ И ТОКЕН, И РОЛЬ (которую прислал сервер)
            tokenManager.saveAuthData(response.token, response.role)

            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val request = AuthRequestDto(email, password)
            val response = api.login(request)

            // СОХРАНЯЕМ И ТОКЕН, И РОЛЬ
            tokenManager.saveAuthData(response.token, response.role)

            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        }
    }
}