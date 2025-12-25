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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.solovyev.android.calculator.EditorState

/**
 * Calculator editor component that shows the input expression with a modern Material3 design.
 *
 * Features:
 * - Syntax highlighting (numbers, operators, functions, constants)
 * - Blinking cursor animation at the current cursor position
 * - Full text selection support with custom Material3 colors
 * - Auto-scrolling to keep cursor visible
 * - Horizontal scrolling for long expressions
 * - Right-to-left text alignment for calculator-style input
 * - Clean, modern Material3 theming
 * - Auto-resizing text based on content length
 *
 * @param state The current editor state containing text and cursor selection
 * @param onTextChange Callback invoked when text changes
 * @param onSelectionChange Callback invoked when cursor selection changes
 * @param modifier Modifier to be applied to the editor
 * @param minTextSize Minimum text size for auto-resizing
 * @param maxTextSize Maximum text size for auto-resizing
 */
@Composable
fun CalculatorEditor(
    state: EditorState,
    onTextChange: (String) -> Unit,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minTextSize: TextUnit = 24.sp,
    maxTextSize: TextUnit = 36.sp
) {
    val scrollState = rememberScrollState()
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Update text field when state changes externally
    LaunchedEffect(state.sequence) {
        val newText = state.text.toString()
        val newSelection = state.selection.coerceIn(0, newText.length)

        if (textFieldValue.text != newText || textFieldValue.selection.start != newSelection) {
            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(newSelection)
            )
        }
    }

    // Auto-scroll to cursor position with smooth animation
    LaunchedEffect(textFieldValue.selection, textLayoutResult) {
        delay(50) // Small delay to ensure layout is complete
        val layout = textLayoutResult
        if (layout != null && textFieldValue.text.isNotEmpty()) {
            val cursorPosition = textFieldValue.selection.start
            if (cursorPosition in 0..textFieldValue.text.length) {
                // Scroll to show cursor
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    // Calculate dynamic font size based on text length
    val fontSize = remember(state.text) {
        calculateEditorFontSize(
            text = state.text.toString(),
            minSize = minTextSize,
            maxSize = maxTextSize
        )
    }

    // Custom text selection colors matching Material3 theme
    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            if (state.text.isEmpty()) {
                // Show standalone blinking cursor when empty
                BlinkingCursor(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onTextChange(newValue.text)
                        if (newValue.selection.start == newValue.selection.end) {
                            onSelectionChange(newValue.selection.start)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.End
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    onTextLayout = { textLayoutResult = it },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            innerTextField()
                        }
                    },
                    // Enable visual transformation for syntax highlighting
                    visualTransformation = SyntaxHighlightingVisualTransformation(
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        tertiaryColor = MaterialTheme.colorScheme.tertiary,
                        baseColor = MaterialTheme.colorScheme.onBackground,
                        surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * Blinking cursor animation for empty editor or standalone cursor indicator.
 */
@Composable
private fun BlinkingCursor(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CursorBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CursorAlpha"
    )

    Box(
        modifier = modifier
            .width(2.dp)
            .height(32.dp)
            .background(color.copy(alpha = alpha))
    )
}

/**
 * Calculates an appropriate font size based on text length.
 * Longer expressions get smaller font sizes for better fit.
 */
private fun calculateEditorFontSize(
    text: String,
    minSize: TextUnit,
    maxSize: TextUnit
): TextUnit {
    val length = text.length
    val size = when {
        length == 0 -> maxSize
        length < 10 -> maxSize
        length < 15 -> 32.sp
        length < 20 -> 28.sp
        length < 30 -> 26.sp
        else -> minSize
    }

    // Coerce between min and max using value comparison
    return when {
        size.value < minSize.value -> minSize
        size.value > maxSize.value -> maxSize
        else -> size
    }
}

/**
 * Visual transformation that applies syntax highlighting to calculator expressions.
 * Highlights different token types with distinct colors.
 */
private class SyntaxHighlightingVisualTransformation(
    private val primaryColor: androidx.compose.ui.graphics.Color,
    private val secondaryColor: androidx.compose.ui.graphics.Color,
    private val tertiaryColor: androidx.compose.ui.graphics.Color,
    private val baseColor: androidx.compose.ui.graphics.Color,
    private val surfaceVariantColor: androidx.compose.ui.graphics.Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = highlightSyntax(text.text)
        return TransformedText(
            text = annotatedString,
            offsetMapping = OffsetMapping.Identity
        )
    }

    /**
     * Applies syntax highlighting to the input text.
     * Highlights numbers, operators, functions, constants, and brackets.
     */
    private fun highlightSyntax(text: String): AnnotatedString {
        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                val char = text[i]

                when {
                    // Numbers (including decimal points and scientific notation)
                    char.isDigit() || char == '.' -> {
                        val start = i
                        while (i < text.length && (text[i].isDigit() || text[i] == '.' ||
                               text[i] == 'E' || text[i] == 'e')) {
                            i++
                        }
                        withStyle(
                            SpanStyle(
                                color = secondaryColor,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(text.substring(start, i))
                        }
                        continue
                    }

                    // Operators
                    isOperator(char) -> {
                        withStyle(
                            SpanStyle(
                                color = tertiaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(char)
                        }
                    }

                    // Functions and constants (letters)
                    char.isLetter() -> {
                        val start = i
                        while (i < text.length && (text[i].isLetter() || text[i].isDigit() || text[i] == '_')) {
                            i++
                        }
                        val word = text.substring(start, i)

                        if (isKnownFunction(word)) {
                            withStyle(
                                SpanStyle(
                                    color = primaryColor,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(word)
                            }
                        } else if (isKnownConstant(word)) {
                            withStyle(
                                SpanStyle(
                                    color = secondaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append(word)
                            }
                        } else {
                            withStyle(SpanStyle(color = baseColor)) {
                                append(word)
                            }
                        }
                        continue
                    }

                    // Parentheses and brackets
                    char in "()[]{}," -> {
                        withStyle(
                            SpanStyle(
                                color = surfaceVariantColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(char)
                        }
                    }

                    // Default - whitespace and other characters
                    else -> {
                        withStyle(SpanStyle(color = baseColor)) {
                            append(char)
                        }
                    }
                }
                i++
            }
        }
    }

    /**
     * Checks if a character is a mathematical operator.
     */
    private fun isOperator(char: Char): Boolean {
        return char in setOf(
            '+', '-', '*', '/', '÷', '×', '·',
            '^', '%', '=', '!', '<', '>',
            '≤', '≥', '≠', '∧', '∨', '⊕'
        )
    }

    /**
     * Checks if a word is a known mathematical function.
     */
    private fun isKnownFunction(word: String): Boolean {
        val lowerWord = word.lowercase()
        return lowerWord in setOf(
            // Trigonometric functions
            "sin", "cos", "tan", "cot", "sec", "csc",
            "asin", "acos", "atan", "acot", "asec", "acsc",
            "arcsin", "arccos", "arctan", "arccot", "arcsec", "arccsc",

            // Hyperbolic functions
            "sinh", "cosh", "tanh", "coth", "sech", "csch",
            "asinh", "acosh", "atanh", "acoth", "asech", "acsch",
            "arcsinh", "arccosh", "arctanh", "arccoth", "arcsech", "arccsch",

            // Logarithmic and exponential
            "ln", "log", "lg", "log10", "log2", "exp",

            // Root functions
            "sqrt", "cbrt", "root",

            // Absolute and sign
            "abs", "sgn", "sign",

            // Rounding functions
            "ceil", "floor", "round", "trunc",

            // Statistical and comparison
            "max", "min", "gcd", "lcm",

            // Other mathematical functions
            "fact", "factorial", "mod", "rem",
            "deg", "rad", "re", "im", "arg"
        )
    }

    /**
     * Checks if a word is a known mathematical constant.
     */
    private fun isKnownConstant(word: String): Boolean {
        val lowerWord = word.lowercase()
        return lowerWord in setOf(
            "e", "pi", "π", "i", "j",
            "inf", "infinity", "nan",
            "true", "false"
        )
    }
}
