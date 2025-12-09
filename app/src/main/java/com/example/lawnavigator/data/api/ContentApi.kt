package com.example.lawnavigator.data.api

import com.example.lawnavigator.data.dto.DisciplineDto
import com.example.lawnavigator.data.dto.TopicDto
import retrofit2.http.GET
import retrofit2.http.Header
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

}