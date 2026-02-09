package org.solovyev.android.views.dragbutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.time.TimeSource

/**
 * Selection state for the popover
 */
private enum class PopoverSelection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    CENTER,  // Main action (click)
    CANCEL   // User dragged outside or to cancel zone
}

/**
 * A button that shows a popover on long-press with directional selection options.
 * 
 * Features:
 * - Tap → normal click action
 * - Long-press → shows popover with directional options
 * - Finger position highlights the corresponding option
 * - Smooth transitions between selections with haptic feedback
 * - Visual cancel zone indicator at bottom
 * - Button dims when popover is shown for focus
 *
 * @param text The main button text.
 * @param onClick Callback invoked when clicked (tap or center selection).
 * @param onDirectionSelected Callback invoked when a direction is selected.
 * @param modifier Modifier for the button.
 * @param directionTexts Map of direction to text/action configuration.
 * @param textStyle Style for the main text.
 * @param enabled Whether the button is enabled.
 * @param vibrateOnSelection Whether to vibrate on selection changes.
 * @param contentColor Color for the main content.
 * @param backgroundColor Background color of the button.
 */
@Composable
fun PopoverDragButton(
    text: String,
    onClick: () -> Unit,
    onDirectionSelected: (DragDirection) -> Unit,
    modifier: Modifier = Modifier,
    directionTexts: Map<DragDirection, DirectionTextConfig> = emptyMap(),
    textStyle: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true,
    vibrateOnSelection: Boolean = true,
    contentColor: Color = LocalContentColor.current,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current
    val longPressTimeout = viewConfig.longPressTimeoutMillis
    val touchSlop = viewConfig.touchSlop
    
    // State
    var isPressed by remember { mutableStateOf(false) }
    var showPopover by remember { mutableStateOf(false) }
    var currentSelection by remember { mutableStateOf(PopoverSelection.CENTER) }
    var previousSelection by remember { mutableStateOf(PopoverSelection.CENTER) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }
    var popoverAnchorOffset by remember { mutableStateOf(Offset.Zero) }
    var selectionChangeCount by remember { mutableIntStateOf(0) }
    
    // Haptic feedback when selection changes
    LaunchedEffect(currentSelection) {
        if (showPopover && currentSelection != previousSelection) {
            if (vibrateOnSelection && currentSelection != PopoverSelection.CANCEL) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            previousSelection = currentSelection
            selectionChangeCount++
        }
    }
    
    // Animations
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !showPopover) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )
    
    val buttonDimAlpha by animateFloatAsState(
        targetValue = if (showPopover) 0.5f else 1f,
        animationSpec = tween(150),
        label = "buttonDimAlpha"
    )
    
    Box(
        modifier = modifier
            .scale(pressScale)
            .graphicsLayer { alpha = buttonDimAlpha }
            .background(backgroundColor)
            .onSizeChanged { buttonSize = it }
            .pointerInput(enabled, onClick, onDirectionSelected, directionTexts) {
                if (!enabled) return@pointerInput
                
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val start = down.position
                    val downMark = TimeSource.Monotonic.markNow()
                    var lastPosition = start
                    var movedBeyondSlop = false
                    var longPressFired = false
                    
                    // Store anchor for popover positioning
                    popoverAnchorOffset = Offset(
                        x = buttonSize.width / 2f,
                        y = buttonSize.height / 2f
                    )
                    
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            
                            if (change.changedToUpIgnoreConsumed()) {
                                if (showPopover) {
                                    // Handle popover selection
                                    when (currentSelection) {
                                        PopoverSelection.UP -> {
                                            if (directionTexts[DragDirection.up]?.text?.isNotEmpty() == true) {
                                                onDirectionSelected(DragDirection.up)
                                                if (vibrateOnSelection) {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                        }
                                        PopoverSelection.DOWN -> {
                                            if (directionTexts[DragDirection.down]?.text?.isNotEmpty() == true) {
                                                onDirectionSelected(DragDirection.down)
                                                if (vibrateOnSelection) {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                        }
                                        PopoverSelection.LEFT -> {
                                            if (directionTexts[DragDirection.left]?.text?.isNotEmpty() == true) {
                                                onDirectionSelected(DragDirection.left)
                                                if (vibrateOnSelection) {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                        }
                                        PopoverSelection.RIGHT -> {
                                            if (directionTexts[DragDirection.right]?.text?.isNotEmpty() == true) {
                                                onDirectionSelected(DragDirection.right)
                                                if (vibrateOnSelection) {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                        }
                                        PopoverSelection.CENTER -> {
                                            onClick()
                                            if (vibrateOnSelection) {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                        PopoverSelection.CANCEL -> {
                                            // Do nothing, cancelled
                                        }
                                    }
                                } else if (!longPressFired) {
                                    // Normal tap
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onClick()
                                }
                                break
                            }
                            
                            if (change.positionChanged()) {
                                lastPosition = change.position
                                
                                if (!movedBeyondSlop) {
                                    movedBeyondSlop = (lastPosition - start).getDistance() > touchSlop
                                }
                                
                                // Update selection based on finger position
                                if (showPopover) {
                                    currentSelection = calculatePopoverSelection(
                                        fingerPosition = lastPosition,
                                        buttonCenter = popoverAnchorOffset,
                                        buttonSize = buttonSize,
                                        directionTexts = directionTexts
                                    )
                                }
                                
                                change.consume()
                            }
                            
                            // Check for long press
                            if (!longPressFired && 
                                !movedBeyondSlop &&
                                downMark.elapsedNow().inWholeMilliseconds >= longPressTimeout
                            ) {
                                longPressFired = true
                                showPopover = true
                                currentSelection = PopoverSelection.CENTER
                                previousSelection = PopoverSelection.CENTER
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    } finally {
                        isPressed = false
                        showPopover = false
                        currentSelection = PopoverSelection.CENTER
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Main button content
        Text(
            text = text,
            style = textStyle,
            color = contentColor
        )
        
        // Direction text hints (smaller, at corners)
        DirectionTextHints(
            directionTexts = directionTexts,
            textStyle = textStyle,
            contentColor = contentColor
        )
        
        // Popover
        if (showPopover) {
            EnhancedSelectionPopover(
                text = text,
                directionTexts = directionTexts,
                currentSelection = currentSelection,
                textStyle = textStyle
            )
        }
    }
}

/**
 * Calculate which popover section the finger is over.
 */
private fun calculatePopoverSelection(
    fingerPosition: Offset,
    buttonCenter: Offset,
    buttonSize: IntSize,
    directionTexts: Map<DragDirection, DirectionTextConfig>
): PopoverSelection {
    val relativeX = fingerPosition.x - buttonCenter.x
    val relativeY = fingerPosition.y - buttonCenter.y
    
    // Cancel zone: way below the button
    if (fingerPosition.y > buttonSize.height + 80) {
        return PopoverSelection.CANCEL
    }
    
    // Check if outside horizontal bounds
    val popoverHalfWidth = buttonSize.width * 2f
    if (kotlin.math.abs(relativeX) > popoverHalfWidth) {
        return PopoverSelection.CANCEL
    }
    
    // Determine selection based on position
    val threshold = buttonSize.width * 0.35f
    
    return when {
        // Up zone
        relativeY < -threshold && kotlin.math.abs(relativeX) < threshold * 2f -> {
            if (directionTexts[DragDirection.up]?.text?.isNotEmpty() == true) {
                PopoverSelection.UP
            } else {
                PopoverSelection.CENTER
            }
        }
        // Down zone (but not in cancel zone)
        relativeY > threshold && kotlin.math.abs(relativeX) < threshold * 2f -> {
            if (directionTexts[DragDirection.down]?.text?.isNotEmpty() == true) {
                PopoverSelection.DOWN
            } else {
                PopoverSelection.CENTER
            }
        }
        // Left zone
        relativeX < -threshold -> {
            if (directionTexts[DragDirection.left]?.text?.isNotEmpty() == true) {
                PopoverSelection.LEFT
            } else {
                PopoverSelection.CENTER
            }
        }
        // Right zone
        relativeX > threshold -> {
            if (directionTexts[DragDirection.right]?.text?.isNotEmpty() == true) {
                PopoverSelection.RIGHT
            } else {
                PopoverSelection.CENTER
            }
        }
        // Center zone
        else -> PopoverSelection.CENTER
    }
}

@Composable
private fun BoxScope.DirectionTextHints(
    directionTexts: Map<DragDirection, DirectionTextConfig>,
    textStyle: TextStyle,
    contentColor: Color
) {
    for (direction in DragDirection.entries) {
        val config = directionTexts[direction] ?: continue
        if (!config.visible || config.text.isEmpty()) continue
        
        val alignment = when (direction) {
            DragDirection.up -> Alignment.TopEnd
            DragDirection.down -> Alignment.BottomEnd
            DragDirection.left -> Alignment.CenterStart
            DragDirection.right -> Alignment.CenterEnd
        }
        
        Text(
            text = config.text,
            style = textStyle.copy(
                fontSize = textStyle.fontSize * config.scale
            ),
            color = contentColor.copy(alpha = config.alpha),
            modifier = Modifier
                .align(alignment)
                .padding(config.padding)
        )
    }
}

@Composable
private fun EnhancedSelectionPopover(
    text: String,
    directionTexts: Map<DragDirection, DirectionTextConfig>,
    currentSelection: PopoverSelection,
    textStyle: TextStyle
) {
    val popoverScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "popoverScale"
    )
    
    val popoverAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(100),
        label = "popoverAlpha"
    )
    
    Popup(
        alignment = Alignment.Center,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main popover content
            Surface(
                modifier = Modifier
                    .size(width = 200.dp, height = 180.dp)
                    .graphicsLayer {
                        scaleX = popoverScale
                        scaleY = popoverScale
                        alpha = popoverAlpha
                    }
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: UP option
                    EnhancedPopoverOption(
                        text = directionTexts[DragDirection.up]?.text ?: "",
                        isSelected = currentSelection == PopoverSelection.UP,
                        isAvailable = directionTexts[DragDirection.up]?.text?.isNotEmpty() == true,
                        textStyle = textStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    
                    // Middle: LEFT | CENTER | RIGHT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EnhancedPopoverOption(
                            text = directionTexts[DragDirection.left]?.text ?: "",
                            isSelected = currentSelection == PopoverSelection.LEFT,
                            isAvailable = directionTexts[DragDirection.left]?.text?.isNotEmpty() == true,
                            textStyle = textStyle,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        
                        // Center option with special styling
                        EnhancedPopoverOption(
                            text = text,
                            isSelected = currentSelection == PopoverSelection.CENTER,
                            isAvailable = true,
                            isCenter = true,
                            textStyle = textStyle,
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                        )
                        
                        EnhancedPopoverOption(
                            text = directionTexts[DragDirection.right]?.text ?: "",
                            isSelected = currentSelection == PopoverSelection.RIGHT,
                            isAvailable = directionTexts[DragDirection.right]?.text?.isNotEmpty() == true,
                            textStyle = textStyle,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    
                    // Bottom: DOWN option
                    EnhancedPopoverOption(
                        text = directionTexts[DragDirection.down]?.text ?: "",
                        isSelected = currentSelection == PopoverSelection.DOWN,
                        isAvailable = directionTexts[DragDirection.down]?.text?.isNotEmpty() == true,
                        textStyle = textStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            
            // Connecting stem/arrow
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(12.dp)
                    .graphicsLayer { alpha = popoverAlpha }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
            )
            
            // Cancel indicator
            if (currentSelection == PopoverSelection.CANCEL) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { alpha = popoverAlpha },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "Cancel",
                        style = textStyle.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedPopoverOption(
    text: String,
    isSelected: Boolean,
    isAvailable: Boolean,
    modifier: Modifier = Modifier,
    isCenter: Boolean = false,
    textStyle: TextStyle
) {
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected && isAvailable) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "optionBg"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected && isAvailable) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "optionScale"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = when {
            !isAvailable -> 0.25f
            isSelected -> 1f
            else -> 0.7f
        },
        animationSpec = tween(100),
        label = "contentAlpha"
    )
    
    val selectedColor = if (isCenter) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val selectedTextColor = if (isCenter) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    val normalTextColor = MaterialTheme.colorScheme.onSurface
    
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected && isAvailable) {
                    selectedColor.copy(alpha = backgroundAlpha)
                } else {
                    Color.Transparent
                }
            )
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                style = textStyle.copy(
                    fontSize = when {
                        isCenter -> textStyle.fontSize * 1.1f
                        else -> textStyle.fontSize * 0.75f
                    },
                    fontWeight = when {
                        isSelected && isAvailable -> FontWeight.Bold
                        isCenter -> FontWeight.Medium
                        else -> FontWeight.Normal
                    }
                ),
                color = when {
                    isSelected && isAvailable -> selectedTextColor
                    else -> normalTextColor
                }.copy(alpha = contentAlpha),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        } else if (!isAvailable) {
            // Show a subtle dot for empty/unavailable options
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(0.2f)
                    .background(
                        color = normalTextColor,
                        shape = CircleShape
                    )
            )
        }
    }
}
