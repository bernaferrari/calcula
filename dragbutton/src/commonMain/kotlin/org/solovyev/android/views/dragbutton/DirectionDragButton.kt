package org.solovyev.android.views.dragbutton

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Configuration for direction text display.
 */
data class DirectionTextConfig(
    val text: String = "",
    val visible: Boolean = true,
    val scale: Float = 0.4f,
    val alpha: Float = 0.4f,
    val padding: Dp = 4.dp
)

/**
 * A button with directional text labels that supports click and drag gestures.
 *
 * @param text The main button text.
 * @param onClick Callback invoked when clicked.
 * @param onDrag Callback invoked with the drag direction. Return true to consume.
 * @param modifier Modifier for the button.
 * @param directionTexts Map of direction to text configuration.
 * @param textStyle Style for the main text.
 * @param enabled Whether the button is enabled.
 * @param minDragDistance Minimum distance for drag recognition.
 * @param vibrateOnDrag Whether to vibrate on successful drag.
 * @param highContrast Whether to use high contrast colors.
 * @param contentColor Color for the main content.
 */
@Composable
fun DirectionDragButton(
    text: String,
    onClick: () -> Unit,
    onDrag: (DragDirection) -> Boolean,
    modifier: Modifier = Modifier,
    directionTexts: Map<DragDirection, DirectionTextConfig> = emptyMap(),
    textStyle: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true,
    minDragDistance: Dp = DefaultMinDragDistance,
    vibrateOnDrag: Boolean = true,
    highContrast: Boolean = false,
    contentColor: Color = LocalContentColor.current
) {
    DragButton(
        onClick = onClick,
        onDrag = onDrag,
        modifier = modifier,
        enabled = enabled,
        minDragDistance = minDragDistance,
        vibrateOnDrag = vibrateOnDrag
    ) {
        DirectionDragButtonContent(
            text = text,
            directionTexts = directionTexts,
            textStyle = textStyle,
            highContrast = highContrast,
            contentColor = contentColor
        )
    }
}

/**
 * Content layout for DirectionDragButton with directional text labels.
 */
@Composable
private fun BoxScope.DirectionDragButtonContent(
    text: String,
    directionTexts: Map<DragDirection, DirectionTextConfig>,
    textStyle: TextStyle,
    highContrast: Boolean,
    contentColor: Color
) {
    // Main text in center
    Text(
        text = text,
        style = textStyle,
        color = contentColor
    )

    // Direction texts
    for (direction in DragDirection.entries) {
        val config = directionTexts[direction]
        if (config != null && config.visible && config.text.isNotEmpty()) {
            val directionTextStyle = textStyle.copy(
                fontSize = textStyle.fontSize * config.scale,
                fontWeight = if (highContrast) FontWeight.Bold else FontWeight.Normal
            )
            val directionColor = if (highContrast) {
                contentColor
            } else {
                contentColor.copy(alpha = config.alpha)
            }

            Text(
                text = config.text,
                style = directionTextStyle,
                color = directionColor,
                modifier = Modifier
                    .align(direction.toAlignment())
                    .padding(config.padding)
            )
        }
    }
}

/**
 * Maps DragDirection to Compose Alignment for positioning direction text.
 */
private fun DragDirection.toAlignment(): Alignment = when (this) {
    DragDirection.up -> Alignment.TopEnd
    DragDirection.down -> Alignment.BottomEnd
    DragDirection.left -> Alignment.CenterStart
    DragDirection.right -> Alignment.CenterEnd
}

/**
 * Convenience composable for an image-based drag button.
 */
@Composable
fun DirectionDragImageButton(
    onClick: () -> Unit,
    onDrag: (DragDirection) -> Boolean,
    modifier: Modifier = Modifier,
    directionTexts: Map<DragDirection, DirectionTextConfig> = emptyMap(),
    textStyle: TextStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
    enabled: Boolean = true,
    minDragDistance: Dp = DefaultMinDragDistance,
    vibrateOnDrag: Boolean = true,
    highContrast: Boolean = false,
    contentColor: Color = LocalContentColor.current,
    image: @Composable BoxScope.() -> Unit
) {
    DragButton(
        onClick = onClick,
        onDrag = onDrag,
        modifier = modifier,
        enabled = enabled,
        minDragDistance = minDragDistance,
        vibrateOnDrag = vibrateOnDrag
    ) {
        // Image content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            image()
        }

        // Direction texts overlay
        for (direction in DragDirection.entries) {
            val config = directionTexts[direction]
            if (config != null && config.visible && config.text.isNotEmpty()) {
                val directionTextStyle = textStyle.copy(
                    fontSize = textStyle.fontSize * config.scale,
                    fontWeight = if (highContrast) FontWeight.Bold else FontWeight.Normal
                )
                val directionColor = if (highContrast) {
                    contentColor
                } else {
                    contentColor.copy(alpha = config.alpha)
                }

                Text(
                    text = config.text,
                    style = directionTextStyle,
                    color = directionColor,
                    modifier = Modifier
                        .align(direction.toAlignment())
                        .padding(config.padding)
                )
            }
        }
    }
}
