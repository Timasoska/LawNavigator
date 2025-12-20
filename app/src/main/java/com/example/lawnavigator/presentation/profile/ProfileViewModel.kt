package com.example.lawnavigator.presentation.profile


import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.core.mvi.BaseViewModel
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.domain.repository.ContentRepository
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
 * ViewModel профиля. Управляет аналитикой, вступлением в группы и просмотром участников.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileDataUseCase: GetProfileDataUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager,
    private val joinGroupUseCase: JoinGroupUseCase,
    private val contentRepository: ContentRepository // <--- ДОБАВЛЕНО: Исправляет ошибку Unresolved reference
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
            is ProfileContract.Event.OnConfirmJoinGroup -> joinGroup()
            is ProfileContract.Event.OnDismissDialog -> {
                setState { copy(showJoinGroupDialog = false, showMembersDialog = false) }
            }
            // СОБЫТИЕ: Клик по группе для просмотра участников
            is ProfileContract.Event.OnGroupClicked -> loadGroupMembers(event.groupId)
        }
    }

    private fun loadGroupMembers(groupId: Int) {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            contentRepository.getGroupMembers(groupId)
                .onSuccess { members: List<String> -> // Явно указали тип
                    setState {
                        copy(
                            isLoading = false,
                            showMembersDialog = true,
                            groupMembers = members
                        )
                    }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false) }
                    setEffect { ProfileContract.Effect.ShowMessage("Ошибка: ${error.message}") }
                }
        }
    }

    private fun joinGroup() {
        val code = currentState.inviteCodeInput
        if (code.isBlank()) return
        setState { copy(isLoading = true, showJoinGroupDialog = false) }
        viewModelScope.launch {
            joinGroupUseCase.joinGroup(code)
                .onSuccess {
                    setEffect { ProfileContract.Effect.ShowMessage("Вы успешно вступили в группу!") }
                    loadData() // Обновляем профиль, чтобы увидеть новую группу
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