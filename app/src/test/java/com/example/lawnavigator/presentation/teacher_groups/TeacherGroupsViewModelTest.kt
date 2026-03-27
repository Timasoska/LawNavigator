package com.example.lawnavigator.presentation.teacher_groups

import com.example.lawnavigator.data.dto.TeacherGroupDto
import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.usecase.CreateGroupUseCase
import com.example.lawnavigator.domain.usecase.DeleteGroupUseCase
import com.example.lawnavigator.domain.usecase.GetDisciplinesUseCase
import com.example.lawnavigator.domain.usecase.GetTeacherGroupsUseCase
import com.example.lawnavigator.domain.usecase.UpdateGroupUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeacherGroupsViewModelTest {

    private lateinit var getTeacherGroupsUseCase: GetTeacherGroupsUseCase
    private lateinit var createGroupUseCase: CreateGroupUseCase
    private lateinit var updateGroupUseCase: UpdateGroupUseCase
    private lateinit var deleteGroupUseCase: DeleteGroupUseCase
    private lateinit var getDisciplinesUseCase: GetDisciplinesUseCase
    private lateinit var viewModel: TeacherGroupsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getTeacherGroupsUseCase = mockk()
        createGroupUseCase = mockk()
        updateGroupUseCase = mockk()
        deleteGroupUseCase = mockk()
        getDisciplinesUseCase = mockk()

        // Симулируем ответ от сервера (Список групп)
        val mockGroups = listOf(TeacherGroupDto(1, "Group 101", "Law", "ABC", 25))
        coEvery { getTeacherGroupsUseCase.getTeacherGroups() } returns Result.success(mockGroups)

        // Симулируем ответ от сервера (Список дисциплин для выпадающего списка)
        val mockDisciplines = listOf(Discipline(10, "Criminal Law", "Desc"))
        coEvery { getDisciplinesUseCase() } returns Result.success(mockDisciplines)

        viewModel = TeacherGroupsViewModel(
            getTeacherGroupsUseCase,
            createGroupUseCase,
            updateGroupUseCase,
            deleteGroupUseCase,
            getDisciplinesUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads groups and disciplines correctly`() = runTest {
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isLoading)

        // Проверяем группы
        assertEquals(1, state.groups.size)
        assertEquals("Group 101", state.groups[0].name)

        // Проверяем дисциплины (первая должна быть выбрана по умолчанию)
        assertEquals(1, state.availableDisciplines.size)
        assertEquals("Criminal Law", state.selectedDiscipline?.name)
    }

    @Test
    fun `creating group with empty name does not call UseCase`() = runTest {
        advanceUntilIdle() // Ждем первоначальную загрузку

        // Симулируем создание группы с пустым именем
        viewModel.setEvent(TeacherGroupsContract.Event.OnCreateGroupClicked)
        viewModel.setEvent(TeacherGroupsContract.Event.OnGroupNameChanged("   ")) // Пробелы
        viewModel.setEvent(TeacherGroupsContract.Event.OnConfirmSaveGroup)

        advanceUntilIdle()

        // Проверяем, что запрос на сервер НЕ отправлялся
        coVerify(exactly = 0) { createGroupUseCase.createGroup(any(), any()) }

        // Диалог все еще должен быть открыт (так как ошибка валидации)
        assertEquals(true, viewModel.state.value.showGroupDialog)
    }

    @Test
    fun `successful group creation triggers reload`() = runTest {
        advanceUntilIdle()

        // Мокаем успешное создание (возвращает Invite Code)
        coEvery { createGroupUseCase.createGroup("New Group", 10) } returns Result.success("XYZ123")

        // Симулируем действия учителя
        viewModel.setEvent(TeacherGroupsContract.Event.OnCreateGroupClicked)
        viewModel.setEvent(TeacherGroupsContract.Event.OnGroupNameChanged("New Group"))

        // Выбираем дисциплину (с ID 10)
        viewModel.setEvent(TeacherGroupsContract.Event.OnDisciplineSelected(Discipline(10, "Criminal Law", "Desc")))

        viewModel.setEvent(TeacherGroupsContract.Event.OnConfirmSaveGroup)

        advanceUntilIdle()

        // Проверяем, что диалог закрылся
        assertEquals(false, viewModel.state.value.showGroupDialog)

        // Проверяем, что UseCase был вызван с правильными параметрами
        coVerify(exactly = 1) { createGroupUseCase.createGroup("New Group", 10) }

        // Проверяем, что список групп обновился (1 раз при init, 1 раз после создания)
        coVerify(exactly = 2) { getTeacherGroupsUseCase.getTeacherGroups() }
    }
}