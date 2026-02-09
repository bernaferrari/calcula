@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Layout orientation for the floating toolbar
 */
enum class FloatingToolbarLayout {
    HORIZONTAL,
    VERTICAL
}

/**
 * Color scheme for the floating toolbar
 */
enum class FloatingToolbarColor {
    STANDARD,
    VIBRANT
}

/**
 * Data class for toolbar items
 */
data class FloatingToolbarItem(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

/**
 * Material 3 Expressive Floating Toolbar
 *
 * Uses the official Material 3 implementations.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingToolbar(
    items: List<FloatingToolbarItem>,
    modifier: Modifier = Modifier,
    layout: FloatingToolbarLayout = FloatingToolbarLayout.HORIZONTAL,
    colorScheme: FloatingToolbarColor = FloatingToolbarColor.STANDARD,
    expanded: Boolean = true
) {
    val colors = when (colorScheme) {
        FloatingToolbarColor.STANDARD -> FloatingToolbarDefaults.standardFloatingToolbarColors()
        FloatingToolbarColor.VIBRANT -> FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    }

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        when (layout) {
            FloatingToolbarLayout.HORIZONTAL -> {
                HorizontalFloatingToolbar(
                    expanded = true,
                    colors = colors,
                    modifier = Modifier.padding(8.dp),
                    content = {
                        items.forEach { item ->
                            FloatingToolbarButton(item)
                        }
                    }
                )
            }
            FloatingToolbarLayout.VERTICAL -> {
                VerticalFloatingToolbar(
                    expanded = true,
                    colors = colors,
                    modifier = Modifier.padding(8.dp),
                    content = {
                        items.forEach { item ->
                            FloatingToolbarButton(item)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FloatingToolbarButton(
    item: FloatingToolbarItem,
    modifier: Modifier = Modifier
) {
    if (item.icon != null) {
        IconButton(
            onClick = item.onClick,
            modifier = modifier.size(48.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp)
            )
        }
    } else {
        TextButton(
            onClick = item.onClick,
            modifier = modifier.height(48.dp)
        ) {
            Text(
                text = item.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Calculator-specific floating toolbar with advanced operations
 *
 * Contains: Cursor Left (<), Cursor Right (>),
 * Functions (f), Converter, History, and Settings
 */
@Composable
fun CalculatorFloatingToolbar(
    onFunctions: () -> Unit,
    onConverter: () -> Unit,
    onGraph: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    layout: FloatingToolbarLayout = FloatingToolbarLayout.HORIZONTAL,
    colorScheme: FloatingToolbarColor = FloatingToolbarColor.STANDARD,
    expanded: Boolean = true
) {
    val items = listOf(
        FloatingToolbarItem(
            label = "f(x)",
            onClick = onFunctions
        ),
        FloatingToolbarItem(
            label = "Convert",
            icon = Icons.Default.SwapHoriz,
            onClick = onConverter
        ),
        FloatingToolbarItem(
            label = "Graph",
            icon = Icons.Default.ShowChart,
            onClick = onGraph
        ),
        FloatingToolbarItem(
            label = "History",
            icon = Icons.Default.History,
            onClick = onHistory
        ),
        FloatingToolbarItem(
            label = "Settings",
            icon = Icons.Default.Settings,
            onClick = onSettings
        )
    )

    FloatingToolbar(
        items = items,
        layout = layout,
        colorScheme = colorScheme,
        expanded = expanded,
        modifier = modifier
    )
}

/**
 * Collapsible floating toolbar with expand/collapse FAB
 */
@Composable
fun CollapsibleFloatingToolbar(
    items: List<FloatingToolbarItem>,
    modifier: Modifier = Modifier,
    layout: FloatingToolbarLayout = FloatingToolbarLayout.HORIZONTAL,
    colorScheme: FloatingToolbarColor = FloatingToolbarColor.STANDARD,
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation"
    )

    when (layout) {
        FloatingToolbarLayout.HORIZONTAL -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = modifier
            ) {
                FloatingToolbar(
                    items = items,
                    layout = layout,
                    colorScheme = colorScheme,
                    expanded = expanded
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    containerColor = when (colorScheme) {
                        FloatingToolbarColor.STANDARD -> MaterialTheme.colorScheme.primaryContainer
                        FloatingToolbarColor.VIBRANT -> MaterialTheme.colorScheme.primary
                    },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = if (expanded) "Collapse toolbar" else "Expand toolbar",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
        FloatingToolbarLayout.VERTICAL -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = modifier
            ) {
                FloatingToolbar(
                    items = items,
                    layout = layout,
                    colorScheme = colorScheme,
                    expanded = expanded
                )

                Spacer(modifier = Modifier.height(8.dp))

                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    containerColor = when (colorScheme) {
                        FloatingToolbarColor.STANDARD -> MaterialTheme.colorScheme.primaryContainer
                        FloatingToolbarColor.VIBRANT -> MaterialTheme.colorScheme.primary
                    },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = if (expanded) "Collapse toolbar" else "Expand toolbar",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    }
}
