package com.example.lawnavigator.domain.repository

import com.example.lawnavigator.domain.model.Discipline

interface ContentRepository {
    suspend fun getDisciplines(): Result<List<Discipline>>
}