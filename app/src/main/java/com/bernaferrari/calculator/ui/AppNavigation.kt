package com.bernaferrari.calculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bernaferrari.calculator.ui.calculator.CalculatorScreen
import com.bernaferrari.calculator.ui.converter.ConverterScreen
import com.bernaferrari.calculator.ui.settings.SettingsScreen

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
                onOpenConverter = { navController.navigate("unit_converter") },
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = back,
                onOpenConverter = { navController.navigate("unit_converter") },
            )
        }
        composable("unit_converter") { ConverterScreen(onNavigateBack = back) }
    }
}
