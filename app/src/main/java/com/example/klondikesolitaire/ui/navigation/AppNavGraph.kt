package com.example.klondikesolitaire.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.klondikesolitaire.ui.screens.GameEntryMode
import com.example.klondikesolitaire.ui.screens.GameScreen
import com.example.klondikesolitaire.ui.screens.HomeScreen
import com.example.klondikesolitaire.ui.screens.SettingsScreen
import com.example.klondikesolitaire.ui.screens.SplashScreen
import com.example.klondikesolitaire.ui.screens.StatsScreen
import com.example.klondikesolitaire.ui.screens.ThemesScreen
import com.example.klondikesolitaire.viewmodel.AppViewModel

@Composable
fun AppNavGraph(
    appViewModel: AppViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                appViewModel = appViewModel,
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                appViewModel = appViewModel,
                onPlay = { navController.navigate(Routes.GAME_NEW) },
                onContinue = { navController.navigate(Routes.GAME_CONTINUE) },
                onThemes = { navController.navigate(Routes.THEMES) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onStats = { navController.navigate(Routes.STATS) }
            )
        }

        composable(Routes.GAME_NEW) {
            GameScreen(
                entryMode = GameEntryMode.ForceNew,
                onGoHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.GAME_CONTINUE) {
            GameScreen(
                entryMode = GameEntryMode.ContinueOrNew,
                onGoHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.THEMES) {
            ThemesScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
