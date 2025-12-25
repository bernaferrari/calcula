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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Direction texts for swipe actions on calculator buttons
 */
data class DirectionTexts(
    val up: String? = null,
    val down: String? = null,
    val left: String? = null,
    val right: String? = null
)

/**
 * Button type determines the styling
 */
enum class ButtonType {
    DIGIT,
    OPERATION,
    OPERATION_HIGHLIGHTED,
    CONTROL,
    SPECIAL
}

/**
 * Calculator button component supporting:
 * - Main text (center)
 * - Direction texts (up, down, left, right) for swipe actions
 * - Different button types (number, operator, function, special)
 * - Click and long-press support
 * - Drag gestures for directional actions
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.DIGIT,
    directionTexts: DirectionTexts = DirectionTexts(),
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isPressed -> when (buttonType) {
            ButtonType.DIGIT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
            ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ButtonType.SPECIAL -> MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        }
        else -> when (buttonType) {
            ButtonType.DIGIT -> MaterialTheme.colorScheme.surface
            ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer
            ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.tertiaryContainer
            ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceVariant
            ButtonType.SPECIAL -> MaterialTheme.colorScheme.surface
        }
    }

    val textColor = when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.onSurface
        ButtonType.OPERATION -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.onTertiaryContainer
        ButtonType.CONTROL -> MaterialTheme.colorScheme.onSurfaceVariant
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.onSurface
    }

    val directionTextColor = textColor.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    onClick()
                },
                onLongClick = {
                    onLongClick()
                }
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false },
                    onDragCancel = { isPressed = false }
                ) { change, dragAmount ->
                    change.consume()

                    // Detect drag direction based on the largest component
                    val threshold = 20f
                    if (abs(dragAmount.x) > threshold || abs(dragAmount.y) > threshold) {
                        when {
                            abs(dragAmount.y) > abs(dragAmount.x) -> {
                                if (dragAmount.y < 0) {
                                    // Swipe up
                                    onSwipeUp?.invoke()
                                } else {
                                    // Swipe down
                                    onSwipeDown?.invoke()
                                }
                            }
                            else -> {
                                if (dragAmount.x < 0) {
                                    // Swipe left
                                    onSwipeLeft?.invoke()
                                } else {
                                    // Swipe right
                                    onSwipeRight?.invoke()
                                }
                            }
                        }
                        isPressed = false
                    }
                }
            }
            .drawWithContent {
                drawContent()

                val directionTextSize = 12.sp.toPx()
                val padding = 8.dp.toPx()

                // Draw direction texts
                val nativeCanvas = drawContext.canvas.nativeCanvas

                directionTexts.up?.let { upText ->
                    val paint = android.graphics.Paint().apply {
                        color = directionTextColor.toArgb()
                        textSize = directionTextSize
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    nativeCanvas.drawText(
                        upText,
                        size.width / 2,
                        padding + directionTextSize,
                        paint
                    )
                }

                directionTexts.down?.let { downText ->
                    val paint = android.graphics.Paint().apply {
                        color = directionTextColor.toArgb()
                        textSize = directionTextSize
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    nativeCanvas.drawText(
                        downText,
                        size.width / 2,
                        size.height - padding,
                        paint
                    )
                }

                directionTexts.left?.let { leftText ->
                    val paint = android.graphics.Paint().apply {
                        color = directionTextColor.toArgb()
                        textSize = directionTextSize
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                    nativeCanvas.drawText(
                        leftText,
                        padding,
                        size.height / 2 + directionTextSize / 3,
                        paint
                    )
                }

                directionTexts.right?.let { rightText ->
                    val paint = android.graphics.Paint().apply {
                        color = directionTextColor.toArgb()
                        textSize = directionTextSize
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    nativeCanvas.drawText(
                        rightText,
                        size.width - padding,
                        size.height / 2 + directionTextSize / 3,
                        paint
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 24.sp,
                color = textColor,
                fontStyle = fontStyle ?: FontStyle.Normal,
                fontWeight = fontWeight ?: FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )
    }
}

/**
 * Preview helper for calculator button
 */
@Composable
fun CalculatorButtonPreview(
    text: String,
    directionTexts: DirectionTexts = DirectionTexts(),
    buttonType: ButtonType = ButtonType.DIGIT
) {
    CalculatorButton(
        text = text,
        buttonType = buttonType,
        directionTexts = directionTexts,
        modifier = Modifier.padding(4.dp)
    )
}
