package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequestDto(
    val name: String,
    val disciplineId: Int
)

@Serializable
data class JoinGroupRequestDto(
    val inviteCode: String
)

@Serializable
data class TeacherGroupDto(
    val id: Int,
    val name: String,
    val disciplineName: String,
    val inviteCode: String,
    val studentCount: Int
)

@Serializable
data class StudentRiskDto(
    val studentId: Int,
    val email: String,
    val averageScore: Double,
    val trend: Double,
    val riskLevel: String // "GREEN", "YELLOW", "RED"
)

@Serializable
data class InviteCodeResponse(
    val inviteCode: String
)