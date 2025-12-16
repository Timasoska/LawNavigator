package com.example.lawnavigator.data.repository

import com.example.lawnavigator.data.api.ContentApi
import com.example.lawnavigator.data.dto.*
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.model.*
import com.example.lawnavigator.domain.repository.ContentRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor(
    private val api: ContentApi,
    private val tokenManager: TokenManager
) : ContentRepository {

    // --- АДМИНКА ТЕСТОВ (НОВОЕ) ---

    override suspend fun getAdminTest(topicId: Int): Result<TestDraft?> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))

            val dto = try {
                api.getAdminTest("Bearer $token", topicId)
            } catch (e: Exception) {
                null // 204 или 404
            }

            if (dto == null) {
                return Result.success(null)
            }

            // Маппинг DTO -> Domain Model
            val draft = TestDraft(
                topicId = dto.topicId, // Теперь это поле есть в DTO
                id = dto.id,
                title = dto.title,
                timeLimitMinutes = dto.timeLimit / 60,
                questions = dto.questions.map { q ->
                    QuestionDraft(
                        id = java.util.UUID.randomUUID().toString(), // Генерируем ID для UI
                        text = q.text,
                        difficulty = q.difficulty,
                        isMultipleChoice = q.isMultipleChoice,
                        answers = q.answers.map { a ->
                            AnswerDraft(
                                id = java.util.UUID.randomUUID().toString(),
                                text = a.text,
                                isCorrect = a.isCorrect
                            )
                        }
                    )
                }
            )
            Result.success(draft)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTest(testDraft: TestDraft): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))

            val request = SaveTestRequestDto(
                topicId = testDraft.topicId,
                title = testDraft.title,
                timeLimit = testDraft.timeLimitMinutes * 60,
                questions = testDraft.questions.map { qDraft ->
                    SaveQuestionRequestDto(
                        text = qDraft.text,
                        difficulty = qDraft.difficulty,
                        isMultipleChoice = qDraft.isMultipleChoice,
                        answers = qDraft.answers.map { aDraft ->
                            SaveAnswerRequestDto(
                                text = aDraft.text,
                                isCorrect = aDraft.isCorrect
                            )
                        }
                    )
                }
            )

            api.saveTest("Bearer $token", request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ОСТАЛЬНЫЕ МЕТОДЫ (Без изменений) ---

    override suspend fun getLeaderboard(): Result<List<LeaderboardUser>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dtos = api.getLeaderboard("Bearer $token")
            val users = dtos.mapIndexed { index, dto -> LeaderboardUser(dto.email, dto.score, index + 1) }
            Result.success(users)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getFavorites(): Result<List<Lecture>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dtos = api.getFavorites("Bearer $token")
            val lectures = dtos.map { Lecture(it.id, it.title, it.content, it.topicId, it.isFavorite) }
            Result.success(lectures)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun addToFavorites(lectureId: Int): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            api.addToFavorites("Bearer $token", lectureId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun removeFromFavorites(lectureId: Int): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            api.removeFromFavorites("Bearer $token", lectureId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun searchLectures(query: String): Result<List<Lecture>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dtos = api.searchLectures("Bearer $token", query)
            val lectures = dtos.map { Lecture(it.id, it.title, it.content, it.topicId) }
            Result.success(lectures)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getDisciplines(): Result<List<Discipline>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("Not authorized"))
            val dtos = api.getDisciplines("Bearer $token")
            val disciplines = dtos.map { Discipline(it.id, it.name, it.description) }
            Result.success(disciplines)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getTopics(disciplineId: Int): Result<List<Topic>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("Not authorized"))
            val dtos = api.getTopics("Bearer $token", disciplineId)
            val topics = dtos.map { Topic(it.id, it.name, it.disciplineId) }
            Result.success(topics)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getLecture(lectureId: Int): Result<Lecture> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dto = api.getLecture("Bearer $token", lectureId)
            Result.success(Lecture(dto.id, dto.title, dto.content, dto.topicId, dto.isFavorite))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun toggleFavorite(lectureId: Int, addToFavorites: Boolean): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val auth = "Bearer $token"
            if (addToFavorites) api.addToFavorites(auth, lectureId) else api.removeFromFavorites(auth, lectureId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getTest(topicId: Int): Result<TestContent> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dto = api.getTest("Bearer $token", topicId)
            val domainTest = TestContent(
                id = dto.id,
                title = dto.title,
                timeLimit = dto.timeLimit,
                questions = dto.questions.map { q ->
                    Question(q.id, q.text, q.difficulty, q.isMultipleChoice, q.answers.map { a -> Answer(a.id, a.text) })
                }
            )
            Result.success(domainTest)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun submitTest(testId: Int, answers: List<SubmitAnswerRequest>): Result<TestResultDto> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val result = api.submitTest("Bearer $token", testId, answers)
            Result.success(result)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getLectureProgress(lectureId: Int): Result<LectureProgress> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dto = api.getLectureProgress("Bearer $token", lectureId)
            val domainModel = LectureProgress(dto.progressIndex, dto.quote)
            Result.success(domainModel)
        } catch (e: Exception) {
            Result.success(LectureProgress(scrollIndex = 0))
        }
    }

    override suspend fun saveLectureProgress(lectureId: Int, progress: LectureProgress): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val request = UpdateProgressRequest(progress.scrollIndex, progress.quote)
            api.saveLectureProgress("Bearer $token", lectureId, request)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getLecturesByTopic(topicId: Int): Result<List<Lecture>> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val dtos = api.getLecturesByTopic("Bearer $token", topicId)
            val lectures = dtos.map { Lecture(it.id, it.title, it.content, it.topicId, it.isFavorite) }
            Result.success(lectures)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun uploadDocx(topicId: Int, title: String, fileBytes: ByteArray): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val topicIdBody = topicId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val requestFile = fileBytes.toRequestBody("application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", "lecture.docx", requestFile)
            api.uploadDocx("Bearer $token", titleBody, topicIdBody, filePart)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteLecture(id: Int): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            api.deleteLecture("Bearer $token", id)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateLecture(id: Int, title: String, content: String): Result<Unit> {
        return try {
            val token = tokenManager.token.first() ?: return Result.failure(Exception("No token"))
            val request = UpdateLectureRequestDto(title, content)
            api.updateLecture("Bearer $token", id, request)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}