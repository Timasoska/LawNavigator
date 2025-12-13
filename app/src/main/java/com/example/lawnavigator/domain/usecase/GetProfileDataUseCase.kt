package com.example.lawnavigator.domain.usecase.profile
import com.example.lawnavigator.data.api.ContentApi
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.model.DisciplineStat
import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.model.UserAnalytics
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Сценарий получения всех данных для профиля.
 * Объединяет статистику и рекомендации.
 */
class GetProfileDataUseCase @Inject constructor(
    private val api: ContentApi,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<UserAnalytics> {
        return try {
            val token = tokenManager.token.first() ?: throw Exception("No token")
            val auth = "Bearer $token"

            // Делаем запросы к API
            val progressDto = api.getProgress(auth)
            val recsDto = api.getRecommendations(auth)

            val analytics = UserAnalytics(
                testsPassed = progressDto.testsPassed,
                averageScore = progressDto.averageScore,
                trend = progressDto.trend,

                history = progressDto.history, // <--- ДОБАВЬ ЭТУ СТРОКУ

                disciplines = progressDto.disciplines.map {
                    DisciplineStat(it.name, it.averageScore, it.trend)
                },
                recommendations = recsDto.map { Topic(it.id, it.name, it.disciplineId) }
            )
            Result.success(analytics)
        } catch (e: Exception) { // <--- ВОТ ЭТОГО НЕ ХВАТАЛО
            Result.failure(e)
        }
    }
}