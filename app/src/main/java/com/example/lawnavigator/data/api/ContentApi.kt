package com.example.lawnavigator.data.api

import com.example.lawnavigator.data.dto.AdminTestResponseDto
import com.example.lawnavigator.data.dto.DisciplineDto
import com.example.lawnavigator.data.dto.LeaderboardItemDto
import com.example.lawnavigator.data.dto.LectureDto
import com.example.lawnavigator.data.dto.LectureProgressDto
import com.example.lawnavigator.data.dto.ProgressDto
import com.example.lawnavigator.data.dto.SaveTestRequestDto
import com.example.lawnavigator.data.dto.SaveTopicRequestDto
import com.example.lawnavigator.data.dto.SubmitAnswerRequest
import com.example.lawnavigator.data.dto.TestDto
import com.example.lawnavigator.data.dto.TestResultDto
import com.example.lawnavigator.data.dto.TopicDto
import com.example.lawnavigator.data.dto.UpdateLectureRequestDto
import com.example.lawnavigator.data.dto.UpdateProgressRequest
import com.example.lawnavigator.data.dto.UpdateTopicRequestDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ContentApi {
    @POST("api/admin/topics")
    suspend fun createTopic(
        @Header("Authorization") token: String,
        @Body request: SaveTopicRequestDto
    )

    @PUT("api/admin/topics/{id}")
    suspend fun updateTopic(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateTopicRequestDto
    )

    @DELETE("api/admin/topics/{id}")
    suspend fun deleteTopic(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )

    @GET("api/admin/tests/lecture/{id}")
    suspend fun getAdminTestByLecture(
        @Header("Authorization") token: String,
        @Path("id") lectureId: Int
    ): AdminTestResponseDto?

    @GET("api/lectures/{id}/test")
    suspend fun getTestByLecture(
        @Header("Authorization") token: String,
        @Path("id") lectureId: Int
    ): TestDto

    @GET("api/admin/tests/{topicId}")
    suspend fun getAdminTest(
        @Header("Authorization") token: String,
        @Path("topicId") topicId: Int
    ): AdminTestResponseDto? // Может вернуть null/204, Retrofit обработает

    @POST("api/admin/tests")
    suspend fun saveTest(
        @Header("Authorization") token: String,
        @Body request: SaveTestRequestDto
    )

    @DELETE("api/admin/lectures/{id}")
    suspend fun deleteLecture(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )

    @PUT("api/admin/lectures/{id}")
    suspend fun updateLecture(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateLectureRequestDto
    )

    @Multipart
    @POST("api/admin/upload/docx")
    suspend fun uploadDocx(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("topicId") topicId: RequestBody,
        @Part file: MultipartBody.Part
    )

    @GET("api/topics/{id}/lectures")
    suspend fun getLecturesByTopic(
        @Header("Authorization") token: String,
        @Path("id") topicId: Int
    ): List<LectureDto>

    // --- ЗАКЛАДКИ ---
    @GET("api/lectures/{id}/progress")
    suspend fun getLectureProgress(
        @Header("Authorization") token: String,
        @Path("id") lectureId: Int
    ): LectureProgressDto // Может вернуть 204 (null body), обработаем в репозитории

    @POST("api/lectures/{id}/progress")
    suspend fun saveLectureProgress(
        @Header("Authorization") token: String,
        @Path("id") lectureId: Int,
        @Body request: UpdateProgressRequest
    )

    @GET("api/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): List<LectureDto>

    @GET("api/search")
    suspend fun searchLectures(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): List<LectureDto>

    // Запрашиваем список дисциплин.
    // Токен будем передавать в заголовке Authorization
    @GET("api/disciplines")
    suspend fun getDisciplines(
        @Header("Authorization") token: String
    ): List<DisciplineDto>

    @GET("api/disciplines/{id}/topics")
    suspend fun getTopics(
        @Header("Authorization") token: String,
        @Path("id") disciplineId: Int
    ): List<TopicDto>

    // 1. Получить конкретную лекцию
    @GET("api/lectures/{id}")
    suspend fun getLecture(
        @Header("Authorization") token: String,
        @Path("id") lectureId: Int
    ): com.example.lawnavigator.data.dto.LectureDto

    // 2. Добавить в избранное
    @POST("api/favorites/{id}")
    suspend fun addToFavorites(
        @Header("Authorization") token: String,
        @Path("id") lectureId: Int
    )

    // 3. Удалить из избранного
    @DELETE("api/favorites/{id}")
    suspend fun removeFromFavorites(
        @Header("Authorization") token: String,
        @Path("id") lectureId: Int
    )

    @GET("api/topics/{id}/test")
    suspend fun getTest(
        @Header("Authorization") token: String,
        @Path("id") topicId: Int
    ): TestDto

    @POST("api/tests/{id}/submit")
    suspend fun submitTest(
        @Header("Authorization") token: String,
        @Path("id") testId: Int,
        @Body answers: List<SubmitAnswerRequest>
    ): TestResultDto

    @GET("api/analytics/leaderboard")
    suspend fun getLeaderboard(
        @Header("Authorization") token: String
    ): List<LeaderboardItemDto>

    /**
     * Получает общую статистику успеваемости.
     */
    @GET("api/analytics/progress")
    suspend fun getProgress(@Header("Authorization") token: String): ProgressDto

    /**
     * Получает список тем, рекомендованных к повторению (Smart Learning).
     */
    @GET("api/analytics/recommendations")
    suspend fun getRecommendations(@Header("Authorization") token: String): List<TopicDto>

}