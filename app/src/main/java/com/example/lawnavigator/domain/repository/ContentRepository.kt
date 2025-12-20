package com.example.lawnavigator.domain.repository

import com.example.lawnavigator.data.dto.StudentRiskDto
import com.example.lawnavigator.data.dto.SubmitAnswerRequest
import com.example.lawnavigator.data.dto.TeacherGroupDto
import com.example.lawnavigator.data.dto.TestResultDto
import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.model.LeaderboardUser
import com.example.lawnavigator.domain.model.Lecture
import com.example.lawnavigator.domain.model.LectureProgress
import com.example.lawnavigator.domain.model.TestContent
import com.example.lawnavigator.domain.model.TestDraft
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

    suspend fun getLectureProgress(lectureId: Int): Result<LectureProgress>
    suspend fun saveLectureProgress(lectureId: Int, progress: LectureProgress): Result<Unit>
    suspend fun getLecturesByTopic(topicId: Int): Result<List<Lecture>>

    suspend fun uploadDocx(topicId: Int, title: String, fileBytes: ByteArray): Result<Unit>

    suspend fun updateLecture(id: Int, title: String, content: String): Result<Unit>

    suspend fun deleteLecture(id: Int): Result<Unit>
    suspend fun saveTest(testDraft: TestDraft): Result<Unit>
    suspend fun getAdminTest(topicId: Int): Result<TestDraft?>
    suspend fun getTestByLectureId(lectureId: Int): Result<TestContent>
    suspend fun getAdminTestByLecture(lectureId: Int): Result<TestDraft?>

    suspend fun createTopic(disciplineId: Int, name: String): Result<Unit>
    suspend fun updateTopic(topicId: Int, name: String): Result<Unit>
    suspend fun deleteTopic(topicId: Int): Result<Unit>
    suspend fun attachFile(lectureId: Int, fileBytes: ByteArray, fileName: String): Result<Unit>

    suspend fun joinGroup(inviteCode: String): Result<Unit>
    suspend fun createGroup(name: String, disciplineId: Int): Result<String>
    suspend fun getTeacherGroups(): Result<List<TeacherGroupDto>>
    suspend fun getGroupAnalytics(groupId: Int): Result<List<StudentRiskDto>>

    // --- Управление группами (Учитель) ---
    suspend fun updateGroup(groupId: Int, name: String): Result<Unit>
    suspend fun deleteGroup(groupId: Int): Result<Unit>
    suspend fun removeStudent(groupId: Int, studentId: Int): Result<Unit>

    suspend fun getGroupMembers(groupId: Int): Result<List<String>>

}

