package com.example.lawnavigator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lawnavigator.presentation.navigation.AppNavigation
import com.example.lawnavigator.presentation.theme.LawNavigatorTheme
import com.example.lawnavigator.worker.FlashcardWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Регистрация контракта на запрос разрешения
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Установка Splash Screen (должно быть до super.onCreate)
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 1. ЗАПРОС РАЗРЕШЕНИЯ НА УВЕДОМЛЕНИЯ (Только для Android 13 / API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 2. ЗАПУСК ФОНОВОГО ПЛАНИРОВЩИКА (WORK MANAGER)
        // Запускаем проверку карточек каждые 12 часов
        val workRequest = PeriodicWorkRequestBuilder<FlashcardWorker>(12, TimeUnit.HOURS)
            .setInitialDelay(10, TimeUnit.SECONDS) // Задержка перед первым запуском (для теста)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FlashcardReminder",
            ExistingPeriodicWorkPolicy.KEEP, // Если уже запланировано — не дублировать
            workRequest
        )

        // 3. ОТРИСОВКА UI
        setContent {
            val viewModel = hiltViewModel<MainViewModel>()
            val startDestination by viewModel.startDestination.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val currentTheme by viewModel.themeMode.collectAsState()

            LawNavigatorTheme(themeMode = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Пока определяем стартовый экран (проверка токена), показываем лоадер
                    if (isLoading || startDestination == null) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Запуск навигации
                        AppNavigation(startDestination = startDestination!!)
                    }
                }
            }
        }
    }
}