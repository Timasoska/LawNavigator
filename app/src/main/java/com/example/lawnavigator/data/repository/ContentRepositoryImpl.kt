package com.example.lawnavigator.data.repository

import com.example.lawnavigator.data.api.ContentApi
import com.example.lawnavigator.data.dto.SubmitAnswerRequest
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.model.Answer
import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.model.Question
import com.example.lawnavigator.domain.model.TestContent
import com.example.lawnavigator.domain.model.TestResult
import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.repository.ContentRepository
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Репозиторий для работы с учебным контентом.
 * Отвечает за получение дисциплин, тем, лекций и тестов.
 * Использует [ContentApi] для сетевых запросов и [TokenManager] для авторизации.
 */

class ContentRepositoryImpl @Inject constructor(
    private val api: ContentApi,
    private val tokenManager: TokenManager
) : ContentRepository {

    override suspend fun searchLectures(query: String): Result<List<Lecture>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dtos = api.searchLectures("Bearer $token", query)
            val lectures = dtos.map { Lecture(it.id, it.title, it.content, it.topicId) }
            Result.success(lectures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    override suspend fun getLecture(lectureId: Int): Result<Lecture> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dto = api.getLecture("Bearer $token", lectureId)
            // Пока isFavorite = false, позже (в профиле) мы будем это проверять умнее
            Result.success(Lecture(dto.id, dto.title, dto.content, dto.topicId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(lectureId: Int, addToFavorites: Boolean): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val auth = "Bearer $token"
            if (addToFavorites) {
                api.addToFavorites(auth, lectureId)
            } else {
                api.removeFromFavorites(auth, lectureId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTest(topicId: Int): Result<TestContent> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dto = api.getTest("Bearer $token", topicId)

            val domainTest = TestContent(
                id = dto.id,
                title = dto.title,
                questions = dto.questions.map { q ->
                    Question(
                        q.id,
                        q.text,
                        difficulty = q.difficulty, // <--- Пробрасываем значение
                        q.answers.map { a -> Answer(a.id, a.text) })
                }
            )
            Result.success(domainTest)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun submitTest(testId: Int, answers: Map<Int, Int>): Result<TestResult> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))

            // Превращаем Map<QuestionId, AnswerId> в список для сервера
            val request = answers.map { (qId, aId) -> SubmitAnswerRequest(qId, aId) }

            val result = api.submitTest("Bearer $token", testId, request)

            Result.success(TestResult(
                score = result.score,
                message = "Верно: ${result.correctCount} из ${result.totalCount}"
            ))
        } catch (e: Exception) { Result.failure(e) }
    }
}