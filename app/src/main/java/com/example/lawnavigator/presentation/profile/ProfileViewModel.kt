package com.example.lawnavigator.presentation.profile


import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.usecase.LogoutUseCase
import com.example.lawnavigator.domain.usecase.profile.GetProfileDataUseCase
import com.example.lawnavigator.presentation.profile.ProfileContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel профиля. Загружает аналитику при старте.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileDataUseCase: GetProfileDataUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager // <--- ДОБАВЛЕНО!
) : BaseViewModel<ProfileContract.State, ProfileContract.Event, ProfileContract.Effect>() {

    override fun createInitialState() = ProfileContract.State()

    init {
        loadData()
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            tokenManager.themeMode.collect { mode ->
                setState { copy(themeMode = mode) }
            }
        }
    }

    override fun handleEvent(event: ProfileContract.Event) {
        when (event) {
            is ProfileContract.Event.OnThemeChanged -> {
                viewModelScope.launch {
                    tokenManager.saveTheme(event.mode)
                }
            }
            // Обработка остальных событий (Pull-to-Refresh и навигация)
            is ProfileContract.Event.OnRefresh -> loadData()
            is ProfileContract.Event.OnLogoutClicked -> logout()
            is ProfileContract.Event.OnBackClicked -> setEffect { ProfileContract.Effect.NavigateBack }
            is ProfileContract.Event.OnRecommendationClicked -> {
                setEffect { ProfileContract.Effect.NavigateToTopic(event.topicId) }
            }
        }
    }

    private fun loadData() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getProfileDataUseCase()
                .onSuccess { data ->
                    setState { copy(isLoading = false, analytics = data) }
                }
                .onFailure {
                    setState { copy(isLoading = false, error = "Не удалось загрузить данные") }
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            setEffect { ProfileContract.Effect.NavigateToLogin }
        }
    }
}