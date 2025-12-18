package com.example.lawnavigator.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lawnavigator.presentation.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("auth_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val THEME_KEY = stringPreferencesKey("app_theme") // <--- Новый ключ
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    // Если роли нет, считаем студентом (безопасный дефолт при чтении)
    val role: Flow<String> = context.dataStore.data.map { it[ROLE_KEY] ?: "student" }

    // Единственный правильный метод сохранения
    suspend fun saveAuthData(token: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
        }
    }

    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
        }
    }

    // Читаем тему (по умолчанию SYSTEM)
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val savedName = prefs[THEME_KEY]
        if (savedName != null) {
            try {
                ThemeMode.valueOf(savedName)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        } else {
            ThemeMode.SYSTEM
        }
    }

    suspend fun saveTheme(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }
}