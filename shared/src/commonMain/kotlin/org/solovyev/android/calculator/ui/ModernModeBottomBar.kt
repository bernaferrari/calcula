package org.solovyev.android.calculator.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ModernModeBottomBar(
    onPrevious: () -> Unit,
    onPreviousStart: () -> Unit,
    onNext: () -> Unit,
    onNextEnd: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenGraph: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BottomToolbarActionButton(
            label = null,
            icon = {
                Text(text = "←")
            },
            onClick = onPrevious,
            onLongClick = onPreviousStart,
            modifier = Modifier.weight(1f)
        )
        BottomToolbarActionButton(
            label = null,
            icon = {
                Text(text = "→")
            },
            onClick = onNext,
            onLongClick = onNextEnd,
            modifier = Modifier.weight(1f)
        )
        BottomToolbarActionButton(
            label = "Graph",
            icon = {
                Text(text = "📊")
            },
            onClick = onOpenGraph,
            modifier = Modifier.weight(1f)
        )
        BottomToolbarActionButton(
            label = "Convert",
            icon = {
                Text(text = "⇄")
            },
            onClick = onOpenConverter,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomToolbarActionButton(
    label: String?,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        modifier = modifier
            .height(46.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            horizontalArrangement = if (label == null) {
                Arrangement.Center
            } else {
                Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
