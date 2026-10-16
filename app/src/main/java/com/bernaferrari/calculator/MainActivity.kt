package com.bernaferrari.calculator

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bernaferrari.calculator.ui.AppNavigation
import com.bernaferrari.calculator.ui.theme.CalculatorTheme
import com.bernaferrari.calculator.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        window.colorMode = ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()

            CalculatorTheme(
                darkTheme = themeViewModel.shouldUseDarkTheme(),
                dynamicColor = themeViewModel.shouldUseDynamicColor(),
            ) {
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
