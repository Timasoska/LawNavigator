package com.example.lawnavigator.data.repository

import com.example.lawnavigator.data.api.ContentApi
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.repository.ContentRepository
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor(
    private val api: ContentApi,
    private val tokenManager: TokenManager
) : ContentRepository {

    override suspend fun getDisciplines(): Result<List<Discipline>> {
        return try {
            // 1. Достаем токен из DataStore (ждем первое значение)
            val token = tokenManager.token.first()
                ?: return Result.failure(Exception("Not authorized"))

            // 2. Делаем запрос с токеном ("Bearer ...")
            val dtos = api.getDisciplines("Bearer $token")

            // 3. Превращаем DTO в Domain Model
            val disciplines = dtos.map { dto ->
                Discipline(dto.id, dto.name, dto.description)
            }

            Result.success(disciplines)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        }
    }

    override suspend fun getTopics(disciplineId: Int): Result<List<Topic>> {
        return try {
            val token = tokenManager.token.first()
                ?: return Result.failure(Exception("Not authorized"))

            val dtos = api.getTopics("Bearer $token", disciplineId)

            val topics = dtos.map { dto ->
                Topic(dto.id, dto.name, dto.disciplineId)
            }
            Result.success(topics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}