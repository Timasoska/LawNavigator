package com.example.lawnavigator.domain.repository

import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.model.Topic

interface ContentRepository {
    suspend fun getDisciplines(): Result<List<Discipline>>
    suspend fun getTopics(disciplineId: Int): Result<List<Topic>>
}

