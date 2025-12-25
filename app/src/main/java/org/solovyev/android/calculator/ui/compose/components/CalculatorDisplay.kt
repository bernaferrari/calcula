/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.solovyev.android.calculator.DisplayState

/**
 * Calculator display component that shows the calculation result.
 *
 * Features:
 * - Auto-resizing text to fit content
 * - Error state with red color styling
 * - Copy button for results (click or long-press on display)
 * - Animated result changes with smooth slide transitions
 * - Horizontal scrolling for long results
 * - Material3 theming with clean, modern design
 * - Haptic feedback on long press
 *
 * @param state The current display state containing text, validity, and result
 * @param onCopy Callback invoked when the copy button is clicked or display is long-pressed
 * @param modifier Modifier to be applied to the display
 * @param minTextSize Minimum text size for auto-resizing
 * @param maxTextSize Maximum text size for auto-resizing
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorDisplay(
    state: DisplayState,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    minTextSize: TextUnit = 20.sp,
    maxTextSize: TextUnit = 48.sp
) {
    val scrollState = rememberScrollState()
    val hapticFeedback = LocalHapticFeedback.current

    // Auto-scroll to end when text changes
    LaunchedEffect(state.text) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display text with animation
                AnimatedContent(
                    targetState = state,
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                        .combinedClickable(
                            enabled = state.text.isNotEmpty() && state.valid,
                            onClick = { /* Optional: single click action */ },
                            onLongClick = {
                                if (state.text.isNotEmpty() && state.valid) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCopy()
                                }
                            }
                        ),
                    transitionSpec = {
                        // Smooth slide up animation for new results
                        (slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetY = { it }
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutVertically(
                                animationSpec = tween(200),
                                targetOffsetY = { -it }
                            ) + fadeOut(animationSpec = tween(200))
                        )
                    },
                    label = "DisplayTextAnimation"
                ) { targetState ->
                    val textColor = if (targetState.valid) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }

                    val fontSize = calculateFontSize(
                        text = targetState.text,
                        minSize = minTextSize,
                        maxSize = maxTextSize
                    )

                    SelectionContainer {
                        Text(
                            text = targetState.text.ifEmpty { "0" },
                            style = TextStyle(
                                color = textColor,
                                fontSize = fontSize,
                                fontWeight = if (targetState.valid) {
                                    FontWeight.Normal
                                } else {
                                    FontWeight.Medium
                                },
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.End,
                                letterSpacing = 0.5.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Copy button - only show when there's valid content
                AnimatedVisibility(
                    visible = state.text.isNotEmpty() && state.valid,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCopy()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculates an appropriate font size based on text length.
 * Longer text gets smaller font size for better fit.
 */
@Composable
private fun calculateFontSize(
    text: String,
    minSize: TextUnit,
    maxSize: TextUnit
): TextUnit {
    val length = text.length
    val size = when {
        length == 0 -> maxSize
        length < 8 -> maxSize
        length < 12 -> 40.sp
        length < 16 -> 32.sp
        length < 20 -> 28.sp
        else -> minSize
    }

    // Coerce between min and max using value comparison
    return when {
        size.value < minSize.value -> minSize
        size.value > maxSize.value -> maxSize
        else -> size
    }
}
