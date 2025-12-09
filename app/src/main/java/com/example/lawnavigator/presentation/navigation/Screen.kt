package com.example.lawnavigator.presentation.navigation

sealed class Screen(val route: String) {
    // Экран входа
    data object Login : Screen("login")

    // Главный экран (список дисциплин)
    data object Home : Screen("home")

    // В будущем добавим:
    // data object Topics : Screen("topics/{disciplineId}")
}