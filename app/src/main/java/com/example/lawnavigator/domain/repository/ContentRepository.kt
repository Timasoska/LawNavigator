package com.example.lawnavigator.domain.repository

import com.example.lawnavigator.data.dto.SubmitAnswerRequest
import com.example.lawnavigator.data.dto.TestResultDto
import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.model.LeaderboardUser
import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.model.TestContent
import com.example.lawnavigator.domain.model.TestResult
import com.example.lawnavigator.domain.model.Topic

interface ContentRepository {
    suspend fun getDisciplines(): Result<List<Discipline>>
    suspend fun getTopics(disciplineId: Int): Result<List<Topic>>
    suspend fun getLecture(lectureId: Int): Result<Lecture>
    suspend fun toggleFavorite(lectureId: Int, addToFavorites: Boolean): Result<Unit>
    suspend fun getTest(topicId: Int): Result<TestContent>
    suspend fun submitTest(testId: Int, answers: List<SubmitAnswerRequest>): Result<TestResultDto>
    suspend fun searchLectures(query: String): Result<List<Lecture>>
    suspend fun getFavorites(): Result<List<Lecture>>
    suspend fun addToFavorites(lectureId: Int): Result<Unit>
    suspend fun removeFromFavorites(lectureId: Int): Result<Unit>
    suspend fun getLeaderboard(): Result<List<LeaderboardUser>>

}

