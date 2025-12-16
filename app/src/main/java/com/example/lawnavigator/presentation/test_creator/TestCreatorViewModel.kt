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
    private val getAdminTestUseCase: GetAdminTestUseCase, // <--- Инжект
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TestCreatorContract.State, TestCreatorContract.Event, TestCreatorContract.Effect>() {

    private val topicId: Int = savedStateHandle.get<Int>("topicId") ?: 0
    private var existingDraft: TestDraft? = null // Храним загруженный драфт

    init {
        checkExistingTest()
    }

    private fun checkExistingTest() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getAdminTestUseCase(topicId)
                .onSuccess { draft ->
                    if (draft != null) {
                        // Сохраняем во временную переменную, чтобы потом восстановить
                        existingDraft = draft
                        // Показываем диалог
                        setState { copy(isLoading = false, showFoundTestDialog = true) }
                    } else {
                        // Теста нет -> Можно создавать новый
                        setState { copy(isLoading = false) }
                    }
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    // Ошибку можно игнорировать и просто дать создать новый
                }
        }
    }

    override fun createInitialState(): TestCreatorContract.State {
        // Добавь лог сюда
        android.util.Log.d("TestCreatorDebug", "ViewModel init with topicId: $topicId")
        return TestCreatorContract.State(
            testDraft = TestDraft(
                topicId = topicId,
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
                // Просто закрываем диалог, у нас уже пустой черновик
                setState { copy(showFoundTestDialog = false) }
            }
            is TestCreatorContract.Event.OnBackClicked -> setEffect { TestCreatorContract.Effect.NavigateBack }
            is TestCreatorContract.Event.OnSaveTestClicked -> saveTest()

            is TestCreatorContract.Event.OnTitleChanged -> {
                setState { copy(testDraft = testDraft.copy(title = event.title)) }
            }
            is TestCreatorContract.Event.OnTimeLimitChanged -> {
                // Фильтруем ввод (только цифры)
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

        // Ищем, есть ли уже вопрос с таким ID (редактирование)
        val index = currentList.indexOfFirst { it.id == question.id }

        if (index != -1) {
            currentList[index] = question // Обновляем
        } else {
            currentList.add(question) // Добавляем новый
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

        // --- ИСПРАВЛЕНИЕ: Принудительно ставим правильный topicId перед отправкой ---
        val finalDraft = draft.copy(topicId = this.topicId)
        if (finalDraft.title.isBlank()) { // Используем finalDraft
            setEffect { TestCreatorContract.Effect.ShowMessage("Введите название теста") }
            return
        }
        if (draft.questions.isEmpty()) {
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