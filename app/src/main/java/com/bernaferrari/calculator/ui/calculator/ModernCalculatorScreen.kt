package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.bernaferrari.calculator.domain.calculator.TokenizedExpression
import com.bernaferrari.calculator.domain.calculator.TokenType
import com.bernaferrari.calculator.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    initialExpression: String = "",
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGraph: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Gesture states for undo/redo
    var swipeOffset by remember { mutableStateOf(0f) }
    val swipeProgress by animateFloatAsState(
        targetValue = if (swipeOffset != 0f) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )
    
    // Active button animation
    var activeButton by remember { mutableStateOf<String?>(null) }
    
    // Celebration state
    var showCelebration by remember { mutableStateOf(false) }
    
    // Step-by-step dialog state
    var showStepByStep by remember { mutableStateOf(false) }
    
    // Flying text animations
    var flyingTexts by remember { mutableStateOf<List<FlyingText>>(emptyList()) }
    var cursorPixelPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Track newly added text for fade-in effect
    var recentlyAddedPosition by remember { mutableStateOf(-1) }
    var recentlyAddedLength by remember { mutableStateOf(0) }
    
    // Track text position for shader ripple effect
    var textCenterPosition by remember { mutableStateOf<Offset?>(null) }
    var shaderRipplePosition by remember { mutableStateOf<Offset?>(null) }
    
    // Load initial expression if provided
    LaunchedEffect(initialExpression) {
        if (initialExpression.isNotEmpty()) {
            viewModel.updateExpression(initialExpression)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(swipeOffset) > 150) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (swipeOffset > 0) {
                                viewModel.undo()
                            } else {
                                viewModel.redo()
                            }
                        }
                        swipeOffset = 0f
                    }
                ) { _, dragAmount ->
                    swipeOffset += dragAmount
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
.background(MaterialTheme.colorScheme.background)
        ) {
            // Clean Material 3 top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Calculator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Memory indicator
                if (state.hasMemoryValue) {
                    Text(
                        text = "M",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(onClick = onNavigateToHistory) {
                    Icon(
                        Icons.Default.History, 
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings, 
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            }
            
            // Remove navigation controls to save space
            
            
            // Compact smart display - TESTING
            SmartDisplay(
                expression = state.currentExpression,
                result = state.currentResult,
                isError = state.isError,
                errorMessage = state.errorMessage,
                cursorPosition = state.cursorPosition,
                recentlyAddedPosition = recentlyAddedPosition,
                recentlyAddedLength = recentlyAddedLength,
                showStepByStep = showStepByStep,
                onExpressionChange = { viewModel.updateExpression(it) },
                onCursorChange = { viewModel.updateCursorPosition(it) },
                onCursorPositioned = { cursorPixelPosition = it },
                onTextCenterPositioned = { textCenterPosition = it },
                onFadeInComplete = { 
                    recentlyAddedPosition = -1
                    recentlyAddedLength = 0
                },
                onCalculate = { 
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onButtonClick(
                        CalculatorButton("equals", "=", null, ButtonType.SPECIAL, ButtonFunction.EQUALS)
                    )
                },
                onShowStepByStep = { showStepByStep = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp)
            )
            
            
            // Remove suggestions to save space
            
            // Calculator buttons with flying animations
            SmartButtonGrid(
                onButtonClick = { button ->
                    activeButton = button.id
                    scope.launch {
                        delay(100)
                        activeButton = null
                    }
                    
                    // Different haptic patterns
                    when (button.type) {
                        ButtonType.NUMBER -> hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        ButtonType.OPERATOR -> hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        else -> hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    
                    when (button.id) {
                        "arrow_left" -> viewModel.moveCursorLeft()
                        "arrow_right" -> viewModel.moveCursorRight()
                        else -> viewModel.onButtonClick(button)
                    }
                },
                onSecondaryActivate = { secondary, startPosition ->
                    // Track where text will be added
                    val currentLength = state.currentExpression.length
                    
                    // Add flying text animation
                    flyingTexts = flyingTexts + FlyingText(
                        text = secondary,
                        startPosition = startPosition,
                        endPosition = cursorPixelPosition,
                        isSpecial = secondary in listOf("π", "e") // Mark special constants
                    )
                    
                    // Track where text will be added
                    recentlyAddedPosition = currentLength
                    recentlyAddedLength = if (secondary in listOf("π", "e")) {
                        secondary.length
                    } else {
                        secondary.length + 1 // Function name + opening parenthesis
                    }
                    
                    // Don't activate the function yet - wait for animation to complete
                    scope.launch {
                        // Wait for flying animation to nearly complete
                        delay(280) // Slightly less than animation to prevent flash
                        
                        // Activate the secondary function
                        val secondaryButton = CalculatorButton(
                            id = secondary,
                            primary = secondary,
                            secondary = null,
                            type = ButtonType.FUNCTION,
                            function = getSecondaryFunction(secondary)
                        )
                        viewModel.onButtonClick(secondaryButton)
                    }
                },
                activeButton = activeButton,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Swipe indicator overlay
        AnimatedVisibility(
            visible = swipeProgress > 0.1f,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = if (swipeOffset > 0) {
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            }
                        )
                    )
            )
        }
        
        // Celebration overlay
        CelebrationEffect(
            isActive = showCelebration,
            onFinished = { showCelebration = false }
        )
        
        // Flying text overlay
        FlyingTextOverlay(
            flyingTexts = flyingTexts,
            onAnimationEnd = { flyingText ->
                flyingTexts = flyingTexts.filter { it.id != flyingText.id }
                // Trigger shader ripple at text position
                shaderRipplePosition = textCenterPosition
                // Extra haptic for special constants
                if (flyingText.isSpecial) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        )
        
        // Step-by-step dialog
        if (showStepByStep && state.currentExpression.isNotEmpty() && state.currentResult != "0") {
            StepByStepDialog(
                expression = state.currentExpression,
                result = state.currentResult,
                onDismiss = { showStepByStep = false }
            )
        }
    }
}

@Composable
private fun SmartDisplay(
    expression: String,
    result: String,
    isError: Boolean,
    errorMessage: String,
    cursorPosition: Int,
    recentlyAddedPosition: Int,
    recentlyAddedLength: Int,
    showStepByStep: Boolean,
    onExpressionChange: (String) -> Unit,
    onCursorChange: (Int) -> Unit,
    onCursorPositioned: (Offset) -> Unit,
    onTextCenterPositioned: (Offset) -> Unit,
    onFadeInComplete: () -> Unit,
    onCalculate: () -> Unit,
    onShowStepByStep: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track ripple trigger
    var rippleOrigin by remember { mutableStateOf<Offset?>(null) }
    var textCenterPosition by remember { mutableStateOf<Offset?>(null) }
    val hapticFeedback = LocalHapticFeedback.current
    
    LaunchedEffect(recentlyAddedPosition, textCenterPosition) {
        if (recentlyAddedPosition >= 0 && textCenterPosition != null) {
            // Trigger ripple from text center position
            rippleOrigin = textCenterPosition
        }
    }
    
    // Gesture states
    var displayScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = displayScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Clean Material 3 SmartDisplay - constrained height
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .wrapContentHeight()
            .heightIn(max = 200.dp) // Constrain maximum height
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .wrapContentHeight()
        ) {
            // Simple text field for expression
            var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
            var fieldCoordinates by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
            val textMeasurer = rememberTextMeasurer()
            val density = LocalDensity.current
            
            // Get the text style once, outside of LaunchedEffect
            val textStyle = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End
            )
            
            // Calculate cursor position when expression or cursor position changes
            LaunchedEffect(expression, cursorPosition, textLayoutResult, fieldCoordinates) {
                fieldCoordinates?.let { coordinates ->
                    val position = coordinates.positionInRoot()
                    
                    if (expression.isEmpty()) {
                        // For empty field, position at the end (right side) vertically centered
                        val center = position + Offset(
                            coordinates.size.width.toFloat(),
                            coordinates.size.height.toFloat() / 2
                        )
                        onCursorPositioned(center)
                        onTextCenterPositioned(center)
                    } else {
                        // Use text measurer to get accurate cursor position
                        val layoutResult = textMeasurer.measure(
                            text = expression,
                            style = textStyle,
                            constraints = androidx.compose.ui.unit.Constraints(
                                maxWidth = coordinates.size.width
                            )
                        )
                        
                        // Get cursor position - for end-aligned text, we need to account for the text width
                        val cursorX = if (cursorPosition >= expression.length) {
                            // At the end of text
                            coordinates.size.width.toFloat()
                        } else if (cursorPosition == 0) {
                            // At the beginning - calculate where text starts for end-aligned text
                            coordinates.size.width.toFloat() - layoutResult.size.width.toFloat()
                        } else {
                            // In the middle - measure text up to cursor position
                            val textToCursor = expression.substring(0, cursorPosition)
                            val partialLayout = textMeasurer.measure(
                                text = textToCursor,
                                style = textStyle
                            )
                            coordinates.size.width.toFloat() - layoutResult.size.width.toFloat() + partialLayout.size.width.toFloat()
                        }
                        
                        val cursorY = coordinates.size.height.toFloat() / 2
                        
                        val cursorPos = position + Offset(cursorX, cursorY)
                        onCursorPositioned(cursorPos)
                        onTextCenterPositioned(cursorPos)
                    }
                }
            }
            
            BasicTextField(
                value = expression,
                onValueChange = { newValue ->
                    onExpressionChange(newValue)
                    // Update cursor position for flying text
                    onCursorChange(newValue.length)
                },
                textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                onTextLayout = { layoutResult ->
                    textLayoutResult = layoutResult
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp, max = 120.dp) // Constrain height
                    .onGloballyPositioned { coordinates ->
                        fieldCoordinates = coordinates
                    },
                decorationBox = { innerTextField ->
                    if (expression.isEmpty()) {
                        Text(
                            text = "0",
                            style = textStyle.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Beautiful result display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated equals button
                val buttonScale by animateFloatAsState(
                    targetValue = if (expression.isNotEmpty()) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                
                Surface(
                    onClick = onCalculate,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(buttonScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    if (expression.isNotEmpty() && !isError) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onShowStepByStep(true)
                                    }
                                }
                            )
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            "=",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Animated result with smart sizing
                AnimatedContent(
                    targetState = if (isError) errorMessage else result,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.95f)) togetherWith
                        (fadeOut() + scaleOut(targetScale = 1.05f))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) { displayText ->
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = when {
                                displayText.length > 15 -> 22.sp
                                displayText.length > 10 -> 28.sp
                                displayText.length > 6 -> 32.sp
                                else -> 36.sp
                            },
                            fontWeight = FontWeight.Light,
                            color = if (isError) MaterialTheme.colorScheme.error 
                                   else MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        ),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenDisplay(
    expression: String,
    cursorPosition: Int,
    recentlyAddedPosition: Int,
    recentlyAddedLength: Int,
    onPositioned: (Offset) -> Unit,
    onTextCenterPositioned: (Offset) -> Unit,
    onFadeInComplete: () -> Unit
) {
    val tokens = remember(expression) {
        if (expression.isEmpty()) emptyList()
        else TokenizedExpression.parse(expression).getTokens()
    }
    
    val cursorAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Text layout state for accurate cursor positioning
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    var containerPosition by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp, max = 80.dp)
            .onGloballyPositioned { coordinates ->
                containerPosition = coordinates.positionInRoot()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        if (expression.isEmpty()) {
            // For empty expression, position cursor at start with proper vertical centering
            LaunchedEffect(containerPosition) {
                val halfHeight = with(density) { 28.dp.toPx() } // Half of 56dp min height
                onPositioned(containerPosition + Offset(0f, halfHeight))
            }
            
            Text(
                "0",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp
                )
            )
            
            // Cursor as overlay
            Text(
                "|",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAnimation),
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp
                ),
                modifier = Modifier.offset(x = (-4).dp) // Slight offset to align with text start
            )
        } else {
            
            // Build annotated string for display
            val annotatedString = buildAnnotatedString {
                var currentPos = 0
                tokens.forEach { token ->
                    withStyle(
                        SpanStyle(
                            color = when (token.type) {
                                TokenType.NUMBER -> MaterialTheme.colorScheme.onSurface
                                TokenType.OPERATOR -> MaterialTheme.colorScheme.primary
                                TokenType.FUNCTION -> MaterialTheme.colorScheme.secondary
                                TokenType.CONSTANT -> MaterialTheme.colorScheme.tertiary
                                TokenType.PARENTHESIS_OPEN, 
                                TokenType.PARENTHESIS_CLOSE -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = when (token.type) {
                                TokenType.FUNCTION -> FontWeight.Medium
                                TokenType.OPERATOR -> FontWeight.Normal
                                else -> FontWeight.Light
                            },
                            fontSize = when (token.type) {
                                TokenType.OPERATOR -> 26.sp
                                else -> 28.sp
                            }
                        )
                    ) {
                        if (token.type == TokenType.FUNCTION) {
                            // Wrap function name in non-breaking wrapper
                            for (i in token.value.indices) {
                                append(token.value[i])
                                if (i < token.value.length - 1) {
                                    // Add zero-width no-break space between characters
                                    append("\u2060")
                                }
                            }
                        } else {
                            append(token.value)
                        }
                    }
                    currentPos += token.value.length
                }
            }
            
            // Display the text
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 0.5.sp,
                    lineHeight = 32.sp
                ),
                onTextLayout = { layoutResult ->
                    textLayoutResult = layoutResult
                },
                softWrap = true,
                maxLines = 3
            )
            
            // Calculate cursor position and render cursor as overlay
            var cursorOffset by remember { mutableStateOf(Offset.Zero) }
            
            LaunchedEffect(textLayoutResult, cursorPosition, containerPosition, annotatedString, recentlyAddedPosition) {
                textLayoutResult?.let { layout ->
                    // Map cursor position in expression to position in annotated string
                    var annotatedPos = 0
                    var exprPos = 0
                    
                    for (token in tokens) {
                        if (exprPos >= cursorPosition) break
                        
                        if (token.type == TokenType.FUNCTION) {
                            // Functions have word joiners between characters
                            val charsToSkip = minOf(token.value.length, cursorPosition - exprPos)
                            // Each character except the last has a word joiner after it
                            annotatedPos += if (charsToSkip > 0) charsToSkip * 2 - 1 else 0
                            exprPos += charsToSkip
                        } else {
                            val charsToSkip = minOf(token.value.length, cursorPosition - exprPos)
                            annotatedPos += charsToSkip
                            exprPos += charsToSkip
                        }
                    }
                    
                    val position = when {
                        cursorPosition == 0 -> {
                            // At start of text
                            val lineTop = if (layout.lineCount > 0) layout.getLineTop(0) else 0f
                            val lineBottom = if (layout.lineCount > 0) layout.getLineBottom(0) else layout.size.height.toFloat()
                            Offset(0f, (lineTop + lineBottom) / 2f)
                        }
                        cursorPosition >= expression.length -> {
                            // At end of text - need to handle properly
                            if (annotatedString.isNotEmpty() && layout.lineCount > 0) {
                                // Use the last character position to get the correct line
                                val lastCharPos = annotatedString.length - 1
                                val lastLine = layout.getLineForOffset(lastCharPos)
                                val lineTop = layout.getLineTop(lastLine)
                                val lineBottom = layout.getLineBottom(lastLine)
                                // Get position after last character
                                val horizontalPos = layout.getHorizontalPosition(annotatedString.length, true)
                                Offset(horizontalPos, (lineTop + lineBottom) / 2f)
                            } else {
                                Offset(0f, layout.size.height / 2f)
                            }
                        }
                        else -> {
                            // In middle of text
                            val adjustedPos = annotatedPos.coerceIn(0, annotatedString.length - 1)
                            val line = layout.getLineForOffset(adjustedPos)
                            val lineTop = layout.getLineTop(line)
                            val lineBottom = layout.getLineBottom(line)
                            val horizontalPos = layout.getHorizontalPosition(adjustedPos, true)
                            Offset(horizontalPos, (lineTop + lineBottom) / 2f)
                        }
                    }
                    
                    cursorOffset = position
                    onPositioned(containerPosition + position)
                    
                    // Calculate center position of recently added text
                    if (recentlyAddedPosition >= 0 && recentlyAddedLength > 0) {
                        // Map start and end positions to annotated string positions
                        var startAnnotatedPos = 0
                        var endAnnotatedPos = 0
                        var currentExprPos = 0
                        
                        // Find the token that contains our recently added text
                        for (token in tokens) {
                            if (currentExprPos <= recentlyAddedPosition && 
                                currentExprPos + token.value.length > recentlyAddedPosition) {
                                // This token contains our start position
                                val offsetInToken = recentlyAddedPosition - currentExprPos
                                
                                if (token.type == TokenType.FUNCTION) {
                                    // Account for word joiners
                                    startAnnotatedPos += offsetInToken * 2
                                    // For functions, exclude the parenthesis from length
                                    val functionNameLength = minOf(token.value.length, recentlyAddedLength - 1)
                                    endAnnotatedPos = startAnnotatedPos + (functionNameLength * 2 - 1)
                                } else {
                                    startAnnotatedPos += offsetInToken
                                    endAnnotatedPos = startAnnotatedPos + recentlyAddedLength
                                }
                                break
                            } else {
                                // Skip this token
                                if (token.type == TokenType.FUNCTION) {
                                    startAnnotatedPos += token.value.length * 2 - 1
                                } else {
                                    startAnnotatedPos += token.value.length
                                }
                                currentExprPos += token.value.length
                            }
                        }
                        
                        // Get the visual bounds
                        if (startAnnotatedPos < annotatedString.length && endAnnotatedPos <= annotatedString.length) {
                            val startOffset = layout.getHorizontalPosition(startAnnotatedPos, true)
                            val endOffset = layout.getHorizontalPosition(endAnnotatedPos, false)
                            
                            val line = layout.getLineForOffset(startAnnotatedPos)
                            
                            // Get the baseline of the line where the text is
                            val lineBaseline = layout.getLineBaseline(line)
                            
                            // Calculate vertical center based on the text metrics
                            // For typical text, the visual center is roughly at baseline - (font size * 0.3)
                            val fontSize = 28f // headlineMedium font size in sp
                            val fontSizePx = with(density) { fontSize.sp.toPx() }
                            
                            // The visual center of text is typically around 30-35% above the baseline
                            val visualCenterY = lineBaseline - (fontSizePx * 0.35f)
                            
                            // Center of the text horizontally
                            val centerX = (startOffset + endOffset) / 2f
                            
                            val textCenterOffset = containerPosition + Offset(centerX, visualCenterY)
                            onTextCenterPositioned(textCenterOffset)
                            
                        }
                    }
                }
            }
            
            // Render cursor as overlay
            if (textLayoutResult != null) {
                val primaryColor = MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier.offset {
                        IntOffset(
                            x = cursorOffset.x.toInt(),
                            y = cursorOffset.y.toInt()
                        )
                    }
                ) {
                    // Draw cursor as a vertical line
                    Canvas(
                        modifier = Modifier
                            .width(2.dp)
                            .height(with(density) { 32.sp.toDp() })
                            .offset(y = with(density) { (-16).sp.toDp() }) // Center vertically on the line
                    ) {
                        drawRect(
                            color = primaryColor.copy(alpha = cursorAnimation),
                            size = size
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartButtonGrid(
    onButtonClick: (CalculatorButton) -> Unit,
    onSecondaryActivate: (String, Offset) -> Unit,
    activeButton: String?,
    modifier: Modifier = Modifier
) {
    val buttons = remember { getSmartButtonLayout() }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        
        buttons.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { button ->
                    SmartButton(
                        button = button,
                        isActive = activeButton == button.id,
                        onClick = { onButtonClick(button) },
                        onSecondaryActivate = onSecondaryActivate,
                        modifier = Modifier.weight(
                            when {
                                button.id == "0" -> 2f
                                button.id == "equals" -> 2f
                                else -> 1f
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartButton(
    button: CalculatorButton,
    isActive: Boolean,
    onClick: () -> Unit,
    onSecondaryActivate: (String, Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var showingSecondary by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    var buttonPosition by remember { mutableStateOf(Offset.Zero) }
    val hapticFeedback = LocalHapticFeedback.current
    
    val scope = rememberCoroutineScope()
    
    // Threshold for activation (in dp converted to pixels)
    val activationThreshold = with(LocalDensity.current) { 40.dp.toPx() }
    val cancelThreshold = with(LocalDensity.current) { 20.dp.toPx() }
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed && !showingSecondary -> 0.92f
            showingSecondary -> 1.08f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    
    
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxSize()
            .scale(scale)
            .onGloballyPositioned { coordinates ->
                buttonPosition = coordinates.positionInRoot() + 
                    Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
            }
            .pointerInput(button) {
                if (button.secondary != null) {
                    detectVerticalDragGestures(
                        onDragStart = { 
                            isPressed = true
                            dragOffset = 0f
                            showingSecondary = false
                        },
                        onDragEnd = {
                            if (showingSecondary && button.secondary != null) {
                                onSecondaryActivate(button.secondary, buttonPosition)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            isPressed = false
                            showingSecondary = false
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            isPressed = false
                            showingSecondary = false
                            dragOffset = 0f
                        },
                        onVerticalDrag = { _, drag ->
                            dragOffset += drag
                            
                            // Check if we should activate secondary
                            val shouldShow = dragOffset < -activationThreshold
                            if (shouldShow != showingSecondary) {
                                showingSecondary = shouldShow
                                if (shouldShow) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            
                            // Cancel if dragged back down
                            if (showingSecondary && dragOffset > -cancelThreshold) {
                                showingSecondary = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    )
                } else {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
            },
        shape = RoundedCornerShape(
            when {
                button.id == "equals" -> 20.dp
                else -> 16.dp
            }
        ),
        color = when {
            button.id == "equals" -> MaterialTheme.colorScheme.primary
            button.id == "clear" -> MaterialTheme.colorScheme.errorContainer
            button.function in listOf(ButtonFunction.MEMORY_STORE, ButtonFunction.MEMORY_RECALL, 
                ButtonFunction.MEMORY_PLUS, ButtonFunction.MEMORY_MINUS, ButtonFunction.MEMORY_CLEAR) -> 
                MaterialTheme.colorScheme.tertiaryContainer
            button.type == ButtonType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
            button.type == ButtonType.NUMBER -> MaterialTheme.colorScheme.surface
            button.id == "backspace" -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.secondaryContainer
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = when {
                button.type == ButtonType.NUMBER -> 1.dp
                button.id == "equals" -> 0.dp
                else -> 0.dp
            },
            color = when {
                button.type == ButtonType.NUMBER -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                else -> Color.Transparent
            }
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = if (button.type == ButtonType.NUMBER) 12.dp else 10.dp)
        ) {
            // Main content
            AnimatedVisibility(
                visible = !showingSecondary,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Text(
                    text = button.primary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = when {
                            button.type == ButtonType.NUMBER -> 24.sp
                            button.id == "equals" -> 24.sp
                            button.id in listOf("clear", "arrow_left", "arrow_right") -> 18.sp
                            button.type == ButtonType.OPERATOR -> 22.sp
                            else -> 20.sp
                        },
                        fontWeight = when {
                            button.type == ButtonType.NUMBER -> FontWeight.Normal
                            button.id == "equals" -> FontWeight.Medium  
                            button.type == ButtonType.OPERATOR -> FontWeight.Normal
                            else -> FontWeight.Normal
                        },
                        color = when {
                            button.id == "equals" -> MaterialTheme.colorScheme.onPrimary
                            button.id == "clear" -> MaterialTheme.colorScheme.onErrorContainer
                            button.function in listOf(ButtonFunction.MEMORY_STORE, ButtonFunction.MEMORY_RECALL,
                                ButtonFunction.MEMORY_PLUS, ButtonFunction.MEMORY_CLEAR) ->
                                MaterialTheme.colorScheme.onTertiaryContainer
                            button.type == ButtonType.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
                            button.type == ButtonType.NUMBER -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        letterSpacing = 0.sp
                    )
                )
            }
            
            // Secondary function hint
            if (button.secondary != null && !showingSecondary) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .alpha(0.6f)
                ) {
                    Text(
                        text = button.secondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Large secondary when dragging - with better animation
            if (button.secondary != null) {
                val secondaryScale by animateFloatAsState(
                    targetValue = if (showingSecondary) 1.1f else 0f,
                    animationSpec = if (showingSecondary) {
                        spring(
                            dampingRatio = 0.6f,
                            stiffness = Spring.StiffnessHigh
                        )
                    } else {
                        tween(200, easing = FastOutSlowInEasing)
                    }
                )
                
                val secondaryAlpha by animateFloatAsState(
                    targetValue = if (showingSecondary) 1f else 0f,
                    animationSpec = tween(100)
                )
                
                if (secondaryAlpha > 0.01f) {
                    Text(
                        text = button.secondary,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .scale(secondaryScale)
                            .alpha(secondaryAlpha)
                    )
                }
            }
        }
    }
}

private fun getSmartButtonLayout(): List<List<CalculatorButton>> {
    return listOf(
        // Memory and control row
        listOf(
            CalculatorButton("ms", "MS", null, ButtonType.SPECIAL, ButtonFunction.MEMORY_STORE),
            CalculatorButton("mr", "MR", null, ButtonType.SPECIAL, ButtonFunction.MEMORY_RECALL),
            CalculatorButton("m_plus", "M+", null, ButtonType.SPECIAL, ButtonFunction.MEMORY_PLUS),
            CalculatorButton("mc", "MC", null, ButtonType.SPECIAL, ButtonFunction.MEMORY_CLEAR)
        ),
        // Control row
        listOf(
            CalculatorButton("clear", "C", null, ButtonType.SPECIAL, ButtonFunction.CLEAR),
            CalculatorButton("parenthesis", "( )", null, ButtonType.PARENTHESIS, ButtonFunction.PARENTHESIS),
            CalculatorButton("percent", "%", null, ButtonType.OPERATOR, ButtonFunction.PERCENT),
            CalculatorButton("divide", "÷", null, ButtonType.OPERATOR, ButtonFunction.DIVIDE)
        ),
        // Numbers 7-9
        listOf(
            CalculatorButton("7", "7", "π", ButtonType.NUMBER, ButtonFunction.SEVEN),
            CalculatorButton("8", "8", "e", ButtonType.NUMBER, ButtonFunction.EIGHT),
            CalculatorButton("9", "9", "x²", ButtonType.NUMBER, ButtonFunction.NINE),
            CalculatorButton("multiply", "×", null, ButtonType.OPERATOR, ButtonFunction.MULTIPLY)
        ),
        // Numbers 4-6
        listOf(
            CalculatorButton("4", "4", "ln", ButtonType.NUMBER, ButtonFunction.FOUR),
            CalculatorButton("5", "5", "log", ButtonType.NUMBER, ButtonFunction.FIVE),
            CalculatorButton("6", "6", "√", ButtonType.NUMBER, ButtonFunction.SIX),
            CalculatorButton("minus", "−", null, ButtonType.OPERATOR, ButtonFunction.MINUS)
        ),
        // Numbers 1-3
        listOf(
            CalculatorButton("1", "1", "sin", ButtonType.NUMBER, ButtonFunction.ONE),
            CalculatorButton("2", "2", "cos", ButtonType.NUMBER, ButtonFunction.TWO),
            CalculatorButton("3", "3", "tan", ButtonType.NUMBER, ButtonFunction.THREE),
            CalculatorButton("plus", "+", null, ButtonType.OPERATOR, ButtonFunction.PLUS)
        ),
        // Bottom number row
        listOf(
            CalculatorButton("0", "0", null, ButtonType.NUMBER, ButtonFunction.ZERO),
            CalculatorButton("dot", ".", null, ButtonType.NUMBER, ButtonFunction.DOT),
            CalculatorButton("backspace", "⌫", null, ButtonType.SPECIAL, ButtonFunction.BACKSPACE),
            CalculatorButton("equals", "=", null, ButtonType.SPECIAL, ButtonFunction.EQUALS)
        )
    )
}

private fun getSecondaryFunction(secondary: String): ButtonFunction {
    return when (secondary) {
        "sin" -> ButtonFunction.SIN
        "cos" -> ButtonFunction.COS
        "tan" -> ButtonFunction.TAN
        "ln" -> ButtonFunction.LN
        "log" -> ButtonFunction.LOG
        "√" -> ButtonFunction.SQRT
        "π" -> ButtonFunction.PI
        "e" -> ButtonFunction.E
        "x²" -> ButtonFunction.SQUARE
        else -> ButtonFunction.ZERO
    }
}


