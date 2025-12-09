package com.example.lawnavigator.data.api

import com.example.lawnavigator.data.dto.DisciplineDto
import retrofit2.http.GET
import retrofit2.http.Header

interface ContentApi {

    // Запрашиваем список дисциплин.
    // Токен будем передавать в заголовке Authorization
    @GET("api/disciplines")
    suspend fun getDisciplines(
        @Header("Authorization") token: String
    ): List<DisciplineDto>
}