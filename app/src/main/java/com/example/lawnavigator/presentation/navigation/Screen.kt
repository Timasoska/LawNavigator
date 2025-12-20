package com.example.lawnavigator.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Topics : Screen("topics/{disciplineId}") {
        fun createRoute(disciplineId: Int) = "topics/$disciplineId"
    }
    data object Lecture : Screen("lecture/{lectureId}") {
        fun createRoute(id: Int) = "lecture/$id"
    }
    data object Profile : Screen("profile")
    data object Search : Screen("search")
    data object Favorites : Screen("favorites")
    data object Leaderboard : Screen("leaderboard")
    data object LecturesList : Screen("lectures_list/{topicId}") {
        fun createRoute(topicId: Int) = "lectures_list/$topicId"
    }
    data object Test : Screen("test?topicId={topicId}&lectureId={lectureId}") {
        fun createRoute(topicId: Int? = null, lectureId: Int? = null): String =
            if (topicId != null) "test?topicId=$topicId" else "test?lectureId=$lectureId"
    }
    data object TestCreator : Screen("test_creator?topicId={topicId}&lectureId={lectureId}") {
        fun createRoute(topicId: Int? = null, lectureId: Int? = null): String =
            if (topicId != null) "test_creator?topicId=$topicId" else "test_creator?lectureId=$lectureId"
    }
    data object TeacherGroups : Screen("teacher_groups")
    data object GroupAnalytics : Screen("group_analytics/{groupId}") {
        fun createRoute(groupId: Int) = "group_analytics/$groupId"
    }

    // Использование Uri.encode предотвращает падение при передаче сложных строк в URL
    data object DisciplineDetails : Screen("discipline_details/{disciplineId}/{disciplineName}") {
        fun createRoute(id: Int, name: String) = "discipline_details/$id/${Uri.encode(name)}"
    }

    data object StudentReport : Screen("student_report/{groupId}/{studentId}") {
        fun createRoute(groupId: Int, studentId: Int) = "student_report/$groupId/$studentId"
    }
}