package com.example.lawnavigator.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lawnavigator.presentation.home.HomeScreen
import com.example.lawnavigator.presentation.login.LoginScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.lawnavigator.presentation.topics.TopicsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- 1. ЭКРАН ВХОДА ---
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    // Переходим на Home и "забываем" экран Login (удаляем из стека)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // --- 2. ГЛАВНЫЙ ЭКРАН (Список дисциплин) ---
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTopics = { disciplineId ->
                    // Пока просто пишем в лог, так как экрана тем еще нет
                    Log.d("Navigation", "Переход к темам дисциплины ID: $disciplineId")

                    // В будущем тут будет:
                    navController.navigate(Screen.Topics.createRoute(disciplineId))
                }
            )
        }

        // Topics (Новый экран)
        composable(
            route = Screen.Topics.route,
            arguments = listOf(
                navArgument("disciplineId") { type = NavType.IntType }
            )
        ) {
            TopicsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLecture = { topicId ->
                    println("Переход к лекции: $topicId") // Заглушка
                }
            )
        }
    }
}