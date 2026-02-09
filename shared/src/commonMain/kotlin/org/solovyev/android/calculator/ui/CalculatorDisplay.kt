package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.DisplayState
import kotlin.math.abs

@Composable
fun CalculatorDisplay(
    state: DisplayState,
    modifier: Modifier = Modifier,
    textSize: TextUnit = 44.sp,
    onSwipeDelete: (() -> Unit)? = null,
    onSwipeCopy: (() -> Unit)? = null,
    onReuseResult: (() -> Unit)? = null
) {

    // Determine what to display and how
    val displayText: String
    val textAlpha: Float
    val textColor = MaterialTheme.colorScheme.onSurface

    displayText = state.text
    textAlpha = when {
        displayText.isEmpty() -> 0f
        state.valid -> 1f
        else -> 0.6f
    }
    val shownText = if (displayText.isEmpty()) " " else displayText

    // Swipe gesture state
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val swipeThreshold = with(LocalDensity.current) { 80.dp.toPx() }
    val scrollState = rememberScrollState()

    LaunchedEffect(displayText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .heightIn(min = 56.dp)
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
            }
            .pointerInput(onSwipeDelete, onSwipeCopy) {
                detectDragGestures(
                    onDragEnd = {
                        // Check if swipe was significant
                        when {
                            offsetX.value < -swipeThreshold && onSwipeDelete != null -> {
                                // Left swipe - delete
                                if (hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onSwipeDelete()
                            }
                            offsetY.value < -swipeThreshold && onSwipeCopy != null -> {
                                // Up swipe - copy
                                if (hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onSwipeCopy()
                            }
                        }
                        // Animate back to center
                        coroutineScope.launch {
                            offsetX.animateTo(
                                0f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                            )
                        }
                        coroutineScope.launch {
                            offsetY.animateTo(
                                0f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                            )
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            // Limit horizontal drag to leftward only
                            if (dragAmount.x < 0 || offsetX.value < 0) {
                                offsetX.snapTo((offsetX.value + dragAmount.x).coerceIn(-swipeThreshold * 1.5f, 0f))
                            }
                            // Limit vertical drag to upward only
                            if (dragAmount.y < 0 || offsetY.value < 0) {
                                offsetY.snapTo((offsetY.value + dragAmount.y).coerceIn(-swipeThreshold * 1.5f, 0f))
                            }
                        }
                    }
                )
            }
            // Add separate pointerInput for taps to avoid conflict/consumption issues
            .pointerInput(onSwipeCopy, onReuseResult) {
                 detectTapGestures(
                     onTap = {
                         if (hapticsEnabled) {
                             haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                         }
                         onReuseResult?.invoke()
                     },
                     onLongPress = {
                         if (hapticsEnabled) {
                             haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                         }
                         onSwipeCopy?.invoke()
                     }
                 )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = shownText,
            style = TextStyle(
                color = textColor,
                fontSize = textSize,
                fontWeight = FontWeight.Medium,
                fontFamily = CalculatorFontFamily,
                textAlign = TextAlign.End
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
                .alpha(textAlpha)
        )
    }
}
