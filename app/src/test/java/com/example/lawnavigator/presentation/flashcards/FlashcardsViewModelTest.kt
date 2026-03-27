package com.example.lawnavigator.presentation.flashcards

import com.example.lawnavigator.domain.model.Flashcard
import com.example.lawnavigator.domain.model.FlashcardOption
import com.example.lawnavigator.domain.usecase.AddXpUseCase
import com.example.lawnavigator.domain.usecase.GetFlashcardsUseCase
import com.example.lawnavigator.domain.usecase.ReviewFlashcardUseCase
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
class FlashcardsViewModelTest {

    private lateinit var getFlashcardsUseCase: GetFlashcardsUseCase
    private lateinit var reviewFlashcardUseCase: ReviewFlashcardUseCase
    private lateinit var addXpUseCase: AddXpUseCase
    private lateinit var viewModel: FlashcardsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getFlashcardsUseCase = mockk()
        reviewFlashcardUseCase = mockk()
        addXpUseCase = mockk()

        // Создаем фейковую колоду из 2 карточек
        val mockCards = listOf(
            Flashcard(1, "What is Law?", listOf(FlashcardOption(10, "Rules", true))),
            Flashcard(2, "What is Tort?", listOf(FlashcardOption(20, "Civil wrong", true)))
        )

        coEvery { getFlashcardsUseCase() } returns Result.success(mockCards)

        viewModel = FlashcardsViewModel(getFlashcardsUseCase, reviewFlashcardUseCase, addXpUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads cards correctly`() = runTest {
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(2, state.cards.size)
        assertEquals(0, state.currentIndex)
        assertEquals(false, state.isFlipped)
        assertEquals(false, state.isFinished)
        assertEquals("What is Law?", state.currentCard?.question)
    }

    @Test
    fun `flipping card updates state`() = runTest {
        advanceUntilIdle() // Ждем загрузки

        // Нажимаем на карточку
        viewModel.setEvent(FlashcardsContract.Event.OnCardFlip)
        advanceUntilIdle()

        // Проверяем, что флаг isFlipped изменился
        assertEquals(true, viewModel.state.value.isFlipped)
    }

    @Test
    fun `rating last card finishes session and adds XP`() = runTest {
        advanceUntilIdle() // Ждем загрузки

        // Мокаем отправку оценки и получение опыта
        coEvery { reviewFlashcardUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { addXpUseCase(50, "flashcards_session") } returns Result.success(Unit)

        // 1. Оцениваем ПЕРВУЮ карточку (индекс 0) как "Легко" (5)
        viewModel.setEvent(FlashcardsContract.Event.OnRate(5))
        advanceUntilIdle()

        // Проверяем переход ко второй карточке
        assertEquals(1, viewModel.state.value.currentIndex)
        assertEquals("What is Tort?", viewModel.state.value.currentCard?.question)
        assertEquals(false, viewModel.state.value.isFlipped) // Новая карточка рубашкой вверх
        assertEquals(false, viewModel.state.value.isFinished)

        // 2. Оцениваем ВТОРУЮ (последнюю) карточку как "Норм" (3)
        viewModel.setEvent(FlashcardsContract.Event.OnRate(3))
        advanceUntilIdle()

        // Проверяем, что сессия завершена
        assertEquals(true, viewModel.state.value.isFinished)

        // Проверяем, что UseCase были вызваны правильно
        coVerify(exactly = 1) { reviewFlashcardUseCase(1, 5) } // Для первой карты
        coVerify(exactly = 1) { reviewFlashcardUseCase(2, 3) } // Для второй карты
        coVerify(exactly = 1) { addXpUseCase(50, "flashcards_session") } // Начислен бонус
    }
}