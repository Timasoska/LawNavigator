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

    data object Profile : Screen("profile")

    data object Search : Screen("search")

    data object Favorites : Screen("favorites") // <--- ДОБАВИТЬ ЭТО

    data object Leaderboard : Screen("leaderboard") // <--- Добавь
    // Новый экран списка лекций
    data object LecturesList : Screen("lectures_list/{topicId}") {
        fun createRoute(topicId: Int) = "lectures_list/$topicId"
    }

    data object Test : Screen("test?topicId={topicId}&lectureId={lectureId}") {
        // Аргументы теперь nullable (Int?) и имеют значение по умолчанию null
        fun createRoute(topicId: Int? = null, lectureId: Int? = null): String {
            return if (topicId != null) "test?topicId=$topicId"
            else "test?lectureId=$lectureId"
        }
    }

    data object TestCreator : Screen("test_creator?topicId={topicId}&lectureId={lectureId}") {
        fun createRoute(topicId: Int? = null, lectureId: Int? = null): String {
            return if (topicId != null) "test_creator?topicId=$topicId"
            else "test_creator?lectureId=$lectureId"
        }
    }
}