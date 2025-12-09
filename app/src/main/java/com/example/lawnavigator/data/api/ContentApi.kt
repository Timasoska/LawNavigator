package com.example.lawnavigator.data.api

import com.example.lawnavigator.data.dto.DisciplineDto
import com.example.lawnavigator.data.dto.SubmitAnswerRequest
import com.example.lawnavigator.data.dto.TestDto
import com.example.lawnavigator.data.dto.TestResultDto
import com.example.lawnavigator.data.dto.TopicDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ContentApi {

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

}