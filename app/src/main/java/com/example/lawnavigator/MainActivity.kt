package com.example.lawnavigator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.lawnavigator.presentation.login.LoginScreen
import com.example.lawnavigator.presentation.theme.LawNavigatorTheme // Тема по умолчанию
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Если темы нет, используй стандартную или удали обертку
            LawNavigatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onNavigateToHome = {
                            // Пока просто покажем уведомление, что вход успешен
                            Toast.makeText(this, "Переход на главный экран!", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }
}