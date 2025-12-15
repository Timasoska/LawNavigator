package com.example.lawnavigator.data.repository

import com.example.lawnavigator.data.api.ContentApi
import com.example.lawnavigator.data.dto.SubmitAnswerRequest
import com.example.lawnavigator.data.dto.TestResultDto
import com.example.lawnavigator.data.dto.UpdateProgressRequest
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.model.Answer
import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.model.LeaderboardUser
import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.model.LectureProgress
import com.example.lawnavigator.domain.model.Question
import com.example.lawnavigator.domain.model.TestContent
import com.example.lawnavigator.domain.model.TestResult
import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.repository.ContentRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    override suspend fun getLeaderboard(): Result<List<LeaderboardUser>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dtos = api.getLeaderboard("Bearer $token")

            val users = dtos.mapIndexed { index, dto ->
                LeaderboardUser(
                    email = dto.email,
                    score = dto.score,
                    rank = index + 1 // Индекс начинается с 0, а место с 1
                )
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavorites(): Result<List<Lecture>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dtos = api.getFavorites("Bearer $token")
            val lectures = dtos.map {
                Lecture(it.id, it.title, it.content, it.topicId, it.isFavorite)
            }
            Result.success(lectures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addToFavorites(lectureId: Int): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            api.addToFavorites("Bearer $token", lectureId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromFavorites(lectureId: Int): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            api.removeFromFavorites("Bearer $token", lectureId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

            Result.success(Lecture(
                id = dto.id,
                title = dto.title,
                content = dto.content,
                topicId = dto.topicId,
                isFavorite = dto.isFavorite // <--- Теперь берем с сервера!
            ))
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
                timeLimit = dto.timeLimit, // <--- Маппинг времени
                questions = dto.questions.map { q ->
                    Question(
                        id = q.id,
                        text = q.text,
                        difficulty = q.difficulty,
                        isMultipleChoice = q.isMultipleChoice,
                        answers = q.answers.map { a -> Answer(a.id, a.text) }
                    )
                }
            )
            Result.success(domainTest)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun submitTest(testId: Int, answers: List<SubmitAnswerRequest>): Result<TestResultDto> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            // Просто передаем список в API
            val result = api.submitTest("Bearer $token", testId, answers)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLectureProgress(lectureId: Int): Result<LectureProgress> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))

            // Получаем DTO
            val dto = api.getLectureProgress("Bearer $token", lectureId)

            // Маппинг: DTO -> Domain
            val domainModel = LectureProgress(
                scrollIndex = dto.progressIndex,
                quote = dto.quote
            )
            Result.success(domainModel)
        } catch (e: Exception) {
            // Если ошибка (например, нет прогресса), возвращаем пустую модель (скролл 0)
            Result.success(LectureProgress(scrollIndex = 0))
        }
    }

    override suspend fun saveLectureProgress(lectureId: Int, progress: LectureProgress): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))

            // Маппинг: Domain -> Request DTO
            val request = UpdateProgressRequest(
                progressIndex = progress.scrollIndex,
                quote = progress.quote
            )

            api.saveLectureProgress("Bearer $token", lectureId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLecturesByTopic(topicId: Int): Result<List<Lecture>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dtos = api.getLecturesByTopic("Bearer $token", topicId)
            val lectures = dtos.map {
                Lecture(it.id, it.title, it.content, it.topicId, it.isFavorite)
            }
            Result.success(lectures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadDocx(topicId: Int, title: String, fileBytes: ByteArray): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))

            // 1. Готовим текстовые поля
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val topicIdBody = topicId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            // 2. Готовим файл
            val requestFile = fileBytes.toRequestBody(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaTypeOrNull()
            )
            val filePart = MultipartBody.Part.createFormData("file", "lecture.docx", requestFile)

            // 3. Отправляем
            api.uploadDocx("Bearer $token", titleBody, topicIdBody, filePart)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}