package com.example.lawnavigator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.lawnavigator.presentation.navigation.AppNavigation
import com.example.lawnavigator.presentation.theme.LawNavigatorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливаем системный Splash Screen (опционально, но красиво)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = hiltViewModel<MainViewModel>()
            val startDestination by viewModel.startDestination.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()

            // Подписываемся на тему
            val currentTheme by viewModel.themeMode.collectAsState()

            // Передаем themeMode в нашу тему
            LawNavigatorTheme(themeMode = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoading || startDestination == null) {
                        // Показываем лоадер, пока проверяем токен
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Запускаем навигацию с правильного экрана
                        AppNavigation(startDestination = startDestination!!)
                    }
                }
            }
        }
    }
}