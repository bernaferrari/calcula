package com.bernaferrari.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.ui.AppNavigation
import com.bernaferrari.calculator.ui.theme.CalculatorTheme
import com.bernaferrari.calculator.ui.theme.ThemeViewModel

/** The whole app, the same on Android, iOS and the web. */
@Composable
fun App() {
    val theme: ThemeViewModel = viewModel { ThemeViewModel() }
    CalculatorTheme(
        darkTheme = theme.shouldUseDarkTheme(),
        palette = theme.palette(),
        highContrast = theme.shouldUseHighContrast(),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
            // A wide window that isn't a phone on its side (a desktop browser, a tablet): the
            // calculator keeps a phone's proportions in a centred column instead of stretching.
            val wide = maxWidth > 600.dp && maxHeight >= 600.dp && maxWidth > maxHeight * 0.9f
            val columnWidth = min(maxHeight * 0.58f, 480.dp)
            if (wide) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppNavigation(
                        Modifier
                            .width(columnWidth)
                            .fillMaxHeight()
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(32.dp)),
                    )
                }
            } else {
                AppNavigation(Modifier.fillMaxSize())
            }
        }
    }
}
