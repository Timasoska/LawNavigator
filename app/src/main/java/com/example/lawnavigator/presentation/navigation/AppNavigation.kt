package com.example.lawnavigator.presentation.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lawnavigator.presentation.home.HomeScreen
import com.example.lawnavigator.presentation.lecture.LectureScreen
import com.example.lawnavigator.presentation.login.LoginScreen
import com.example.lawnavigator.presentation.topics.TopicsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Анимация по умолчанию для всех экранов:
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
    ) {
        // Login (без анимации при старте)
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTopics = { disciplineId ->
                    navController.navigate(Screen.Topics.createRoute(disciplineId))
                }
            )
        }

        // Topics
        composable(
            route = Screen.Topics.route,
            arguments = listOf(navArgument("disciplineId") { type = NavType.IntType })
        ) {
            TopicsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLecture = { topicId ->
                    // Временная заглушка, пока нет экрана Лекции, или переход, если уже сделал
                    navController.navigate(Screen.Lecture.createRoute(topicId))
                }
            )
        }

        // Lecture
        composable(
            route = Screen.Lecture.route,
            arguments = listOf(navArgument("lectureId") { type = NavType.IntType })
        ) {
            LectureScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}