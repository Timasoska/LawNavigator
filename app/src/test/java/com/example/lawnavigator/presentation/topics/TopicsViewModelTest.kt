package com.example.lawnavigator.presentation.topics

import androidx.lifecycle.SavedStateHandle
import com.example.lawnavigator.MainDispatcherRule
import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.usecase.GetTopicsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TopicsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = mockk<GetTopicsUseCase>()
    // Эмулируем переданные аргументы навигации
    private val savedStateHandle = SavedStateHandle(mapOf("disciplineId" to 1))

    @Test
    fun `init should load topics successfully`() = runTest {
        // GIVEN
        val mockTopics = listOf(Topic(1, "Тема 1", 1))
        coEvery { useCase(1) } returns Result.success(mockTopics)

        // WHEN (ViewModel создается и сразу вызывает init { loadTopics() })
        val viewModel = TopicsViewModel(useCase, savedStateHandle)

        // THEN
        assertEquals(mockTopics, viewModel.currentState.topics)
        assertEquals(false, viewModel.currentState.isLoading)
    }

    @Test
    fun `init should handle error`() = runTest {
        // GIVEN
        val errorMsg = "Network Error"
        coEvery { useCase(1) } returns Result.failure(Exception(errorMsg))

        // WHEN
        val viewModel = TopicsViewModel(useCase, savedStateHandle)

        // THEN
        assertEquals(emptyList<Topic>(), viewModel.currentState.topics)
        assertEquals(errorMsg, viewModel.currentState.error)
    }
}