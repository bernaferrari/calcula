package org.solovyev.android.calculator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Represents a calculator tab with id and expression
 */
data class CalculatorTabData(
    val id: String,
    val expression: String,
    val result: String = ""
) {
    fun getLabel(): String {
        return when {
            expression.isNotEmpty() -> expression.take(20)
            result.isNotEmpty() -> "= $result"
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
    canAddMore: Boolean = true,
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
            tabs.forEach { tab ->
                TabChip(
                    tab = tab,
                    isActive = tab.id == activeTabId,
                    showCloseButton = tabs.size > 1,
                    onClick = { onTabSelect(tab.id) },
                    onClose = { onTabClose(tab.id) }
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
    isActive: Boolean,
    showCloseButton: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
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

    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        tonalElevation = if (isActive) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = if (showCloseButton) 4.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tab.getLabel(),
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
                    Text(
                        text = "✕",
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTabButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
