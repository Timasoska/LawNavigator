package com.example.lawnavigator.presentation.topics

import androidx.lifecycle.SavedStateHandle
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.usecase.CreateTopicUseCase
import com.example.lawnavigator.domain.usecase.DeleteTopicUseCase
import com.example.lawnavigator.domain.usecase.GetTopicsUseCase
import com.example.lawnavigator.domain.usecase.UpdateTopicUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopicsViewModelTest {

    private lateinit var getTopicsUseCase: GetTopicsUseCase
    private lateinit var tokenManager: TokenManager
    private lateinit var createTopicUseCase: CreateTopicUseCase
    private lateinit var updateTopicUseCase: UpdateTopicUseCase
    private lateinit var deleteTopicUseCase: DeleteTopicUseCase
    private lateinit var viewModel: TopicsViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val mockDisciplineId = 5

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getTopicsUseCase = mockk()
        tokenManager = mockk(relaxed = true)
        createTopicUseCase = mockk()
        updateTopicUseCase = mockk()
        deleteTopicUseCase = mockk()

        // Симулируем учителя
        coEvery { tokenManager.role } returns MutableStateFlow("teacher")

        // Симулируем загрузку тем
        val mockTopics = listOf(Topic(1, "Topic 1", mockDisciplineId, 80))
        coEvery { getTopicsUseCase(mockDisciplineId) } returns Result.success(mockTopics)

        val savedStateHandle = SavedStateHandle(mapOf("disciplineId" to mockDisciplineId))

        viewModel = TopicsViewModel(
            getTopicsUseCase,
            tokenManager,
            createTopicUseCase,
            updateTopicUseCase,
            deleteTopicUseCase,
            savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `delete topic confirms and reloads list`() = runTest {
        val topicToDelete = 1

        // Мокаем успешное удаление
        coEvery { deleteTopicUseCase.deleteTopic(topicToDelete) } returns Result.success(Unit)

        advanceUntilIdle() // Ждем первоначальную загрузку

        // 1. Учитель нажимает на корзину -> открывается диалог
        viewModel.setEvent(TopicsContract.Event.OnDeleteTopicClicked(topicToDelete))
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.showDeleteDialog)
        assertEquals(topicToDelete, viewModel.state.value.topicToDeleteId)

        // 2. Учитель подтверждает удаление
        viewModel.setEvent(TopicsContract.Event.OnConfirmDeleteTopic)
        advanceUntilIdle()

        // Диалог закрылся, спиннер выключен
        assertEquals(false, viewModel.state.value.showDeleteDialog)
        assertEquals(false, viewModel.state.value.isLoading)

        // Проверяем, что UseCase был вызван
        coVerify(exactly = 1) { deleteTopicUseCase.deleteTopic(topicToDelete) }
        // Проверяем, что список был перезагружен (2 раза: при init и после удаления)
        coVerify(exactly = 2) { getTopicsUseCase(mockDisciplineId) }
    }
}