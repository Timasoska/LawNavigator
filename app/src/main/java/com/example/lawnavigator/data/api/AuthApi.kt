package com.example.lawnavigator.data.api

import com.example.lawnavigator.data.dto.AuthResponseDto
import com.example.lawnavigator.data.dto.LoginRequestDto
import com.example.lawnavigator.data.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto
    /*@POST("auth/register")
    suspend fun register(@Body request: AuthRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequestDto): AuthResponseDto*/
}