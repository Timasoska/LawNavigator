package com.example.lawnavigator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawnavigator.data.local.TokenManager
import com.example.lawnavigator.presentation.navigation.Screen
import com.example.lawnavigator.presentation.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // Добавляем поток темы
    val themeMode = tokenManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    init {
        viewModelScope.launch {
            // Небольшая задержка, чтобы не мелькало (имитация Splash Screen)
            // delay(500)

            tokenManager.token.collect { token ->
                if (!token.isNullOrBlank()) {
                    _startDestination.value = Screen.Home.route
                } else {
                    _startDestination.value = Screen.Login.route
                }
                _isLoading.value = false
            }
        }
    }
}