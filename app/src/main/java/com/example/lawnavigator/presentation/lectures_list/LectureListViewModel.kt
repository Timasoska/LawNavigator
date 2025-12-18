package com.example.lawnavigator.presentation.lectures_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.GetLecturesByTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.usecase.UploadDocxUseCase
import kotlinx.coroutines.flow.first


@HiltViewModel
class LecturesListViewModel @Inject constructor(
    private val getLecturesByTopicUseCase: GetLecturesByTopicUseCase,
    private val uploadDocxUseCase: UploadDocxUseCase,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<LecturesListContract.State, LecturesListContract.Event, LecturesListContract.Effect>() {

    private val topicId: Int = checkNotNull(savedStateHandle["topicId"])

    override fun createInitialState() = LecturesListContract.State()

    init {
        checkRole()
        loadLectures()
    }

    private fun checkRole() {
        viewModelScope.launch {
            val role = tokenManager.role.first()
            setState { copy(isTeacher = role == "teacher") }
        }
    }

    override fun handleEvent(event: LecturesListContract.Event) {
        when (event) {
            is LecturesListContract.Event.OnRefresh -> loadLectures() // <--- Добавить
            is LecturesListContract.Event.OnBackClicked -> setEffect { LecturesListContract.Effect.NavigateBack }
            is LecturesListContract.Event.OnLectureClicked -> setEffect { LecturesListContract.Effect.NavigateToLecture(event.lectureId) }
            is LecturesListContract.Event.OnFileSelected -> uploadFile(event.bytes, event.name)
        }
    }

    private fun uploadFile(bytes: ByteArray, fileName: String) {
        setState { copy(isUploading = true) }
        viewModelScope.launch {
            // Название лекции берем из имени файла (без .docx)
            val title = fileName.substringBeforeLast(".")

            uploadDocxUseCase(topicId, title, bytes)
                .onSuccess {
                    setEffect { LecturesListContract.Effect.ShowMessage("Лекция загружена!") }
                    loadLectures() // Обновляем список
                }
                .onFailure { e ->
                    setEffect { LecturesListContract.Effect.ShowMessage("Ошибка: ${e.localizedMessage}") }
                }

            setState { copy(isUploading = false) }
        }
    }

    private fun loadLectures() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getLecturesByTopicUseCase(topicId)
                .onSuccess { list ->
                    setState { copy(isLoading = false, lectures = list) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                }
        }
    }
}