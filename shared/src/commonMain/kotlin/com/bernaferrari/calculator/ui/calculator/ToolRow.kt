package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex

/** Off shows digits; Once flips back after one function, like Shift; Locked stays, like Caps Lock. */
enum class FunctionLayer { Off, Once, Locked }

private val SegmentWidth = 60.dp
private val SegmentStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 16.sp, fontWeight = FontWeight.Medium)

/**
 * The row between display and keypad: history on the left, the 123/ƒ switch in the middle and
 * settings on the right. Everything that isn't a key lives here, in one predictable place.
 */
@Composable
fun ToolRow(
    historyOpen: Boolean,
    layer: FunctionLayer,
    onToggleHistory: () -> Unit,
    onLayer: (FunctionLayer) -> Unit,
    onOpenConverter: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenGraph: () -> Unit = {},
) {
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier.fillMaxWidth().height(52.dp).padding(horizontal = 8.dp)) {
        Row(Modifier.align(Alignment.CenterStart)) {
            IconButton(onClick = onToggleHistory) {
                Icon(
                    if (historyOpen) Icons.Outlined.Dialpad else Icons.Outlined.History,
                    contentDescription = stringResource(if (historyOpen) Res.string.show_keypad else Res.string.show_history),
                    tint = quiet,
                )
            }
            IconButton(onClick = onOpenGraph, modifier = Modifier.containerTransform("graph")) {
                Icon(Icons.Outlined.ShowChart, contentDescription = stringResource(Res.string.graph_title), tint = quiet)
            }
        }
        // Functions mean nothing while browsing history, so the switch steps out of the way.
        AnimatedVisibility(
            !historyOpen,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.align(Alignment.Center),
        ) {
            LayerSwitch(layer, onLayer)
        }
        Row(Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = onOpenConverter, modifier = Modifier.containerTransform("converter")) {
                Icon(Icons.Outlined.Straighten, contentDescription = stringResource(Res.string.menu_unit_converter), tint = quiet)
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.containerTransform("settings")) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(Res.string.menu_settings), tint = quiet)
            }
        }
    }
}

@Composable
private fun LayerSwitch(layer: FunctionLayer, onLayer: (FunctionLayer) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val on = layer != FunctionLayer.Off
    val locked = layer == FunctionLayer.Locked
    val indicatorOffset by animateDpAsState(
        if (on) SegmentWidth else 0.dp,
        spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
        label = "indicator",
    )
    val indicatorColor by animateColorAsState(
        when {
            locked -> colors.primary
            on -> colors.primaryContainer
            else -> colors.surfaceContainerHighest
        },
        label = "indicatorColor",
    )
    val description = stringResource(Res.string.function_layer_description)
    Box(
        Modifier
            .height(40.dp)
            .width(SegmentWidth * 2)
            .clip(CircleShape)
            .background(colors.surfaceContainer),
    ) {
        Box(
            Modifier
                .offset(x = indicatorOffset)
                .width(SegmentWidth)
                .fillMaxHeight()
                .padding(3.dp)
                .background(indicatorColor, CircleShape),
        )
        Row {
            Segment(
                label = "123",
                selected = !on,
                color = if (!on) colors.onSurface else colors.onSurfaceVariant,
                onClick = { onLayer(FunctionLayer.Off) },
                onLongClick = null,
            )
            Segment(
                label = "ƒ",
                selected = on,
                color = when {
                    locked -> colors.onPrimary
                    on -> colors.onPrimaryContainer
                    else -> colors.onSurfaceVariant
                },
                // Tap once for one function, again to lock; hold to lock straight away.
                onClick = {
                    onLayer(
                        when (layer) {
                            FunctionLayer.Off -> FunctionLayer.Once
                            FunctionLayer.Once -> FunctionLayer.Locked
                            FunctionLayer.Locked -> FunctionLayer.Off
                        },
                    )
                },
                onLongClick = { onLayer(FunctionLayer.Locked) },
                modifier = Modifier.semantics { contentDescription = description },
                underline = locked,
            )
        }
    }
}

@Composable
private fun Segment(
    label: String,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    underline: Boolean = false,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(SegmentWidth)
            .fillMaxHeight()
            .clip(CircleShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick, role = Role.Tab)
            .semantics { this.selected = selected },
    ) {
        Text(label, style = SegmentStyle, color = color)
        if (underline) {
            // Locked, like Caps Lock: a small bar under the ƒ.
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(width = 12.dp, height = 2.dp)
                    .background(color, CircleShape),
            )
        }
    }
}
