package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
    val icon: @Composable (() -> Unit)? = null,
    val onClick: () -> Unit,
    val onLongClick: (() -> Unit)? = null,
    val onDoubleClick: (() -> Unit)? = null
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
                    modifier = Modifier.padding(6.dp),
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
                    modifier = Modifier.padding(6.dp),
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
    val interactionSource = remember { MutableInteractionSource() }
    if (item.icon != null) {
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            modifier = modifier
                .size(48.dp)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = item.onClick,
                    onLongClick = item.onLongClick,
                    onDoubleClick = item.onDoubleClick
                )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                item.icon()
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color.Transparent,
            modifier = modifier
                .height(48.dp)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = item.onClick,
                    onLongClick = item.onLongClick,
                    onDoubleClick = item.onDoubleClick
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
            ) {
                Text(
                    text = item.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
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
    onPrevious: () -> Unit,
    onPreviousStart: () -> Unit,
    onNext: () -> Unit,
    onNextEnd: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onVariables: () -> Unit,
    onConverter: () -> Unit,
    onGraph: () -> Unit,
    modifier: Modifier = Modifier,
    layout: FloatingToolbarLayout = FloatingToolbarLayout.HORIZONTAL,
    colorScheme: FloatingToolbarColor = FloatingToolbarColor.STANDARD,
    expanded: Boolean = true
) {
    val items = listOf(
        FloatingToolbarItem(
            label = "Prev",
            icon = { Text(text = "←") },
            onClick = onPrevious,
            onLongClick = onCopy,
            onDoubleClick = onPreviousStart
        ),
        FloatingToolbarItem(
            label = "Next",
            icon = { Text(text = "→") },
            onClick = onNext,
            onLongClick = onPaste,
            onDoubleClick = onNextEnd
        ),
        FloatingToolbarItem(
            label = "Vars",
            icon = { Text(text = "∫") },
            onClick = onVariables
        ),
        FloatingToolbarItem(
            label = "Convert",
            icon = { Text(text = "⇄") },
            onClick = onConverter
        ),
        FloatingToolbarItem(
            label = "Graph",
            icon = { Text(text = "📊") },
            onClick = onGraph
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
                    Text(
                        text = "⋯",
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
                    Text(
                        text = "⋯",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    }
}
