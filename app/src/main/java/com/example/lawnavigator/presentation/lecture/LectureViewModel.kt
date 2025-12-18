package com.example.lawnavigator.presentation.lecture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.usecase.AttachFileUseCase
import com.example.lawnavigator.domain.usecase.DeleteLectureUseCase
import com.example.lawnavigator.domain.usecase.LectureUseCase
import com.example.lawnavigator.domain.usecase.UpdateLectureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LectureViewModel @Inject constructor(
    private val lectureUseCase: LectureUseCase,
    private val updateLectureUseCase: UpdateLectureUseCase, // <--- Новый UseCase
    private val tokenManager: TokenManager, // <--- Для проверки роли
    private val deleteLectureUseCase: DeleteLectureUseCase, // <--- Новый UseCase
    private val attachFileUseCase: AttachFileUseCase, // <--- Инжект
    savedStateHandle: SavedStateHandle
) : BaseViewModel<LectureContract.State, LectureContract.Event, LectureContract.Effect>() {

    private val lectureId: Int = checkNotNull(savedStateHandle["lectureId"])
    private val searchQuery: String? = savedStateHandle["searchQuery"]

    override fun createInitialState() = LectureContract.State()

    init {
        loadLecture()
        checkRole()
    }

    private fun checkRole() {
        viewModelScope.launch {
            val role = tokenManager.role.first()
            setState { copy(isTeacher = role == "teacher") }
        }
    }

    override fun handleEvent(event: LectureContract.Event) {
        when (event) {
            is LectureContract.Event.OnAttachFileSelected -> attachFile(event.bytes, event.name)
            is LectureContract.Event.OnFileClicked -> setEffect { LectureContract.Effect.OpenUrl(event.url) }
            is LectureContract.Event.OnBackClicked -> setEffect { LectureContract.Effect.NavigateBack }
            is LectureContract.Event.OnFavoriteClicked -> toggleFavorite()
            is LectureContract.Event.OnSaveProgress -> saveProgress(event.scrollIndex)

            // Новые события
            is LectureContract.Event.OnEditClicked -> {
                // Включаем режим редактирования и копируем текущий текст в поля для ввода
                val current = currentState.lecture
                if (current != null) {
                    setState {
                        copy(
                            isEditing = true,
                            editedTitle = current.title,
                            editedContent = current.content
                        )
                    }
                }
            }
            is LectureContract.Event.OnSaveEditsClicked -> saveEdits()
            is LectureContract.Event.OnCancelEditClicked -> setState { copy(isEditing = false) }
            is LectureContract.Event.OnTitleChanged -> setState { copy(editedTitle = event.newTitle) }
            is LectureContract.Event.OnContentChanged -> setState { copy(editedContent = event.newContent) }

            // Удаление
            is LectureContract.Event.OnDeleteClicked -> setState { copy(showDeleteDialog = true) }
            is LectureContract.Event.OnDismissDeleteDialog -> setState { copy(showDeleteDialog = false) }
            is LectureContract.Event.OnConfirmDelete -> deleteLecture()
        }
    }

    private fun saveEdits() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            updateLectureUseCase(lectureId, currentState.editedTitle, currentState.editedContent)
                .onSuccess {
                    // Обновляем локальные данные и выходим из режима редактирования
                    setState {
                        copy(
                            isLoading = false,
                            isEditing = false,
                            lecture = lecture?.copy(
                                title = editedTitle,
                                content = editedContent
                            )
                        )
                    }
                    setEffect { LectureContract.Effect.ShowMessage("Лекция обновлена!") }
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    setEffect { LectureContract.Effect.ShowMessage("Ошибка обновления") }
                }
        }
    }

    private fun deleteLecture() {
        setState { copy(isLoading = true, showDeleteDialog = false) }
        viewModelScope.launch {
            deleteLectureUseCase(lectureId)
                .onSuccess {
                    setEffect { LectureContract.Effect.ShowMessage("Лекция удалена") }
                    setEffect { LectureContract.Effect.NavigateBack }
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    setEffect { LectureContract.Effect.ShowMessage("Не удалось удалить лекцию") }
                }
        }
    }

    private fun loadLecture() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            lectureUseCase.getLecture(lectureId)
                .onSuccess { lecture ->
                    val progressResult = lectureUseCase.getProgress(lectureId)
                    val progressIndex = progressResult.map { it.scrollIndex }.getOrDefault(0)
                    setState {
                        copy(
                            isLoading = false,
                            lecture = lecture,
                            isFavorite = lecture.isFavorite,
                            initialScrollIndex = progressIndex,
                            searchQuery = searchQuery
                        )
                    }
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    setEffect { LectureContract.Effect.ShowMessage("Ошибка загрузки") }
                }
        }
    }

    private fun saveProgress(index: Int) {
        if (index <= 0) return
        viewModelScope.launch {
            lectureUseCase.saveProgress(lectureId, index)
        }
    }

    private fun toggleFavorite() {
        val newStatus = !currentState.isFavorite
        setState { copy(isFavorite = newStatus) } // Сразу меняем UI для отзывчивости

        viewModelScope.launch {
            lectureUseCase.toggleFavorite(lectureId, newStatus)
                .onSuccess {
                    val msg = if (newStatus) "Добавлено в избранное" else "Удалено из избранного"
                    setEffect { LectureContract.Effect.ShowMessage(msg) }
                }
                .onFailure {
                    // Если ошибка - откатываем состояние
                    setState { copy(isFavorite = !newStatus) }
                    setEffect { LectureContract.Effect.ShowMessage("Ошибка сети") }
                }
        }
    }

    private fun attachFile(bytes: ByteArray, name: String) {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            attachFileUseCase(lectureId, bytes, name)
                .onSuccess {
                    setEffect { LectureContract.Effect.ShowMessage("Файл прикреплен") }
                    loadLecture() // Перезагружаем лекцию, чтобы увидеть файл в списке
                }
                .onFailure {
                    setState { copy(isLoading = false) }
                    setEffect { LectureContract.Effect.ShowMessage("Ошибка загрузки файла") }
                }
        }
    }
}