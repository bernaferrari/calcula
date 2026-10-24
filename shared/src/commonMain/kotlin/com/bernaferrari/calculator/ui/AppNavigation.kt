package com.bernaferrari.calculator.ui

import com.bernaferrari.calculator.ui.settings.CustomizeKeysScreen
import com.bernaferrari.calculator.ui.programmer.ProgrammerScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.bernaferrari.calculator.ui.about.AboutScreen
import com.bernaferrari.calculator.ui.calculator.CalculatorScreen
import com.bernaferrari.calculator.ui.graph.GraphScreen
import com.bernaferrari.calculator.ui.converter.ConverterScreen
import com.bernaferrari.calculator.ui.settings.SettingsScreen

/** The app's places. The calculator is always at the bottom of the stack. */
enum class Screen { Calculator, Settings, Converter, About, Graph, Programmer, CustomizeKeys }

/**
 * Navigation 3 with a shared-transition scope, so the tool row's icons can grow into the screens
 * they open (an M3 container transform) and shrink back on back, predictive back included.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberSaveable(
        saver = listSaver(save = { stack -> stack.map { it.name } }, restore = { names -> mutableStateListOf(*names.map(Screen::valueOf).toTypedArray()) }),
    ) { mutableStateListOf(Screen.Calculator) }
    val open: (Screen) -> Unit = { screen -> if (backStack.lastOrNull() != screen) backStack.add(screen) }
    val back: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    // The calculator hands its current number to the converter, and the converter hands one back.
    var converterSeed by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingInsert by rememberSaveable { mutableStateOf<String?>(null) }

    SharedTransitionLayout(modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                onBack = back,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                sharedTransitionScope = this,
                // The container transform carries the movement; the screens themselves only cross-fade.
                // The screen underneath stays put while the container grows over it (and back).
                transitionSpec = { fadeIn(tween(120)) togetherWith ExitTransition.KeepUntilTransitionsFinished },
                popTransitionSpec = { EnterTransition.None togetherWith fadeOut(tween(200, delayMillis = 120)) },
                predictivePopTransitionSpec = { fadeIn(tween(220)) togetherWith (fadeOut(tween(220)) + scaleOut(targetScale = 0.9f)) },
                entryProvider = entryProvider {
                    entry<Screen>(Screen.Calculator) {
                        CalculatorScreen(
                            initialExpression = "",
                            onOpenSettings = { open(Screen.Settings) },
                            onOpenConverter = { seed -> converterSeed = seed; open(Screen.Converter) },
                            onOpenGraph = { open(Screen.Graph) },
                            onOpenProgrammer = { open(Screen.Programmer) },
                            pendingInsert = pendingInsert,
                            onInsertConsumed = { pendingInsert = null },
                        )
                    }
                    entry<Screen>(Screen.Settings) {
                        SettingsScreen(
                            onNavigateBack = back,
                            onOpenAbout = { open(Screen.About) },
                            onOpenCustomizeKeys = { open(Screen.CustomizeKeys) },
                        )
                    }
                    entry<Screen>(Screen.Graph) {
                        GraphScreen(onNavigateBack = back)
                    }
                    entry<Screen>(Screen.CustomizeKeys) {
                        CustomizeKeysScreen(onNavigateBack = back)
                    }
                    entry<Screen>(Screen.Programmer) {
                        ProgrammerScreen(onNavigateBack = back)
                    }
                    entry<Screen>(Screen.About) {
                        AboutScreen(onNavigateBack = back)
                    }
                    entry<Screen>(Screen.Converter) {
                        ConverterScreen(
                            seed = converterSeed,
                            onNavigateBack = back,
                            onUse = { value -> pendingInsert = value; back() },
                        )
                    }
                },
            )
        }
    }
}
