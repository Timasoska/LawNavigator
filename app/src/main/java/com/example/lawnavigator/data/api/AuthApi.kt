package com.example.lawnavigator.data.api

import com.example.lawnavigator.data.dto.AuthRequestDto
import com.example.lawnavigator.data.dto.AuthResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: AuthRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequestDto): AuthResponseDto
}