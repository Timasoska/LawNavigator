package com.example.lawnavigator.presentation.profile


import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.usecase.CreateGroupUseCase
import com.example.lawnavigator.domain.usecase.GetAnalyticsUseCase
import com.example.lawnavigator.domain.usecase.GetTeacherGroupsUseCase
import com.example.lawnavigator.domain.usecase.JoinGroupUseCase
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
    private val tokenManager: TokenManager,
    private val joinGroupUseCase: JoinGroupUseCase // <--- ВНЕДРЯЕМ КОНКРЕТНЫЙ USECASE
) : BaseViewModel<ProfileContract.State, ProfileContract.Event, ProfileContract.Effect>() {

    override fun createInitialState() = ProfileContract.State()

    init {
        loadData()
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            tokenManager.themeMode.collect { mode -> setState { copy(themeMode = mode) } }
        }
    }

    override fun handleEvent(event: ProfileContract.Event) {
        when (event) {
            is ProfileContract.Event.OnRefresh -> loadData()
            is ProfileContract.Event.OnLogoutClicked -> logout()
            is ProfileContract.Event.OnBackClicked -> setEffect { ProfileContract.Effect.NavigateBack }
            is ProfileContract.Event.OnRecommendationClicked -> setEffect { ProfileContract.Effect.NavigateToTopic(event.topicId) }
            is ProfileContract.Event.OnThemeChanged -> {
                viewModelScope.launch { tokenManager.saveTheme(event.mode) }
            }

            is ProfileContract.Event.OnJoinGroupClicked -> {
                setState { copy(showJoinGroupDialog = true, inviteCodeInput = "") }
            }
            is ProfileContract.Event.OnInviteCodeChanged -> {
                setState { copy(inviteCodeInput = event.code.uppercase()) }
            }
            is ProfileContract.Event.OnDismissDialog -> {
                setState { copy(showJoinGroupDialog = false) }
            }
            is ProfileContract.Event.OnConfirmJoinGroup -> joinGroup()
        }
    }

    private fun joinGroup() {
        val code = currentState.inviteCodeInput
        if (code.isBlank()) return

        setState { copy(isLoading = true, showJoinGroupDialog = false) }

        viewModelScope.launch {
            // ИСПОЛЬЗУЕМ НОВЫЙ USECASE
            joinGroupUseCase.joinGroup(code)
                .onSuccess {
                    setEffect { ProfileContract.Effect.ShowMessage("Вы успешно вступили в группу!") }
                    setState { copy(isLoading = false) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false) }
                    setEffect { ProfileContract.Effect.ShowMessage("Ошибка: ${error.message}") }
                }
        }
    }

    private fun loadData() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            getProfileDataUseCase()
                .onSuccess { data -> setState { copy(isLoading = false, analytics = data) } }
                .onFailure { setState { copy(isLoading = false, error = "Не удалось загрузить данные") } }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            setEffect { ProfileContract.Effect.NavigateToLogin }
        }
    }
}