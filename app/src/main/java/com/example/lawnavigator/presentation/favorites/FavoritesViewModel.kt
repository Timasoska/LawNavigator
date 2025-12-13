package com.example.lawnavigator.presentation.favorites

import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.domain.usecase.FavoritesUseCase
// ... А лучше сделать отдельный UseCase getUser() или хранить ID в TokenManager.
// Для простоты, допустим, FavoritesUseCase сам достает ID или ID не нужен (если API берет из токена).
// API getFavorites берет из токена, но FavoritesUseCase.getAll(userId) требует ID.
// ДАВАЙ ИСПРАВИМ FavoritesUseCase, чтобы он не требовал userId (брал из токена внутри репо)
// В Android ContentRepositoryImpl.getFavorites может брать userId из токена (декодировать JWT).
// Но проще всего передать заглушку 0, так как на БЭКЕНДЕ метод getFavorites берет ID из токена.
// Проверь ContentRouting на бэке: get("/api/favorites") берет userId из principal. Значит аргумент userId в API клиенте не важен?
// А, в Android ContentApi.getFavorites не принимает userId. Отлично!

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesUseCase: FavoritesUseCase
) : BaseViewModel<FavoritesContract.State, FavoritesContract.Event, FavoritesContract.Effect>() {

    override fun createInitialState() = FavoritesContract.State()

    init {
        loadFavorites()
    }

    override fun handleEvent(event: FavoritesContract.Event) {
        when (event) {
            is FavoritesContract.Event.OnBackClicked -> setEffect { FavoritesContract.Effect.NavigateBack }
            is FavoritesContract.Event.OnLectureClicked -> setEffect { FavoritesContract.Effect.NavigateToLecture(event.lectureId) }
            is FavoritesContract.Event.OnRemoveClicked -> removeFavorite(event.lectureId)
            is FavoritesContract.Event.OnRetryClicked -> loadFavorites()
        }
    }

    private fun loadFavorites() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // ИСПРАВЛЕНО: убран аргумент 0, так как UseCase берет токен сам
            favoritesUseCase.getAll()
                .onSuccess { list ->
                    println("Favorites loaded: ${list.size}")
                    setState { copy(isLoading = false, favorites = list) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun removeFavorite(lectureId: Int) {
        viewModelScope.launch {
            // ИСПРАВЛЕНО: убрана заглушка userId = 0
            favoritesUseCase.remove(lectureId)
            loadFavorites() // Перезагружаем список
        }
    }
}