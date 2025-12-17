package com.example.lawnavigator.presentation.test_creator

import com.example.lawnavigator.domain.model.QuestionDraft
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.model.TestDraft
import com.example.lawnavigator.domain.usecase.GetAdminTestUseCase
import com.example.lawnavigator.domain.usecase.SaveTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TestCreatorViewModel @Inject constructor(
    private val saveTestUseCase: SaveTestUseCase,
    private val getAdminTestUseCase: GetAdminTestUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TestCreatorContract.State, TestCreatorContract.Event, TestCreatorContract.Effect>() {

    // 1. Читаем оба аргумента. Если аргумента нет, вернется null, поэтому используем эвис ?: -1
    private val topicIdArg: Int = savedStateHandle["topicId"] ?: -1
    private val lectureIdArg: Int = savedStateHandle["lectureId"] ?: -1

    // 2. Преобразуем в Nullable для удобства логики
    // Если пришел -1, значит это null (не этот тип теста)
    private val topicId: Int? = if (topicIdArg != -1) topicIdArg else null
    private val lectureId: Int? = if (lectureIdArg != -1) lectureIdArg else null

    private var existingDraft: TestDraft? = null // Храним загруженный драфт

    init {
        checkExistingTest()
    }

    private fun checkExistingTest() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            // Передаем оба ID, UseCase сам разберется, какой искать
            getAdminTestUseCase(topicId, lectureId)
                .onSuccess { draft ->
                    if (draft != null) {
                        existingDraft = draft
                        setState { copy(isLoading = false, showFoundTestDialog = true) }
                    } else {
                        setState { copy(isLoading = false) }
                    }
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                }
        }
    }

    override fun createInitialState(): TestCreatorContract.State {
        android.util.Log.d("TestCreatorDebug", "Init: topicId=$topicId, lectureId=$lectureId")

        return TestCreatorContract.State(
            testDraft = TestDraft(
                topicId = topicId,     // Может быть null
                lectureId = lectureId, // Может быть null
                title = "",
                timeLimitMinutes = 0
            )
        )
    }

    override fun handleEvent(event: TestCreatorContract.Event) {
        when (event) {
            is TestCreatorContract.Event.OnLoadExistingTest -> {
                existingDraft?.let { draft ->
                    setState {
                        copy(
                            testDraft = draft,
                            showFoundTestDialog = false
                        )
                    }
                }
            }
            is TestCreatorContract.Event.OnCreateNewTest -> {
                setState { copy(showFoundTestDialog = false) }
            }
            is TestCreatorContract.Event.OnBackClicked -> setEffect { TestCreatorContract.Effect.NavigateBack }
            is TestCreatorContract.Event.OnSaveTestClicked -> saveTest()

            is TestCreatorContract.Event.OnTitleChanged -> {
                setState { copy(testDraft = testDraft.copy(title = event.title)) }
            }
            is TestCreatorContract.Event.OnTimeLimitChanged -> {
                val minutes = event.minutes.filter { it.isDigit() }.toIntOrNull() ?: 0
                setState { copy(testDraft = testDraft.copy(timeLimitMinutes = minutes)) }
            }

            // Работа с диалогом
            is TestCreatorContract.Event.OnAddQuestionClicked -> {
                setState { copy(isQuestionDialogOpen = true, editingQuestion = null) }
            }
            is TestCreatorContract.Event.OnEditQuestionClicked -> {
                setState { copy(isQuestionDialogOpen = true, editingQuestion = event.question) }
            }
            is TestCreatorContract.Event.OnCloseDialog -> {
                setState { copy(isQuestionDialogOpen = false, editingQuestion = null) }
            }

            // Логика списка вопросов
            is TestCreatorContract.Event.OnSaveQuestion -> saveQuestionToDraft(event.question)
            is TestCreatorContract.Event.OnDeleteQuestionClicked -> deleteQuestion(event.questionId)
        }
    }

    private fun saveQuestionToDraft(question: QuestionDraft) {
        val currentList = currentState.testDraft.questions.toMutableList()
        val index = currentList.indexOfFirst { it.id == question.id }

        if (index != -1) {
            currentList[index] = question
        } else {
            currentList.add(question)
        }

        setState {
            copy(
                testDraft = testDraft.copy(questions = currentList),
                isQuestionDialogOpen = false,
                editingQuestion = null
            )
        }
    }

    private fun deleteQuestion(id: String) {
        val newList = currentState.testDraft.questions.filter { it.id != id }
        setState { copy(testDraft = testDraft.copy(questions = newList)) }
    }

    private fun saveTest() {
        val draft = currentState.testDraft

        // 3. ФИНАЛЬНАЯ СБОРКА: Гарантируем, что ID правильные перед отправкой
        // Мы берем topicId и lectureId из полей класса (которые мы вычислили в init),
        // а не из draft, чтобы исключить любые ошибки UI.
        val finalDraft = draft.copy(
            topicId = this.topicId,
            lectureId = this.lectureId
        )

        if (finalDraft.title.isBlank()) {
            setEffect { TestCreatorContract.Effect.ShowMessage("Введите название теста") }
            return
        }
        if (finalDraft.questions.isEmpty()) {
            setEffect { TestCreatorContract.Effect.ShowMessage("Добавьте хотя бы один вопрос") }
            return
        }

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            saveTestUseCase(finalDraft)
                .onSuccess {
                    setEffect { TestCreatorContract.Effect.ShowMessage("Тест сохранен!") }
                    setEffect { TestCreatorContract.Effect.NavigateBack }
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    setEffect { TestCreatorContract.Effect.ShowMessage("Ошибка: ${it.message}") }
                }
        }
    }
}