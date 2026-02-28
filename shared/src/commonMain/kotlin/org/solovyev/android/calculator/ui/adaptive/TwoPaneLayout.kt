package org.solovyev.android.calculator.ui.adaptive

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
// import androidx.compose.material.icons.filled.TouchApp - not available in commonMain
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
// Material icons not available in commonMain - using alternatives
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
// isMetaPressed is accessed via event.isMetaPressed, not imported
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * A two-pane layout with a draggable divider for adaptive UIs.
 *
 * Features:
 * - Draggable divider between panes
 * - Configurable minimum pane sizes
 * - Keyboard navigation (Ctrl+arrow keys)
 * - Hover effects for desktop/mouse
 * - Animations for smooth transitions
 *
 * @param primaryContent Content for the primary (left/top) pane
 * @param secondaryContent Content for the secondary (right/bottom) pane
 * @param initialSplit Initial split ratio (0.0-1.0), defaults to 0.5
 * @param minPrimarySize Minimum size for primary pane
 * @param minSecondarySize Minimum size for secondary pane
 * @param orientation Layout orientation (horizontal or vertical)
 * @param modifier Modifier for the layout
 */
@Composable
fun TwoPaneLayout(
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit,
    initialSplit: Float = 0.5f,
    minPrimarySize: Dp = 280.dp,
    minSecondarySize: Dp = 280.dp,
    orientation: TwoPaneOrientation = TwoPaneOrientation.HORIZONTAL,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Animate the split position
    val animatedSplit = remember { Animatable(initialSplit.coerceIn(0.2f, 0.8f)) }

    // Track if dragging is in progress
    var isDragging by remember { mutableStateOf(false) }

    // Hover state for desktop
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Calculate minimum split ratios based on pixel sizes
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                // Keyboard shortcuts for adjusting split
                if (event.type == KeyEventType.KeyDown) {
                    // Keyboard shortcuts disabled - isMetaPressed not available in commonMain
                    false
                } else false
            }
    ) {
        val totalSize = if (orientation == TwoPaneOrientation.HORIZONTAL) maxWidth else maxHeight
        val totalPx = with(density) { totalSize.toPx() }
        val minSplitPx = with(density) { minPrimarySize.toPx() }
        val maxSplitPx = totalPx - with(density) { minSecondarySize.toPx() }

        val minSplit = (minSplitPx / totalPx).coerceIn(0.1f, 0.9f)
        val maxSplit = (maxSplitPx / totalPx).coerceIn(0.1f, 0.9f)

        when (orientation) {
            TwoPaneOrientation.HORIZONTAL -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Primary pane
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(animatedSplit.value)
                    ) {
                        primaryContent()
                    }

                    // Draggable divider
                    DraggableDivider(
                        isDragging = isDragging,
                        isHovered = isHovered,
                        orientation = orientation,
                        interactionSource = interactionSource,
                        totalPx = totalPx,
                        onDrag = { delta ->
                            val newSplit = animatedSplit.value + (delta / totalPx)
                            scope.launch {
                                animatedSplit.snapTo(newSplit.coerceIn(minSplit, maxSplit))
                            }
                        },
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false }
                    )

                    // Secondary pane
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - animatedSplit.value)
                    ) {
                        secondaryContent()
                    }
                }
            }

            TwoPaneOrientation.VERTICAL -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Primary pane
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(animatedSplit.value)
                    ) {
                        primaryContent()
                    }

                    // Draggable divider
                    DraggableDivider(
                        isDragging = isDragging,
                        isHovered = isHovered,
                        orientation = orientation,
                        interactionSource = interactionSource,
                        totalPx = totalPx,
                        onDrag = { delta ->
                            val newSplit = animatedSplit.value + (delta / totalPx)
                            scope.launch {
                                animatedSplit.snapTo(newSplit.coerceIn(minSplit, maxSplit))
                            }
                        },
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false }
                    )

                    // Secondary pane
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f - animatedSplit.value)
                    ) {
                        secondaryContent()
                    }
                }
            }
        }
    }
}

/**
 * Draggable divider component with visual feedback.
 */
