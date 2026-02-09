package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Window width breakpoints for adaptive layout.
 * Based on Material 3 guidelines.
 */
enum class WindowWidthClass {
    COMPACT,   // < 600dp (phones)
    MEDIUM,    // 600dp - 840dp (small tablets, foldables)
    EXPANDED   // > 840dp (large tablets, desktops)
}

/**
 * Calculates the window width class based on current constraints.
 */
@Composable
fun calculateWindowWidthClass(widthDp: Int): WindowWidthClass {
    return when {
        widthDp < 600 -> WindowWidthClass.COMPACT
        widthDp < 840 -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.EXPANDED
    }
}

/**
 * Adaptive layout wrapper for Calculator++.
 * 
 * - COMPACT: Single column (current layout)
 * - MEDIUM/EXPANDED: Two-column split (side panel + calculator)
 * 
 * @param sidePanel Content to show in side panel (history, variables, etc.)
 * @param mainContent The main calculator content
 */
@Composable
fun AdaptiveCalculatorLayout(
    sidePanel: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthClass = calculateWindowWidthClass(maxWidth.value.toInt())
        
        when (widthClass) {
            WindowWidthClass.COMPACT -> {
                // Single column - just show main content
                mainContent()
            }
            WindowWidthClass.MEDIUM, WindowWidthClass.EXPANDED -> {
                // Two-column layout
                Row(modifier = Modifier.fillMaxSize()) {
                    // Side panel (history/variables) - 35% width
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .fillMaxHeight()
                            .padding(end = 8.dp)
                    ) {
                        sidePanel()
                    }
                    
                    // Main calculator - remaining width
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        mainContent()
                    }
                }
            }
        }
    }
}
