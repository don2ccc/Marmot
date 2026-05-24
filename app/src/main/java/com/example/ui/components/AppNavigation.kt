package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AnalyzeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhraseBookScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.PhraseBuddyViewModel

object AppRoutes {
    const val HOME = "home"
    const val ANALYZE = "analyze"
    const val PHRASE_BOOK = "phrase_book"
    const val QUIZ = "quiz"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(
    viewModel: PhraseBuddyViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAnalyze = { navController.navigate(AppRoutes.ANALYZE) },
                onNavigateToPhraseBook = { navController.navigate(AppRoutes.PHRASE_BOOK) },
                onNavigateToQuiz = { navController.navigate(AppRoutes.QUIZ) },
                onNavigateToSettings = { navController.navigate(AppRoutes.SETTINGS) }
            )
        }

        composable(AppRoutes.ANALYZE) {
            AnalyzeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PHRASE_BOOK) {
            PhraseBookScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.QUIZ) {
            QuizScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
