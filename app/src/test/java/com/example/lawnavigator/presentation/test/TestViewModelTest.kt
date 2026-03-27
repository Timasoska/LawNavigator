package com.example.lawnavigator.presentation.test

import androidx.lifecycle.SavedStateHandle
import com.example.lawnavigator.domain.model.Answer
import com.example.lawnavigator.domain.model.Question
import com.example.lawnavigator.domain.model.TestContent
import com.example.lawnavigator.domain.model.TestResult
import com.example.lawnavigator.domain.usecase.AddXpUseCase
import com.example.lawnavigator.domain.usecase.GetTestByLectureUseCase
import com.example.lawnavigator.domain.usecase.SubmitTestUseCase
import com.example.lawnavigator.domain.usecase.TestUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestViewModelTest {

    private lateinit var testUseCase: TestUseCase
    private lateinit var submitTestUseCase: SubmitTestUseCase
    private lateinit var getTestByLectureUseCase: GetTestByLectureUseCase
    private lateinit var addXpUseCase: AddXpUseCase
    private lateinit var viewModel: TestViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Мокаем системный класс Log
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0

        testUseCase = mockk()
        submitTestUseCase = mockk()
        getTestByLectureUseCase = mockk()
        addXpUseCase = mockk()

        val savedStateHandle = SavedStateHandle(mapOf("topicId" to 1, "lectureId" to -1))

        val dummyTest = TestContent(
            id = 10,
            title = "Dummy Test",
            timeLimit = 0,
            questions = listOf(
                Question(1, "Q1", 1, false, listOf(Answer(101, "A1"), Answer(102, "A2"))),
                Question(2, "Q2", 1, false, listOf(Answer(201, "A3"), Answer(202, "A4")))
            )
        )

        coEvery { testUseCase.loadTest(1) } returns Result.success(dummyTest)

        viewModel = TestViewModel(testUseCase, submitTestUseCase, getTestByLectureUseCase, addXpUseCase, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test sequential answering and submission`() = runTest {
        advanceUntilIdle()

        // 1. Отвечаем на первый вопрос
        viewModel.setEvent(TestContract.Event.OnAnswerSelected(questionId = 1, answerId = 101))
        advanceUntilIdle()

        // 2. Нажимаем Далее
        viewModel.setEvent(TestContract.Event.OnNextClicked)
        advanceUntilIdle()

        // 3. Отвечаем на второй вопрос
        viewModel.setEvent(TestContract.Event.OnAnswerSelected(questionId = 2, answerId = 202))
        advanceUntilIdle()

        // 4. Подготавливаем моки
        val expectedResult = TestResult(score = 100, message = "Perfect!", correctAnswers = emptyMap())
        coEvery { submitTestUseCase(any(), any(), any()) } returns Result.success(expectedResult)
        coEvery { addXpUseCase(any(), any()) } returns Result.success(Unit)

        // 5. Завершаем тест
        viewModel.setEvent(TestContract.Event.OnNextClicked)
        advanceUntilIdle()

        // 6. Проверка итогов
        val finalState = viewModel.state.value
        assertEquals(100, finalState.resultScore)
        assertEquals("Perfect!", finalState.resultMessage)
    }
}