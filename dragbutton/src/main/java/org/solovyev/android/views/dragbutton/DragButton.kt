package org.solovyev.android.views.dragbutton

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp

/**
 * A button that supports both click and directional drag gestures.
 *
 * @param onClick Callback invoked when the button is clicked (not dragged).
 * @param onDrag Callback invoked with the detected [DragDirection] when a drag gesture is recognized.
 *               Return true to consume the event and trigger haptic feedback.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 * @param minDragDistance Minimum distance for a drag gesture to be recognized.
 * @param vibrateOnDrag Whether to provide haptic feedback on successful drag.
 * @param content The content to display inside the button.
 */
@Composable
fun DragButton(
    onClick: () -> Unit,
    onDrag: (DragDirection) -> Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minDragDistance: Dp = DefaultMinDragDistance,
    vibrateOnDrag: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .detectDirectionalDrag(
                minDistance = minDragDistance,
                vibrateOnDrag = vibrateOnDrag,
                onDrag = { direction ->
                    if (enabled) onDrag(direction) else false
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
