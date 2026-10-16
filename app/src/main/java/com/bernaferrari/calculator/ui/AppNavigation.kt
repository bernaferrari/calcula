package com.bernaferrari.calculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bernaferrari.calculator.ui.calculator.CalculatorScreen
import com.bernaferrari.calculator.ui.converter.UnitConverterScreen
import com.bernaferrari.calculator.ui.programmer.ProgrammerModeDisplay
import com.bernaferrari.calculator.ui.settings.SettingsScreen
import com.bernaferrari.calculator.ui.statistics.StatisticsCalculator

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = "calculator", modifier = modifier) {
        composable(
            route = "calculator?expression={expression}",
            arguments = listOf(navArgument("expression") { type = NavType.StringType; defaultValue = "" }),
        ) { entry ->
            CalculatorScreen(
                initialExpression = entry.arguments?.getString("expression").orEmpty(),
                onOpenSettings = { navController.navigate("settings") },
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = back,
                onOpenConverter = { navController.navigate("unit_converter") },
                onOpenProgrammer = { navController.navigate("programmer") },
                onOpenStatistics = { navController.navigate("statistics") },
            )
        }
        composable("unit_converter") { UnitConverterScreen(onNavigateBack = back) }
        composable("programmer") { ProgrammerModeDisplay(onNavigateBack = back) }
        composable("statistics") { StatisticsCalculator(onNavigateBack = back) }
    }
}
