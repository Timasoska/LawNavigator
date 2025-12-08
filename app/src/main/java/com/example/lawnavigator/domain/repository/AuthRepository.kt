package com.example.lawnavigator.domain.repository

/**
 * Интерфейс репозитория авторизации.
 * Определяет методы, которые должен реализовать слой данных.
 * Слой Domain не знает, как именно эти данные добываются (сеть или локальная БД).
 */
interface AuthRepository {

    /**
     * Выполняет регистрацию пользователя.
     * @return Result<Unit> - успешно или ошибка.
     */
    suspend fun register(email: String, password: String): Result<Unit>

    /**
     * Выполняет вход пользователя.
     * @return Result<Unit> - успешно или ошибка.
     */
    suspend fun login(email: String, password: String): Result<Unit>
}