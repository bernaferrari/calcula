package com.bernaferrari.calculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bernaferrari.calculator.ui.calculator.ModernCalculatorScreen
import com.bernaferrari.calculator.ui.graphing.GraphingScreen
import com.bernaferrari.calculator.ui.history.HistoryScreen
import com.bernaferrari.calculator.ui.settings.SettingsScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "calculator",
        modifier = modifier,
    ) {
        composable(
            route = "calculator?expression={expression}",
            arguments =
                listOf(
                    navArgument("expression") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
        ) { backStackEntry ->
            val expression = backStackEntry.arguments?.getString("expression") ?: ""
            ModernCalculatorScreen(
                initialExpression = expression,
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToGraph = { navController.navigate("graphing") },
            )
        }

        composable("history") {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onUseExpression = { expression ->
                    navController.navigate("calculator?expression=$expression") {
                        popUpTo("calculator") { inclusive = true }
                    }
                },
            )
        }

        composable("graphing") {
            GraphingScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
