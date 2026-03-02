package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.ui.tokens.CalculatorMenuTokens

sealed interface CalculatorMenuEntry {
    data class Action(
        val label: String,
        val icon: ImageVector? = null,
        val onClick: () -> Unit,
        val enabled: Boolean = true,
        val destructive: Boolean = false,
        val showTrailingArrow: Boolean = true
    ) : CalculatorMenuEntry

    object Divider : CalculatorMenuEntry
}

@Composable
fun CalculatorOverflowIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(Res.string.cpp_a11y_more_options)
        )
    }
}

@Composable
fun CalculatorOverflowDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    entries: List<CalculatorMenuEntry>,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(0.dp, 4.dp),
        shape = RoundedCornerShape(CalculatorMenuTokens.ContainerCorner),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        modifier = modifier.widthIn(
            min = CalculatorMenuTokens.MinWidth,
            max = CalculatorMenuTokens.MaxWidth
        )
    ) {
        entries.forEach { entry ->
            when (entry) {
                CalculatorMenuEntry.Divider -> {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                }

                is CalculatorMenuEntry.Action -> {
                    CalculatorOverflowDropdownMenuItem(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun CalculatorOverflowDropdownMenuItem(
    entry: CalculatorMenuEntry.Action
) {
    val textColor = if (entry.destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val iconTint = if (entry.destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    val trailingTint = if (entry.destructive) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    }

    DropdownMenuItem(
        modifier = Modifier.heightIn(min = CalculatorMenuTokens.ItemMinHeight),
        text = {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = entry.icon?.let { icon ->
            {
                Surface(
                    modifier = Modifier.size(CalculatorMenuTokens.LeadingIconSize),
                    shape = RoundedCornerShape(CalculatorMenuTokens.LeadingIconCorner),
                    color = if (entry.destructive) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        trailingIcon = if (entry.showTrailingArrow) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = trailingTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            null
        },
        enabled = entry.enabled,
        onClick = entry.onClick
    )
}
