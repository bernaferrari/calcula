package org.solovyev.android.calculator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.stringResource

/**
 * Represents a calculator tab with id and expression
 */
data class CalculatorTabData(
    val id: String,
    val expression: String,
    val result: String = ""
) {
    fun getLabel(maxLength: Int): String {
        return when {
            expression.isNotEmpty() -> middleEllipsize(expression, maxLength)
            result.isNotEmpty() -> middleEllipsize("= $result", maxLength)
            else -> "New"
        }
    }
}

/**
 * A horizontal scrollable tab bar for the calculator.
 * 
 * Shows all open tabs with their expression previews. Users can:
 * - Tap a tab to switch to it
 * - Tap the × button to close a tab
 * - Tap the + button to add a new tab
 *
 * @param tabs List of all calculator tabs
 * @param activeTabId ID of the currently active tab
 * @param onTabSelect Called when a tab is selected
 * @param onTabClose Called when a tab's close button is pressed
 * @param onAddTab Called when the add button is pressed
 * @param canAddMore Whether more tabs can be added (respecting max limit)
 * @param modifier Modifier for the tab bar
 */
@Composable
fun CalculatorTabBar(
    tabs: List<CalculatorTabData>,
    activeTabId: String,
    onTabSelect: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onAddTab: () -> Unit,
    onTabMove: (fromIndex: Int, toIndex: Int) -> Unit,
    canAddMore: Boolean = true,
    maxLabelLength: Int = 12,
    reorderThreshold: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                TabChip(
                    tab = tab,
                    index = index,
                    tabCount = tabs.size,
                    isActive = tab.id == activeTabId,
                    showCloseButton = tabs.size > 1,
                    maxLabelLength = maxLabelLength,
                    reorderThreshold = reorderThreshold,
                    onClick = { onTabSelect(tab.id) },
                    onClose = { onTabClose(tab.id) },
                    onMove = onTabMove
                )
            }

            // Add tab button
            AnimatedVisibility(
                visible = canAddMore,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                AddTabButton(onClick = onAddTab)
            }
        }
    }
}

@Composable
private fun TabChip(
    tab: CalculatorTabData,
    index: Int,
    tabCount: Int,
    isActive: Boolean,
    showCloseButton: Boolean,
    maxLabelLength: Int,
    reorderThreshold: Dp,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val reduceMotion = LocalCalculatorReduceMotion.current
    val thresholdPx = with(LocalDensity.current) { reorderThreshold.toPx() }
    var dragAccumulatedX by remember(index) { mutableFloatStateOf(0f) }
    var isDragging by remember(index) { mutableStateOf(false) }
    val dragScaleTarget = if (isDragging && !reduceMotion) 1.03f else 1f
    val scale by animateFloatAsState(
        targetValue = dragScaleTarget,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 120),
        label = "tab_drag_scale"
    )
    val elevation by animateFloatAsState(
        targetValue = if (isDragging) 8f else if (isActive) 2f else 0f,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 120),
        label = "tab_drag_elevation"
    )

    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .zIndex(if (isDragging) 1f else 0f)
            .pointerInput(index, tabCount) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                        dragAccumulatedX = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        dragAccumulatedX = 0f
                    },
                    onDragEnd = {
                        isDragging = false
                        dragAccumulatedX = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    dragAccumulatedX += dragAmount.x
                    if (dragAccumulatedX > thresholdPx && index < tabCount - 1) {
                        onMove(index, index + 1)
                        dragAccumulatedX = 0f
                    } else if (dragAccumulatedX < -thresholdPx && index > 0) {
                        onMove(index, index - 1)
                        dragAccumulatedX = 0f
                    }
                }
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        tonalElevation = elevation.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = if (showCloseButton) 4.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tab.getLabel(maxLength = maxLabelLength),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (showCloseButton) {
                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(Res.string.cpp_a11y_close),
                        tint = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun middleEllipsize(value: String, maxLength: Int): String {
    if (maxLength <= 3) return value.take(maxLength)
    if (value.length <= maxLength) return value

    val charsToKeep = maxLength - 3
    val prefixLength = (charsToKeep + 1) / 2
    val suffixLength = charsToKeep - prefixLength
    return value.take(prefixLength) + "..." + value.takeLast(suffixLength)
}

@Composable
private fun AddTabButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add tab"
        )
    }
}
