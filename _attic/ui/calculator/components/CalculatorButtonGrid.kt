package com.bernaferrari.calculator.ui.calculator.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.domain.model.ButtonType
import com.bernaferrari.calculator.domain.model.CalculatorButton
import com.bernaferrari.calculator.domain.model.CalculatorState

@Composable
fun CalculatorButtonGrid(
    state: CalculatorState,
    onButtonClick: (CalculatorButton) -> Unit,
    onSecondaryClick: (CalculatorButton) -> Unit,
    modifier: Modifier = Modifier,
) {
// Award-winning organic layout with expressive spacing
    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Row 1: Navigation and operators
        CalculatorButtonRow(
            buttons =
                listOf(
                    CalculatorButton(
                        "clear",
                        "C",
                        null,
                        ButtonType.SPECIAL,
                        com.bernaferrari.calculator.domain.model.ButtonFunction.CLEAR,
                    ),
                    CalculatorButton(
                        "delete",
                        "⌫",
                        null,
                        ButtonType.SPECIAL,
                        com.bernaferrari.calculator.domain.model.ButtonFunction.BACKSPACE,
                    ),
                    CalculatorButton(
                        "left",
                        "←",
                        null,
                        ButtonType.SPECIAL,
                        com.bernaferrari.calculator.domain.model.ButtonFunction.BACKSPACE,
                    ),
                    CalculatorButton(
                        "divide",
                        "÷",
                        null,
                        ButtonType.OPERATOR,
                        com.bernaferrari.calculator.domain.model.ButtonFunction.DIVIDE,
                    ),
                ),
            onButtonClick = onButtonClick,
            onSecondaryClick = onSecondaryClick,
            showSecondary = state.isInverseMode,
            isJoyfulRow = true,
        )

        // Row 2: Numbers 7-9 with scientific functions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 7 with sin at bottom
            SwipeableNumberButton(
                number = "7",
                scientificFunction = "sin",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.SEVEN,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // 8 with cos at bottom
            SwipeableNumberButton(
                number = "8",
                scientificFunction = "cos",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.EIGHT,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // 9 with tan at bottom
            SwipeableNumberButton(
                number = "9",
                scientificFunction = "tan",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.NINE,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // Multiply operator
            JoyfulCalculatorButton(
                button =
                    CalculatorButton(
                        "multiply",
                        "×",
                        null,
                        ButtonType.OPERATOR,
                        com.bernaferrari.calculator.domain.model.ButtonFunction.MULTIPLY,
                    ),
                onClick = {
                    onButtonClick(
                        CalculatorButton(
                            "multiply",
                            "×",
                            null,
                            ButtonType.OPERATOR,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.MULTIPLY,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
            )
        }

        // Row 3: Numbers 4-6 with scientific functions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 4 with ln at bottom
            SwipeableNumberButton(
                number = "4",
                scientificFunction = "ln",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.FOUR,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // 5 with log at bottom
            SwipeableNumberButton(
                number = "5",
                scientificFunction = "log",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.FIVE,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // 6 with √ at bottom
            SwipeableNumberButton(
                number = "6",
                scientificFunction = "√",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.SIX,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // Minus operator
            JoyfulCalculatorButton(
                button =
                    CalculatorButton(
                        "minus",
                        "−",
                        null,
                        ButtonType.OPERATOR,
                        com.bernaferrari.calculator.domain.model.ButtonFunction.MINUS,
                    ),
                onClick = {
                    onButtonClick(
                        CalculatorButton(
                            "minus",
                            "−",
                            null,
                            ButtonType.OPERATOR,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.MINUS,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
            )
        }

        // Row 4: Numbers 1-3 with scientific functions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 1 with π at bottom
            SwipeableNumberButton(
                number = "1",
                scientificFunction = "π",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.ONE,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // 2 with e at bottom
            SwipeableNumberButton(
                number = "2",
                scientificFunction = "e",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.TWO,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // 3 with x² at bottom
            SwipeableNumberButton(
                number = "3",
                scientificFunction = "x²",
                buttonFunction = com.bernaferrari.calculator.domain.model.ButtonFunction.THREE,
                onButtonClick = onButtonClick,
                onSecondaryClick = onSecondaryClick,
                modifier = Modifier.weight(1f),
            )

            // Plus operator
            JoyfulCalculatorButton(
                button =
                    CalculatorButton(
                        "plus",
                        "+",
                        null,
                        ButtonType.OPERATOR,
                        com.bernaferrari.calculator.domain.model.ButtonFunction.PLUS,
                    ),
                onClick = {
                    onButtonClick(
                        CalculatorButton(
                            "plus",
                            "+",
                            null,
                            ButtonType.OPERATOR,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.PLUS,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
            )
        }

        // Row 5: Bottom row with split parenthesis and enhanced zero
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Split parenthesis button (vertical layout)
            SplitParenthesisButton(
                onOpenParenClick = {
                    onButtonClick(
                        CalculatorButton(
                            "open_paren",
                            "(",
                            null,
                            ButtonType.PARENTHESIS,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.PARENTHESIS,
                        ),
                    )
                },
                onCloseParenClick = {
                    onButtonClick(
                        CalculatorButton(
                            "close_paren",
                            ")",
                            null,
                            ButtonType.PARENTHESIS,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.PARENTHESIS,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
            )

            // Enhanced zero button with 000/00
            EnhancedZeroButton(
                onZeroClick = {
                    onButtonClick(
                        CalculatorButton(
                            "0",
                            "0",
                            null,
                            ButtonType.NUMBER,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.ZERO,
                        ),
                    )
                },
                onTripleZeroClick = {
                    onButtonClick(
                        CalculatorButton(
                            "000",
                            "000",
                            null,
                            ButtonType.NUMBER,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.ZERO,
                        ),
                    )
                },
                onDoubleZeroClick = {
                    onButtonClick(
                        CalculatorButton(
                            "00",
                            "00",
                            null,
                            ButtonType.NUMBER,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.ZERO,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
            )

            // Decimal point
            JoyfulCalculatorButton(
                button =
                    CalculatorButton(
                        "dot",
                        ".",
                        null,
                        ButtonType.SPECIAL,
                        com.bernaferrari.calculator.domain.model.ButtonFunction.DOT,
                    ),
                onClick = {
                    onButtonClick(
                        CalculatorButton(
                            "dot",
                            ".",
                            null,
                            ButtonType.SPECIAL,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.DOT,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
                isSpecial = true,
            )

            // Equals button with enhanced sparkle
            EnhancedEqualsButton(
                onClick = {
                    onButtonClick(
                        CalculatorButton(
                            "equals",
                            "=",
                            null,
                            ButtonType.SPECIAL,
                            com.bernaferrari.calculator.domain.model.ButtonFunction.EQUALS,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SwipeableNumberButton(
    number: String,
    scientificFunction: String,
    buttonFunction: com.bernaferrari.calculator.domain.model.ButtonFunction,
    onButtonClick: (CalculatorButton) -> Unit,
    onSecondaryClick: (CalculatorButton) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var isScientificActivated by remember { mutableStateOf(false) }
    var totalDragDistance by remember { mutableStateOf(0f) }
    var dragProgress by remember { mutableStateOf(0f) }

    // Animation for drag progress (0f = normal, 1f = fully activated)
    val animatedDragProgress by animateFloatAsState(
        targetValue = if (isScientificActivated) 1f else 0f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh,
            ),
        label = "drag_progress",
    )

    // Main number alpha - fades out when scientific function is activated
    val numberAlpha by animateFloatAsState(
        targetValue = if (isScientificActivated) 0.3f else 1f,
        animationSpec = tween(200),
        label = "number_alpha",
    )

    // Scientific function scale and alpha - moves to center when activated
    val functionScale by animateFloatAsState(
        targetValue = if (isScientificActivated) 1.2f else 0.8f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh,
            ),
        label = "function_scale",
    )

    val functionAlpha by animateFloatAsState(
        targetValue = if (isScientificActivated) 1f else 0.6f,
        animationSpec = tween(200),
        label = "function_alpha",
    )

    val scientificOffset by animateFloatAsState(
        targetValue = if (isScientificActivated) -8f else 0f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "scientific_offset",
    )

    val colors = getAdaptiveButtonColors(ButtonType.NUMBER)
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    Box(
        modifier =
            modifier
                .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = {
                onButtonClick(
                    CalculatorButton(
                        number,
                        number,
                        scientificFunction,
                        ButtonType.NUMBER,
                        buttonFunction,
                    ),
                )
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                totalDragDistance = 0f
                            },
                            onDragEnd = {
                                if (isScientificActivated) {
                                    onSecondaryClick(
                                        CalculatorButton(
                                            scientificFunction,
                                            scientificFunction,
                                            null,
                                            ButtonType.FUNCTION,
                                            buttonFunction,
                                        ),
                                    )
                                }
                                isDragging = false
                                isScientificActivated = false
                                totalDragDistance = 0f
                            },
                            onDrag = { _, dragAmount ->
                                // Accumulate drag distance (positive Y is downward)
                                totalDragDistance += dragAmount.y
                                dragProgress = kotlin.math.max(0f, kotlin.math.min(1f, totalDragDistance / 60f))
                                if (totalDragDistance > 30f && !isScientificActivated) {
                                    isScientificActivated = true
                                }
                            },
                        )
                    }.shadow(
                        elevation =
                            if (isPressed) {
                                4.dp
                            } else if (isScientificActivated) {
                                20.dp
                            } else {
                                12.dp
                            },
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = colors.first.copy(alpha = 0.2f),
                        spotColor = colors.first.copy(alpha = 0.4f),
                    ),
            shape = RoundedCornerShape(24.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colors.first,
                    contentColor = colors.second,
                ),
            contentPadding = PaddingValues(0.dp),
            interactionSource = interactionSource,
            elevation =
                ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
// Main number (center) - fades when scientific function is activated
                Text(
                    text = number,
                    color = colors.second.copy(alpha = numberAlpha),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )

                // Award-winning expressive scientific function animation
                val functionOffset =
                    if (isScientificActivated) {
                        Alignment.Center
                    } else {
                        Alignment.BottomEnd
                    }

                // Background glow effect when scientific function is activated
                if (isScientificActivated) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(40.dp)
                                .background(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                                    Color.Transparent,
                                                ),
                                        ),
                                    shape = RoundedCornerShape(20.dp),
                                ),
                    )
                }

                Text(
                    text = scientificFunction,
                    color =
                        if (isScientificActivated) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            colors.second.copy(alpha = functionAlpha)
                        },
                    fontSize = if (isScientificActivated) 26.sp else 11.sp,
                    fontWeight = if (isScientificActivated) FontWeight.ExtraBold else FontWeight.Medium,
                    textAlign = if (isScientificActivated) TextAlign.Center else TextAlign.End,
                    modifier =
                        Modifier
                            .align(functionOffset)
                            .padding(
                                if (isScientificActivated) 0.dp else 8.dp,
                            ).scale(functionScale),
                )

                // Playful indicator when scientific function is activated
                if (isScientificActivated) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                                .size(6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(24.dp),
                                ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitParenthesisButton(
    onOpenParenClick: () -> Unit,
    onCloseParenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh,
            ),
        label = "paren_scale",
    )

    val colors = getAdaptiveButtonColors(ButtonType.PARENTHESIS)

    Box(
        modifier =
            modifier
                .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = if (isPressed) 6.dp else 10.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = colors.first.copy(alpha = 0.2f),
                        spotColor = colors.first.copy(alpha = 0.3f),
                    ),
            shape = RoundedCornerShape(24.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = colors.first,
                ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Open parenthesis (top half)
                Button(
                    onClick = {
                        onOpenParenClick()
                        isPressed = true
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = colors.second,
                        ),
                    contentPadding = PaddingValues(0.dp),
                    elevation =
                        ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                        ),
                ) {
                    Text(
                        text = "(",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.second,
                    )
                }

// Divider
                HorizontalDivider(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(1.dp),
                    color = colors.second.copy(alpha = 0.3f),
                )

                // Close parenthesis (bottom half)
                Button(
                    onClick = {
                        onCloseParenClick()
                        isPressed = true
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = colors.second,
                        ),
                    contentPadding = PaddingValues(0.dp),
                    elevation =
                        ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                        ),
                ) {
                    Text(
                        text = ")",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.second,
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedZeroButton(
    onZeroClick: () -> Unit,
    onTripleZeroClick: () -> Unit,
    onDoubleZeroClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var totalDragDistance by remember { mutableStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh,
            ),
        label = "zero_scale",
    )

    val colors = getAdaptiveButtonColors(ButtonType.NUMBER)

    Box(
        modifier =
            modifier
                .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onZeroClick,
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = {
                                // Execute action based on drag direction on release
                                if (totalDragDistance < -60f) {
                                    onTripleZeroClick() // Dragged up
                                } else if (totalDragDistance > 60f) {
                                    onDoubleZeroClick() // Dragged down
                                }
                                isDragging = false
                                totalDragDistance = 0f
                            },
                            onDrag = { _, dragAmount ->
                                // Track cumulative drag for zero button
                                val currentDrag = dragAmount.y
                                // Don't execute immediately, just track the drag
                            },
                        )
                    }.shadow(
                        elevation =
                            if (isPressed) {
                                4.dp
                            } else if (isDragging) {
                                16.dp
                            } else {
                                12.dp
                            },
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = colors.first.copy(alpha = 0.2f),
                        spotColor = colors.first.copy(alpha = 0.4f),
                    ),
            shape = RoundedCornerShape(24.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colors.first,
                    contentColor = colors.second,
                ),
            contentPadding = PaddingValues(0.dp),
            elevation =
                ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                // Main zero (center)
                Text(
                    text = "0",
                    color = colors.second,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )

// Triple zero indicator (top right corner) - highlight when dragging up
                val upIndicatorAlpha =
                    if (totalDragDistance < -30f) {
                        1f
                    } else if (isDragging) {
                        0.7f
                    } else {
                        0.4f
                    }
                Text(
                    text = "000",
                    color = colors.second.copy(alpha = upIndicatorAlpha),
                    fontSize = if (totalDragDistance < -30f) 12.sp else 8.sp,
                    fontWeight = if (totalDragDistance < -30f) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                )

                // Double zero indicator (bottom right corner) - highlight when dragging down
                val downIndicatorAlpha =
                    if (totalDragDistance > 30f) {
                        1f
                    } else if (isDragging) {
                        0.7f
                    } else {
                        0.4f
                    }
                Text(
                    text = "00",
                    color = colors.second.copy(alpha = downIndicatorAlpha),
                    fontSize = if (totalDragDistance > 30f) 12.sp else 8.sp,
                    fontWeight = if (totalDragDistance > 30f) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp),
                )
            }
        }
    }
}

@Composable
private fun EnhancedEqualsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    var showSparkles by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh,
            ),
        label = "equals_scale",
    )

    val sparkleAlpha by animateFloatAsState(
        targetValue = if (showSparkles) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "sparkle_alpha",
    )

    Box(
        modifier =
            modifier
                .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = {
                onClick()
                isPressed = true
                showSparkles = true
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = if (isPressed) 8.dp else 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    ),
            shape = RoundedCornerShape(24.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            contentPadding = PaddingValues(0.dp),
            elevation =
                ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
        ) {
            Text(
                text = "=",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

// Award-winning sparkle constellation effect
        if (showSparkles) {
            // Primary sparkle ring
            repeat(8) { index ->
                val angle = (index * 45f) // 360/8 = 45 degrees apart
                val radius = 40.dp
                val sparkleSize = (3 + (index % 3)).dp

                Box(
                    modifier =
                        Modifier
                            .offset(
                                x = (radius.value * kotlin.math.cos(Math.toRadians(angle.toDouble()))).dp,
                                y = (radius.value * kotlin.math.sin(Math.toRadians(angle.toDouble()))).dp,
                            ).size(sparkleSize)
                            .background(
                                brush =
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = sparkleAlpha),
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = sparkleAlpha * 0.3f),
                                                Color.Transparent,
                                            ),
                                    ),
                                shape = RoundedCornerShape(sparkleSize / 2),
                            ),
                )
            }

            // Secondary inner sparkles
            repeat(4) { index ->
                val angle = (index * 90f + 22.5f) // Offset by 22.5 degrees
                val radius = 25.dp
                val sparkleSize = 2.dp

                Box(
                    modifier =
                        Modifier
                            .offset(
                                x = (radius.value * kotlin.math.cos(Math.toRadians(angle.toDouble()))).dp,
                                y = (radius.value * kotlin.math.sin(Math.toRadians(angle.toDouble()))).dp,
                            ).size(sparkleSize)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = sparkleAlpha * 0.8f),
                                shape = RoundedCornerShape(1.dp),
                            ),
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }

    LaunchedEffect(showSparkles) {
        if (showSparkles) {
            kotlinx.coroutines.delay(600)
            showSparkles = false
        }
    }
}

@Composable
private fun CalculatorButtonRow(
    buttons: List<CalculatorButton>,
    onButtonClick: (CalculatorButton) -> Unit,
    onSecondaryClick: (CalculatorButton) -> Unit,
    showSecondary: Boolean = false,
    isJoyfulRow: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        buttons.forEach { button ->
            JoyfulCalculatorButton(
                button = button,
                onClick = { onButtonClick(button) },
                onSecondaryClick = { onSecondaryClick(button) },
                showSecondary = showSecondary,
                modifier = Modifier.weight(1f),
                isSpecial = button.type == ButtonType.SPECIAL,
                isOperator = button.type == ButtonType.OPERATOR,
                isJoyful = isJoyfulRow,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun JoyfulCalculatorButton(
    button: CalculatorButton,
    onClick: () -> Unit,
    onSecondaryClick: () -> Unit = {},
    showSecondary: Boolean = false,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isOperator: Boolean = false,
    isEquals: Boolean = false,
    isJoyful: Boolean = false,
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue =
            if (isPressed) {
                0.88f
            } else if (isHovered) {
                1.05f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh,
            ),
        label = "button_scale",
    )

    val colors =
        when {
            isEquals -> Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            isOperator -> getJoyfulButtonColors(ButtonType.OPERATOR)
            isSpecial -> getJoyfulButtonColors(ButtonType.SPECIAL)
            else -> getJoyfulButtonColors(button.type)
        }

    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    Box(
        modifier =
            modifier
                .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onClick,
            modifier =
                Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation =
                            if (isPressed) {
                                1.dp
                            } else if (isEquals) {
                                12.dp
                            } else {
                                6.dp
                            },
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = colors.first.copy(alpha = 0.2f),
                        spotColor = colors.first.copy(alpha = 0.4f),
                    ),
            shape = RoundedCornerShape(24.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colors.first,
                    contentColor = colors.second,
                ),
            contentPadding = PaddingValues(0.dp),
            interactionSource = interactionSource,
            elevation =
                ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = button.primary,
                    color = colors.second,
                    fontSize =
                        when {
                            isEquals -> 30.sp
                            isOperator -> 24.sp
                            button.primary.length > 2 -> 14.sp
                            else -> 22.sp
                        },
                    fontWeight =
                        when {
                            isEquals -> FontWeight.Bold
                            isOperator -> FontWeight.SemiBold
                            else -> FontWeight.Medium
                        },
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Joy sparkle effect for equals button
        if (isEquals && isPressed) {
            repeat(3) { index ->
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding((4 + index * 2).dp)
                            .size((3 + index).dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(24.dp),
                            ),
                )
            }
        }
    }
}

// Award-winning Material 3 Expressive button color system with dynamic theming
@Composable
private fun getAdaptiveButtonColors(type: ButtonType): Pair<Color, Color> =
    when (type) {
        ButtonType.NUMBER ->
            Pair(
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                MaterialTheme.colorScheme.onSurface,
            )

        ButtonType.OPERATOR ->
            Pair(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.onPrimaryContainer,
            )

        ButtonType.FUNCTION ->
            Pair(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.onSecondaryContainer,
            )

        ButtonType.SPECIAL ->
            Pair(
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                MaterialTheme.colorScheme.onTertiaryContainer,
            )

        ButtonType.PARENTHESIS ->
            Pair(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }

@Composable
private fun getJoyfulButtonColors(type: ButtonType): Pair<Color, Color> = getAdaptiveButtonColors(type)
