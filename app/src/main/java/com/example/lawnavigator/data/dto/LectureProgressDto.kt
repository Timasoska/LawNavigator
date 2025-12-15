package com.example.lawnavigator.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LectureProgressDto(
    val lectureId: Int,
    val progressIndex: Int,
    val quote: String? = null
)

@Serializable
data class UpdateProgressRequest(
    val progressIndex: Int,
    val quote: String? = null
)