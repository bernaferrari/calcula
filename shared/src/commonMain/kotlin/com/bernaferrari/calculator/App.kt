package com.bernaferrari.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bernaferrari.calculator.ui.calculator.LocalReduceMotion
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.ui.AppNavigation
import com.bernaferrari.calculator.ui.theme.CalculatorTheme
import com.bernaferrari.calculator.ui.theme.ThemeViewModel

/** The whole app, the same on Android, iOS and the web. */
@Composable
fun App() {
    val theme: ThemeViewModel = viewModel { ThemeViewModel() }
    val reduceMotion by theme.reduceMotion.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        CalculatorTheme(
            darkTheme = theme.shouldUseDarkTheme(),
            palette = theme.palette(),
            colorStyle = theme.colorStyle(),
        ) {
            // Every screen lays itself out for the window it gets: phone, phone on its side, tablet,
            // unfolded foldable or desktop browser.
            AppNavigation(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
        }
    }
}
