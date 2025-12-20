package com.example.lawnavigator.domain.usecase.profile
import com.example.lawnavigator.data.api.ContentApi
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.model.DisciplineStat
import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.model.UserAnalytics
import com.example.lawnavigator.domain.model.UserGroup
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Сценарий получения всех данных для профиля.
 * Объединяет общую статистику по дисциплинам и рекомендации.
 */
class GetProfileDataUseCase @Inject constructor(
    private val api: ContentApi,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<UserAnalytics> {
        return try {
            val token = tokenManager.token.first() ?: throw Exception("No token")
            val auth = "Bearer $token"

            // 1. Загружаем данные с бэкенда
            val progressDto = api.getProgress(auth)
            val recsDto = api.getRecommendations(auth)

            // 2. Преобразуем DTO в доменную модель (Маппинг)
            val analytics = UserAnalytics(
                testsPassed = progressDto.testsPassed,
                averageScore = progressDto.averageScore,
                trend = progressDto.trend,
                history = progressDto.history,

                // Маппинг групп
                groups = progressDto.groups.map { UserGroup(it.id, it.name) },

                // ИСПРАВЛЕННЫЙ МАППИНГ ДИСЦИПЛИН:
                disciplines = progressDto.disciplines.map {
                    DisciplineStat(
                        id = it.id,              // Берем Int ID
                        name = it.name,          // Берем String Name
                        score = it.averageScore, // Берем Double Score
                        trend = it.trend         // Берем Double Trend
                    )
                },

                // Маппинг рекомендаций
                recommendations = recsDto.map { Topic(it.id, it.name, it.disciplineId) }
            )

            Result.success(analytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}