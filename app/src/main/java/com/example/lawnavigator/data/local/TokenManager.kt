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
        private val NAME_KEY = stringPreferencesKey("user_name") // <--- Новый ключ для имени
        private val THEME_KEY = stringPreferencesKey("app_theme")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    // Если роли нет, считаем студентом
    val role: Flow<String> = context.dataStore.data.map { it[ROLE_KEY] ?: "student" }

    // Читаем имя (по умолчанию "User", если еще не сохранено)
    val userName: Flow<String> = context.dataStore.data.map { it[NAME_KEY] ?: "User" }

    // Обновленный метод сохранения: теперь принимает и сохраняет имя
    suspend fun saveAuthData(token: String, role: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
            preferences[NAME_KEY] = name // <--- Сохраняем имя
        }
    }

    // При выходе удаляем всё, включая имя
    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(NAME_KEY)
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