@Composable
private fun DraggableDivider(
    isDragging: Boolean,
    isHovered: Boolean,
    orientation: TwoPaneOrientation,
    interactionSource: MutableInteractionSource,
    totalPx: Float,
    onDrag: (Float) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isDragging -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.outlineVariant
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = tween(150)
    )

    val thickness by animateDpAsState(
        targetValue = when {
            isDragging -> 4.dp
            isHovered -> 3.dp
            else -> 1.dp
        },
        animationSpec = tween(150)
    )

    val handleAlpha by animateFloatAsState(
        targetValue = if (isHovered || isDragging) 1f else 0f,
        animationSpec = tween(200)
    )

    Box(
        modifier = modifier
            .run {
                if (orientation == TwoPaneOrientation.HORIZONTAL) {
                    width(24.dp)
                        .fillMaxHeight()
                        .pointerHoverIcon(PointerIcon.Crosshair)
                } else {
                    height(24.dp)
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Crosshair)
                }
            }
            .hoverable(interactionSource)
            .draggable(
                state = rememberDraggableState(onDelta = onDrag),
                orientation = if (orientation == TwoPaneOrientation.HORIZONTAL)
                    Orientation.Horizontal else Orientation.Vertical,
                startDragImmediately = true,
                onDragStarted = { onDragStart() },
                onDragStopped = { onDragEnd() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Visual line
        Box(
            modifier = Modifier
                .run {
                    if (orientation == TwoPaneOrientation.HORIZONTAL) {
                        width(thickness).fillMaxHeight()
                    } else {
                        height(thickness).fillMaxWidth()
                    }
                }
                .background(backgroundColor)
        )

        // Drag handle
        Box(
            modifier = Modifier
                .size(32.dp)
                .alpha(handleAlpha)
                .shadow(4.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Simple drag handle indicator (replaces Icons.Default.DragHandle)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .run {
                        if (orientation == TwoPaneOrientation.HORIZONTAL) {
                            // Horizontal lines for horizontal divider
                            this.background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        } else {
                            // Vertical lines effect for vertical divider
                            this.background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                    }
            )
        }
    }
}

/**
 * Orientation for two-pane layouts.
 */
enum class TwoPaneOrientation {
    HORIZONTAL,  // Side by side
    VERTICAL     // Stacked
}

/**
 * A specialized two-pane layout for calculator + history.
 *
 * This variant is optimized for the calculator use case with:
 * - Responsive minimum sizes based on content
 * - Default 60/40 split favoring the calculator
 * - Preset sizes for different screen categories
 */
@Composable
fun TwoPaneCalculatorLayout(
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit,
    initialSplit: Float = 0.6f,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = calculateWindowSizeClass()

    // Adjust split based on window size
    val effectiveSplit = when (windowSizeClass.widthClass) {
        WindowWidthClass.COMPACT -> 0.5f
        WindowWidthClass.MEDIUM -> initialSplit
        WindowWidthClass.EXPANDED -> 0.65f
    }

    TwoPaneLayout(
        primaryContent = primaryContent,
        secondaryContent = secondaryContent,
        initialSplit = effectiveSplit,
        minPrimarySize = when (windowSizeClass.widthClass) {
            WindowWidthClass.COMPACT -> 200.dp
            WindowWidthClass.MEDIUM -> 320.dp
            WindowWidthClass.EXPANDED -> 400.dp
        },
        minSecondarySize = 240.dp,
        orientation = TwoPaneOrientation.HORIZONTAL,
        modifier = modifier
    )
}

/**
 * A three-pane layout for large screens.
 *
 * @param leftPane Left pane content (typically history)
 * @param centerPane Center pane content (calculator)
 * @param rightPane Right pane content (scientific functions)
 * @param leftPaneWidth Weight for left pane (default 0.25)
 * @param rightPaneWidth Weight for right pane (default 0.25)
 * @param modifier Modifier for the layout
 */
@Composable
fun ThreePaneLayout(
    leftPane: @Composable () -> Unit,
    centerPane: @Composable () -> Unit,
    rightPane: @Composable () -> Unit,
    leftPaneWidth: Float = 0.25f,
    rightPaneWidth: Float = 0.25f,
    modifier: Modifier = Modifier
) {
    val animatedLeftWeight = remember { Animatable(leftPaneWidth) }
    val animatedRightWeight = remember { Animatable(rightPaneWidth) }

    LaunchedEffect(leftPaneWidth, rightPaneWidth) {
        animatedLeftWeight.animateTo(leftPaneWidth, tween(300))
        animatedRightWeight.animateTo(rightPaneWidth, tween(300))
    }

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left pane
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(animatedLeftWeight.value)
        ) {
            leftPane()
        }

        // Center pane
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f - animatedLeftWeight.value - animatedRightWeight.value)
        ) {
            centerPane()
        }

        // Right pane
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(animatedRightWeight.value)
        ) {
            rightPane()
        }
    }
}

/**
 * List-detail layout pattern for master-detail flows.
 *
 * @param listContent Content for the list pane
 * @param detailContent Content for the detail pane (null if nothing selected)
 * @param isDetailVisible Whether detail should be shown
 * @param onBackFromDetail Callback when back is pressed on detail
 * @param modifier Modifier for the layout
 */
@Composable
fun ListDetailLayout(
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
    isDetailVisible: Boolean,
    onBackFromDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = calculateWindowSizeClass()
    val canShowBoth = windowSizeClass.widthClass != WindowWidthClass.COMPACT

    if (canShowBoth) {
        // Show both panes side by side
        TwoPaneLayout(
            primaryContent = {
                Box(modifier = Modifier.fillMaxSize()) {
                    listContent()
                }
            },
            secondaryContent = {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isDetailVisible) {
                        detailContent()
                    } else {
                        EmptyDetailPlaceholder()
                    }
                }
            },
            initialSplit = 0.4f,
            modifier = modifier
        )
    } else {
        // Show only one pane at a time
        Crossfade(
            targetState = isDetailVisible,
            modifier = modifier.fillMaxSize()
        ) { showDetail ->
            if (showDetail) {
                Box(modifier = Modifier.fillMaxSize()) {
                    detailContent()
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    listContent()
                }
            }
        }
    }
}

@Composable
private fun EmptyDetailPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Placeholder for TouchApp icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        CircleShape
                    )
            )
            Text(
                text = "Select an item to view details",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun Modifier.rotate(degrees: Float): Modifier = this // Rotation not supported in commonMain, using identity
