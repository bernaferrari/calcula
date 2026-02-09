package org.solovyev.android.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import kotlin.math.roundToInt
import org.solovyev.android.calculator.EditorState

/**
 * Calculator editor component that shows the input expression with a modern Material3 design.
 *
 * Features:
 * - Syntax highlighting (numbers, operators, functions, constants)
 * - Blinking cursor animation at the current cursor position
 * - Full text selection support with custom Material3 colors
 * - Click-to-position cursor support
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
 *  * @param maxTextSize Maximum text size for auto-resizing
 */
import androidx.compose.foundation.layout.Row

@Composable
fun CalculatorEditor(
    state: EditorState,
    onTextChange: (String, Int) -> Unit,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightExpressions: Boolean = true,
    minTextSize: TextUnit = 38.sp,
    maxTextSize: TextUnit = 38.sp
) {
    val scrollState = rememberScrollState()
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var fieldWidthPx by remember { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    
    // Track last synced values to detect external vs internal changes
    var lastExternalSequence by remember { mutableStateOf(-1L) }
    var lastExternalText by remember { mutableStateOf("") }
    
    // Only sync from external state when it actually changes from outside
    LaunchedEffect(state.sequence, state.text) {
        val newText = state.text.toString()
        
        // Detect if this is an external change (from calculator engine)
        if (state.sequence != lastExternalSequence || newText != lastExternalText) {
            lastExternalSequence = state.sequence
            lastExternalText = newText
            val newSelection = state.selection.coerceIn(0, newText.length)
            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(newSelection)
            )
        }
    }

    // Auto-scroll to keep cursor visible
    LaunchedEffect(textFieldValue.selection, textLayoutResult, fieldWidthPx) {
        delay(50)
        val layout = textLayoutResult ?: return@LaunchedEffect
        val textLength = textFieldValue.text.length
        val layoutTextLength = layout.layoutInput.text.length
        
        if (fieldWidthPx == 0 || textLength == 0 || layoutTextLength != textLength) {
            return@LaunchedEffect
        }
        
        val cursorPosition = textFieldValue.selection.end.coerceIn(0, textLength)
        if (cursorPosition < 0 || cursorPosition > layoutTextLength) {
            return@LaunchedEffect
        }
        
        val cursorRect = layout.getCursorRect(cursorPosition)
        val current = scrollState.value.toFloat()
        val minVisible = current
        val maxVisible = current + fieldWidthPx
        val target = when {
            cursorRect.left < minVisible -> cursorRect.left
            cursorRect.right > maxVisible -> cursorRect.right - fieldWidthPx
            else -> current
        }.coerceIn(0f, scrollState.maxValue.toFloat())
        scrollState.animateScrollTo(target.roundToInt())
    }

    val fontSize = maxTextSize

    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .heightIn(min = 72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val oldValue = textFieldValue
                    textFieldValue = newValue
                    
                    if (newValue.text != oldValue.text) {
                        // Text changed - notify parent
                        onTextChange(newValue.text, newValue.selection.start)
                    } else if (newValue.selection != oldValue.selection && newValue.selection.collapsed) {
                        // Only selection changed (user clicked/tapped) - notify parent
                        onSelectionChange(newValue.selection.start)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .focusRequester(focusRequester)
                    .onSizeChanged { fieldWidthPx = it.width },
                enabled = true,
                readOnly = true, // Prevent system keyboard from opening
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start,
                    fontFamily = CalculatorFontFamily
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.None,
                    autoCorrectEnabled = false,
                    showKeyboardOnFocus = true
                ),
                singleLine = true,
                interactionSource = interactionSource,
                onTextLayout = { textLayoutResult = it },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) { innerTextField() }
                },
                visualTransformation = if (highlightExpressions) {
                    SyntaxHighlightingVisualTransformation(
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary,
                        tertiaryColor = MaterialTheme.colorScheme.tertiary,
                        baseColor = MaterialTheme.colorScheme.onBackground,
                        surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    VisualTransformation.None
                }
            )
        }
    }
}

private class SyntaxHighlightingVisualTransformation(
    private val primaryColor: Color,
    private val secondaryColor: Color,
    private val tertiaryColor: Color,
    private val baseColor: Color,
    private val surfaceVariantColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = highlightSyntax(text.text)
        return TransformedText(
            text = annotatedString,
            offsetMapping = OffsetMapping.Identity
        )
    }

    private fun highlightSyntax(text: String): AnnotatedString {
        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                val char = text[i]

                when {
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

                    char in "()[]{},\u0000" -> {
                        withStyle(
                            SpanStyle(
                                color = surfaceVariantColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(char)
                        }
                    }

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

    private fun isOperator(char: Char): Boolean {
        return char in setOf(
            '+', '-', '*', '/', '÷', '×', '·',
            '^', '%', '=', '!', '<', '>',
            '≤', '≥', '≠', '∧', '∨', '⊕'
        )
    }

    private fun isKnownFunction(word: String): Boolean {
        val lowerWord = word.lowercase()
        return lowerWord in setOf(
            "sin", "cos", "tan", "cot", "sec", "csc",
            "asin", "acos", "atan", "acot", "asec", "acsc",
            "arcsin", "arccos", "arctan", "arccot", "arcsec", "arccsc",
            "sinh", "cosh", "tanh", "coth", "sech", "csch",
            "asinh", "acosh", "atanh", "acoth", "asech", "acsch",
            "arcsinh", "arccosh", "arctanh", "arccoth", "arcsech", "arccsch",
            "ln", "log", "lg", "log10", "log2", "exp",
            "sqrt", "cbrt", "root",
            "abs", "sgn", "sign",
            "ceil", "floor", "round", "trunc",
            "max", "min", "gcd", "lcm",
            "fact", "factorial", "mod", "rem",
            "deg", "rad", "re", "im", "arg"
        )
    }

    private fun isKnownConstant(word: String): Boolean {
        val lowerWord = word.lowercase()
        return lowerWord in setOf(
            "e", "pi", "π", "i", "j",
            "inf", "infinity", "nan",
            "true", "false"
        )
    }
}
