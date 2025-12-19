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
import com.example.lawnavigator.presentation.profile.ProfileScreen
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

        // Topics
        composable(
            route = Screen.Topics.route,
            arguments = listOf(
                navArgument("disciplineId") { type = NavType.IntType }
            )
        ) {
            TopicsScreen(
                onNavigateBack = { navController.popBackStack() },

                // ВАЖНО: Теперь мы идем не в Lecture, а в LecturesList!
                onNavigateToLecture = { topicId ->
                    navController.navigate(Screen.LecturesList.createRoute(topicId))
                },

                onNavigateToTest = { topicId ->
                    navController.navigate(Screen.Test.createRoute(topicId))
                },

                onNavigateToCreateTest = { topicId ->
                    navController.navigate(Screen.TestCreator.createRoute(topicId))
                }
            )
        }

        // Test
        composable(
            route = Screen.Test.route, // Убедись, что Screen.Test.route = "test?topicId={topicId}&lectureId={lectureId}"
            arguments = listOf(
                // Мы должны явно указать оба аргумента, чтобы навигация их распознала
                navArgument("topicId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("lectureId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            // Hilt ViewModel сама достанет аргументы
            com.example.lawnavigator.presentation.test.TestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Lecture
        composable(
            route = Screen.Lecture.route,
            arguments = listOf(
                navArgument(name = "lectureId") { type = NavType.IntType },
                navArgument(name = "searchQuery") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // Получаем значение аргумента
            val queryArgument = backStackEntry.arguments?.getString("searchQuery")

            LectureScreen(
                searchQuery = queryArgument, // <--- ДОБАВЬ ЭТУ СТРОКУ
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTest = { lectureId ->
                    navController.navigate(route = Screen.Test.createRoute(lectureId = lectureId))
                }
            )
        }

        // --- 5. ПРОФИЛЬ ---
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                // 4. РЕАЛИЗУЕМ ПЕРЕХОД ПО РЕКОМЕНДАЦИИ
                onNavigateToTopic = { topicId ->
                    // Здесь мы используем topicId.
                    // Если у темы одна лекция, можно переходить сразу на Screen.Lecture.
                    // Если много - лучше на Screen.Topics или сделать Screen.LecturesList.
                    // Для простоты пока переходим на лекцию (предполагаем 1 к 1 или заглушку)
                    navController.navigate(Screen.Lecture.createRoute(topicId))
                }
            )
        }

        composable(Screen.Search.route) {
            com.example.lawnavigator.presentation.search.SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                // Теперь здесь (Int, String) -> Unit, как и ожидает SearchScreen
                onNavigateToLecture = { lectureId ->
                    navController.navigate(Screen.Lecture.createRoute(lectureId))
                }
            )
        }

        // ЭКРАН ИЗБРАННОГО (НОВЫЙ БЛОК)
        composable(Screen.Favorites.route) {
            com.example.lawnavigator.presentation.favorites.FavoritesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLecture = { lectureId ->
                    navController.navigate(Screen.Lecture.createRoute(lectureId))
                }
            )
        }
        // LeaderboardScreen
        composable(Screen.Leaderboard.route) {
            com.example.lawnavigator.presentation.leaderboard.LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- НОВЫЙ БЛОК: Список лекций ---
        composable(
            route = Screen.LecturesList.route,
            arguments = listOf(navArgument("topicId") { type = NavType.IntType })
        ) {
            com.example.lawnavigator.presentation.lectures_list.LecturesListScreen(
                onNavigateBack = { navController.popBackStack() },

                onNavigateToLecture = { lectureId ->
                    navController.navigate(Screen.Lecture.createRoute(lectureId))
                },

                // --- ДОБАВЛЯЕМ ПРОПУЩЕННЫЕ КОЛБЭКИ ---

                // 1. Пройти тест по лекции
                onNavigateToTest = { lectureId ->
                    navController.navigate(Screen.Test.createRoute(lectureId = lectureId))
                },

                // 2. Создать тест по лекции (для учителя)
                onNavigateToCreateTest = { lectureId ->
                    navController.navigate(Screen.TestCreator.createRoute(lectureId = lectureId))
                }
            )
        }


        composable(
            route = Screen.TestCreator.route,
            arguments = listOf(
                // Теперь принимаем ДВА необязательных аргумента
                navArgument("topicId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("lectureId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            com.example.lawnavigator.presentation.test_creator.TestCreatorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Topics.route,
            arguments = listOf(
                navArgument("disciplineId") { type = NavType.IntType }
            )
        ) {
            TopicsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLecture = { topicId ->
                    navController.navigate(Screen.LecturesList.createRoute(topicId))
                },
                onNavigateToTest = { topicId ->
                    navController.navigate(Screen.Test.createRoute(topicId))
                },
                // Прокидываем навигацию в конструктор
                onNavigateToCreateTest = { topicId ->
                    navController.navigate(Screen.TestCreator.createRoute(topicId)) // Создаем маршрут с ID
                }
            )
        }
        composable(Screen.TeacherGroups.route) {
            com.example.lawnavigator.presentation.teacher_groups.TeacherGroupsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAnalytics = { groupId ->
                    // Переход к аналитике (следующий шаг)
                    // navController.navigate(Screen.GroupAnalytics.createRoute(groupId))
                }
            )
        }
        // HOME SCREEN
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTopics = { disciplineId ->
                    navController.navigate(Screen.Topics.createRoute(disciplineId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                // --- НОВЫЙ КОЛБЭК ---
                onNavigateToTeacherGroups = {
                    navController.navigate(Screen.TeacherGroups.route)
                }
            )
        }

    }
}