package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Material 3 Floating Toolbar
 *
 * A floating action bar that can be placed above the keyboard.
 * Supports both horizontal and vertical layouts, and standard/vibrant color schemes.
 */
@Composable
fun FloatingToolbar(
    items: List<FloatingToolbarItem>,
    modifier: Modifier = Modifier,
    layout: FloatingToolbarLayout = FloatingToolbarLayout.HORIZONTAL,
    colorScheme: FloatingToolbarColor = FloatingToolbarColor.STANDARD,
    expanded: Boolean = true
) {
    val containerColor = when (colorScheme) {
        FloatingToolbarColor.STANDARD -> MaterialTheme.colorScheme.surfaceContainerHigh
        FloatingToolbarColor.VIBRANT -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when (colorScheme) {
        FloatingToolbarColor.STANDARD -> MaterialTheme.colorScheme.onSurface
        FloatingToolbarColor.VIBRANT -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = containerColor,
            shadowElevation = 6.dp,
            tonalElevation = 2.dp,
            modifier = Modifier.padding(8.dp)
        ) {
            when (layout) {
                FloatingToolbarLayout.HORIZONTAL -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        items.forEach { item ->
                            FloatingToolbarButton(
                                item = item,
                                contentColor = contentColor
                            )
                        }
                    }
                }
                FloatingToolbarLayout.VERTICAL -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        items.forEach { item ->
                            FloatingToolbarButton(
                                item = item,
                                contentColor = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingToolbarButton(
    item: FloatingToolbarItem,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = item.onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = contentColor
        ),
        modifier = modifier.size(48.dp)
    ) {
        if (item.icon != null) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = item.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
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
    onCursorLeft: () -> Unit,
    onCursorRight: () -> Unit,
    onFunctions: () -> Unit,
    onConverter: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    layout: FloatingToolbarLayout = FloatingToolbarLayout.HORIZONTAL,
    colorScheme: FloatingToolbarColor = FloatingToolbarColor.STANDARD,
    expanded: Boolean = true
) {
    val items = listOf(
        FloatingToolbarItem(
            label = "<",
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            onClick = onCursorLeft
        ),
        FloatingToolbarItem(
            label = ">",
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            onClick = onCursorRight
        ),
        FloatingToolbarItem(
            label = "f",
            icon = Icons.Default.Functions,
            onClick = onFunctions
        ),
        FloatingToolbarItem(
            label = "Convert",
            icon = Icons.Default.SwapHoriz,
            onClick = onConverter
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
