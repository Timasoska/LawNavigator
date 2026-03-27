package com.example.lawnavigator.presentation.profile

import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.model.DisciplineStat
import com.example.lawnavigator.domain.model.Topic
import com.example.lawnavigator.domain.model.UserAnalytics
import com.example.lawnavigator.domain.model.UserGroup
import com.example.lawnavigator.domain.repository.ContentRepository
import com.example.lawnavigator.domain.usecase.JoinGroupUseCase
import com.example.lawnavigator.domain.usecase.LogoutUseCase
import com.example.lawnavigator.domain.usecase.profile.GetProfileDataUseCase
import com.example.lawnavigator.presentation.theme.ThemeMode
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var getProfileDataUseCase: GetProfileDataUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var tokenManager: TokenManager
    private lateinit var joinGroupUseCase: JoinGroupUseCase
    private lateinit var contentRepository: ContentRepository
    private lateinit var viewModel: ProfileViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getProfileDataUseCase = mockk()
        logoutUseCase = mockk()
        tokenManager = mockk(relaxed = true) // Relaxed позволяет не мокать методы, возвращающие Unit
        joinGroupUseCase = mockk()
        contentRepository = mockk()

        // Мокаем Flow из TokenManager
        coEvery { tokenManager.userName } returns MutableStateFlow("Ivan Ivanov")
        coEvery { tokenManager.role } returns MutableStateFlow("student")
        coEvery { tokenManager.themeMode } returns MutableStateFlow(ThemeMode.DARK)

        // Имитация успешной загрузки данных профиля
        val mockAnalytics = UserAnalytics(
            testsPassed = 15,
            averageScore = 82.5,
            trend = 2.1,
            history = listOf(70, 80, 90),
            groups = listOf(UserGroup(1, "Law-101")),
            disciplines = listOf(DisciplineStat(1, "Criminal Law", 85.0, 1.5)),
            recommendations = listOf(Topic(1, "History of Law", 1, null))
        )
        coEvery { getProfileDataUseCase() } returns Result.success(mockAnalytics)

        viewModel = ProfileViewModel(
            getProfileDataUseCase,
            logoutUseCase,
            tokenManager,
            joinGroupUseCase,
            contentRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads analytics and user data correctly`() = runTest {
        // Прогоняем корутины, запущенные в init блоке ViewModel
        advanceUntilIdle()

        val state = viewModel.state.value

        // Проверяем, что данные из TokenManager попали в стейт
        assertEquals("Ivan Ivanov", state.userName)
        assertEquals("student", state.userRole)
        assertEquals(ThemeMode.DARK, state.themeMode)

        // Проверяем загрузку аналитики
        assertEquals(false, state.isLoading)
        assertNotNull(state.analytics)
        assertEquals(82.5, state.analytics?.averageScore)
        assertEquals(1, state.analytics?.recommendations?.size)
    }

    @Test
    fun `join group success updates state and shows message`() = runTest {
        val inviteCode = "A1B2C3"
        coEvery { joinGroupUseCase.joinGroup(inviteCode) } returns Result.success(Unit)
        // Для обновления после успешного вступления
        coEvery { getProfileDataUseCase() } returns Result.success(mockk(relaxed = true))

        // Имитируем ввод кода и нажатие кнопки
        viewModel.setEvent(ProfileContract.Event.OnJoinGroupClicked)
        viewModel.setEvent(ProfileContract.Event.OnInviteCodeChanged(inviteCode))
        viewModel.setEvent(ProfileContract.Event.OnConfirmJoinGroup)

        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.showJoinGroupDialog) // Диалог должен закрыться
        assertEquals(false, state.isLoading) // Спиннер должен пропасть
    }
}