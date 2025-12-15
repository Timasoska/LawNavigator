package com.example.lawnavigator.presentation.navigation

sealed class Screen(val route: String) {
    // Экран входа
    data object Login : Screen("login")

    // Главный экран (список дисциплин)
    data object Home : Screen("home")

    // Маршрут с параметром
    data object Topics : Screen("topics/{disciplineId}") {
        fun createRoute(disciplineId: Int) = "topics/$disciplineId"
    }

    data object Lecture : Screen("lecture/{lectureId}") { fun createRoute(id: Int) = "lecture/$id" }

    data object Test : Screen("test/{topicId}") { fun createRoute(id: Int) = "test/$id" }

    data object Profile : Screen("profile")

    data object Search : Screen("search")

    data object Favorites : Screen("favorites") // <--- ДОБАВИТЬ ЭТО

    data object Leaderboard : Screen("leaderboard") // <--- Добавь
    // Новый экран списка лекций
    data object LecturesList : Screen("lectures_list/{topicId}") {
        fun createRoute(topicId: Int) = "lectures_list/$topicId"
    }

}