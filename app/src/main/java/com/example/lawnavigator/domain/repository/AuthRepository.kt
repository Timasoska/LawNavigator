package com.example.lawnavigator.domain.repository

/**
 * Интерфейс репозитория авторизации.
 * Определяет методы, которые должен реализовать слой данных.
 * Слой Domain не знает, как именно эти данные добываются (сеть или локальная БД).
 */
interface AuthRepository {

    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, name: String, inviteCode: String?): Result<Unit>
}