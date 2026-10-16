package org.solovyev.android.calculator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            else -> "0"
        }
    }
}

/**
 * A horizontal scrollable tab bar for the calculator with refined pill aesthetic.
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
@Suppress("UNUSED_PARAMETER")
fun CalculatorTabBar(
    tabs: List<CalculatorTabData>,
    activeTabId: String,
    onTabSelect: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onAddTab: () -> Unit,
    onTabMove: (fromIndex: Int, toIndex: Int) -> Unit,
    canAddMore: Boolean = true,
    maxLabelLength: Int = 5,
    reorderThreshold: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            TabChip(
                tab = tab,
                isActive = tab.id == activeTabId,
                showCloseButton = tabs.size > 1,
                maxLabelLength = maxLabelLength,
                onClick = { onTabSelect(tab.id) },
                onClose = { onTabClose(tab.id) }
            )
        }

        AnimatedVisibility(
            visible = canAddMore,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            AddTabButton(onClick = onAddTab)
        }
    }
}

@Composable
private fun TabChip(
    tab: CalculatorTabData,
    isActive: Boolean,
    showCloseButton: Boolean,
    maxLabelLength: Int,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    val selectionAnimationDuration = if (reduceMotion) 0 else 220
    val chipShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    val closeSlotWidth by animateDpAsState(
        targetValue = if (showCloseButton) 38.dp else 0.dp,
        animationSpec = tween(durationMillis = selectionAnimationDuration),
        label = "tab_close_slot_width"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.44f)
        },
        animationSpec = tween(durationMillis = selectionAnimationDuration),
        label = "tab_bg_color"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = selectionAnimationDuration),
        label = "tab_text_color"
    )
    val closeAlpha by animateFloatAsState(
        targetValue = if (isActive && showCloseButton) 1f else 0f,
        animationSpec = tween(durationMillis = selectionAnimationDuration),
        label = "tab_close_alpha"
    )
    val closeScale by animateFloatAsState(
        targetValue = if (isActive && showCloseButton) 1f else 0.85f,
        animationSpec = tween(durationMillis = selectionAnimationDuration),
        label = "tab_close_scale"
    )
    val closeOffsetX by animateFloatAsState(
        targetValue = if (isActive && showCloseButton) 0f else -6f,
        animationSpec = tween(durationMillis = selectionAnimationDuration),
        label = "tab_close_offset_x"
    )

    Surface(
        modifier = modifier
            .heightIn(min = 44.dp)
            .widthIn(min = 108.dp, max = 152.dp)
            .clickable(onClick = onClick),
        shape = chipShape,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tab.getLabel(maxLength = maxLabelLength),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(min = 42.dp, max = 98.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier.width(closeSlotWidth),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onClose,
                    enabled = isActive && showCloseButton,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = textColor
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            alpha = closeAlpha
                            scaleX = closeScale
                            scaleY = closeScale
                            translationX = closeOffsetX
                        }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(Res.string.cpp_a11y_close),
                        tint = textColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp)
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
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(width = 44.dp, height = 44.dp),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.34f)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add tab",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
