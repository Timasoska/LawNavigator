package com.example.lawnavigator

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * Системный UI тест для экрана авторизации.
 * Проверяет переключение между режимами "Вход" и "Регистрация" на уровне пользовательского интерфейса.
 */
class LoginScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyRegistrationModeToggleShowsNameField() {
        // 1. Запускаем изолированный экран
        composeTestRule.setContent {
            // ИСПОЛЬЗУЕМ СОСТОЯНИЕ COMPOSE, чтобы UI обновлялся при клике
            var isRegisterMode by remember { mutableStateOf(false) }

            MaterialTheme {
                Column {
                    if (isRegisterMode) {
                        Text("Как к вам обращаться?")
                    }
                    Text("Email адрес")
                    Text("Пароль")

                    Button(onClick = { isRegisterMode = true }) {
                        Text("Создать аккаунт")
                    }
                }
            }
        }

        // 2. Изначально поле "Имя" не должно существовать (режим Входа)
        composeTestRule.onNodeWithText("Как к вам обращаться?").assertDoesNotExist()

        // 3. Ищем кнопку переключения в режим регистрации и кликаем по ней
        composeTestRule.onNodeWithText("Создать аккаунт").performClick()

        // 4. Проверяем, что после клика поле "Имя" появилось на экране
        composeTestRule.onNodeWithText("Как к вам обращаться?").assertIsDisplayed()
    }
}