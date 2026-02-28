package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import org.solovyev.android.calculator.EditorState

/**
 * Calculator editor component that shows the input expression with a modern Material3 design.
 *
 * Features:
 * - Syntax highlighting (numbers, operators, functions, constants)
 * - Matching parentheses highlighting
 * - Error underline for invalid syntax
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
 * @param maxTextSize Maximum text size for auto-resizing
 */
import androidx.compose.foundation.layout.Row

@OptIn(ExperimentalComposeUiApi::class)
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
    var fieldWidthPx by remember { mutableIntStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }
    var editorFocused by remember { mutableStateOf(false) }
    var cursorPosition by remember { mutableIntStateOf(0) }
    val disableSoftKeyboardInterceptor = remember {
        PlatformTextInputInterceptor { _, _ -> awaitCancellation() }
    }

    // Track last synced values to detect meaningful external changes
    var lastExternalText by remember { mutableStateOf("") }
    var lastExternalSelection by remember { mutableIntStateOf(-1) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    // Only sync from external state when text/selection changed (ignore sequence-only updates).
    // Preserve active local range selection while focused so users can select/copy reliably.
    LaunchedEffect(state.sequence, state.text, state.selection) {
        val newText = state.text.toString()
        val newSelection = state.selection.coerceIn(0, newText.length)

        val textChanged = newText != lastExternalText
        val selectionChanged = newSelection != lastExternalSelection
        if (!textChanged && !selectionChanged) {
            return@LaunchedEffect
        }

        lastExternalText = newText
        lastExternalSelection = newSelection

        if (textChanged) {
            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(newSelection)
            )
            cursorPosition = newSelection
            return@LaunchedEffect
        }

        val hasActiveLocalRangeSelection =
            editorFocused && !textFieldValue.selection.collapsed && textFieldValue.text == newText
        if (hasActiveLocalRangeSelection) {
            return@LaunchedEffect
        }

        val targetSelection = TextRange(newSelection)
        if (textFieldValue.selection != targetSelection || textFieldValue.text != newText) {
            textFieldValue = textFieldValue.copy(
                text = newText,
                selection = targetSelection
            )
            cursorPosition = newSelection
        }
    }

    // Reset scroll to start on initial load
    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
    }

    // Auto-scroll to keep cursor visible (only after user interaction)
    LaunchedEffect(textFieldValue.selection, textLayoutResult, fieldWidthPx) {
        if (!hasUserInteracted) {
            return@LaunchedEffect
        }
        delay(50)
        val layout = textLayoutResult ?: return@LaunchedEffect
        val textLength = textFieldValue.text.length
        val layoutTextLength = layout.layoutInput.text.length

        if (fieldWidthPx == 0 || textLength == 0 || layoutTextLength != textLength) {
            return@LaunchedEffect
        }

        val cursorPos = textFieldValue.selection.end.coerceIn(0, textLength)
        if (cursorPos < 0 || cursorPos > layoutTextLength) {
            return@LaunchedEffect
        }

        cursorPosition = cursorPos

        val cursorRect = layout.getCursorRect(cursorPos)
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
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            InterceptPlatformTextInput(
                interceptor = disableSoftKeyboardInterceptor
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val oldValue = textFieldValue
                        textFieldValue = newValue
                        cursorPosition = newValue.selection.end

                        if (newValue.text != oldValue.text) {
                            // Text changed - notify parent and mark interaction
                            hasUserInteracted = true
                            onTextChange(newValue.text, newValue.selection.start)
                        } else if (newValue.selection != oldValue.selection && newValue.selection.collapsed) {
                            // Only selection changed (user clicked/tapped) - notify parent
                            hasUserInteracted = true
                            onSelectionChange(newValue.selection.start)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .onFocusChanged { focusState ->
                            editorFocused = focusState.isFocused
                        }
                        .onSizeChanged { fieldWidthPx = it.width },
                    enabled = true,
                    readOnly = false,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.End,
                        fontFamily = CalculatorFontFamily
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.None,
                        autoCorrectEnabled = false,
                        showKeyboardOnFocus = false
                    ),
                    singleLine = true,
                    interactionSource = interactionSource,
                    onTextLayout = { textLayoutResult = it },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) { innerTextField() }
                    },
                    visualTransformation = if (highlightExpressions) {
                        SyntaxHighlightingVisualTransformation(
                            text = textFieldValue.text,
                            cursorPosition = cursorPosition,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            secondaryColor = MaterialTheme.colorScheme.secondary,
                            tertiaryColor = MaterialTheme.colorScheme.tertiary,
                            baseColor = MaterialTheme.colorScheme.onBackground,
                            surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            errorColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        VisualTransformation.None
                    }
                )
            }
        }
    }
}

private class SyntaxHighlightingVisualTransformation(
    private val text: String,
    private val cursorPosition: Int,
    private val primaryColor: Color,
    private val secondaryColor: Color,
    private val tertiaryColor: Color,
    private val baseColor: Color,
    private val surfaceVariantColor: Color,
    private val errorColor: Color
) : VisualTransformation {
    override fun filter(annotatedString: AnnotatedString): TransformedText {
        val highlighted = highlightSyntax(text, cursorPosition)
        return TransformedText(
            text = highlighted,
            offsetMapping = OffsetMapping.Identity
        )
    }

    private fun highlightSyntax(text: String, cursorPos: Int): AnnotatedString {
        val matchingParenIndices = findMatchingParentheses(text, cursorPos)
        val errorIndices = findSyntaxErrors(text)

        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                val char = text[i]

                // Check if this position has an error
                val hasError = i in errorIndices

                // Check if this position is a matching parenthesis
                val isMatchingParen = i in matchingParenIndices

                when {
                    char.isDigit() || char == '.' -> {
                        val start = i
                        while (i < text.length && (text[i].isDigit() || text[i] == '.' ||
                               text[i] == 'E' || text[i] == 'e' || text[i] == '_')) {
                            i++
                        }
                        withStyle(
                            SpanStyle(
                                color = secondaryColor,
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (hasError) TextDecoration.Underline else null
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
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (hasError) TextDecoration.Underline else null
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

                        when {
                            isKnownFunction(word) -> {
                                withStyle(
                                    SpanStyle(
                                        color = primaryColor,
                                        fontStyle = FontStyle.Normal,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (hasError) TextDecoration.Underline else null
                                    )
                                ) {
                                    append(word)
                                }
                            }
                            isKnownConstant(word) -> {
                                withStyle(
                                    SpanStyle(
                                        color = secondaryColor,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic,
                                        textDecoration = if (hasError) TextDecoration.Underline else null
                                    )
                                ) {
                                    append(word)
                                }
                            }
                            else -> {
                                withStyle(
                                    SpanStyle(
                                        color = baseColor,
                                        textDecoration = if (hasError) TextDecoration.Underline else null
                                    )
                                ) {
                                    append(word)
                                }
                            }
                        }
                        continue
                    }

                    char in "()[]{}" -> {
                        val parenColor = when {
                            isMatchingParen -> primaryColor
                            hasError -> errorColor
                            else -> surfaceVariantColor
                        }
                        val parenWeight = when {
                            isMatchingParen -> FontWeight.ExtraBold
                            else -> FontWeight.Bold
                        }
                        val parenBackground = when {
                            isMatchingParen -> primaryColor.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }

                        withStyle(
                            SpanStyle(
                                color = parenColor,
                                fontWeight = parenWeight,
                                background = parenBackground
                            )
                        ) {
                            append(char)
                        }
                    }

                    char == ',' -> {
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
                        withStyle(
                            SpanStyle(
                                color = baseColor,
                                textDecoration = if (hasError) TextDecoration.Underline else null
                            )
                        ) {
                            append(char)
                        }
                    }
                }
                i++
            }
        }
    }

    private fun findMatchingParentheses(text: String, cursorPos: Int): Set<Int> {
        val result = mutableSetOf<Int>()
        if (cursorPos < 0 || cursorPos > text.length) return result

        // Check if cursor is at or just after a parenthesis
        val checkPositions = listOf(cursorPos - 1, cursorPos).filter { it in text.indices }

        for (pos in checkPositions) {
            when (text[pos]) {
                '(', '[', '{' -> {
                    val matching = findClosingMatch(text, pos, text[pos], getClosingBracket(text[pos]))
                    if (matching >= 0) {
                        result.add(pos)
                        result.add(matching)
                    }
                }
                ')', ']', '}' -> {
                    val matching = findOpeningMatch(text, pos, text[pos], getOpeningBracket(text[pos]))
                    if (matching >= 0) {
                        result.add(pos)
                        result.add(matching)
                    }
                }
            }
        }

        return result
    }

    private fun getClosingBracket(opening: Char): Char = when (opening) {
        '(' -> ')'
        '[' -> ']'
        '{' -> '}'
        else -> opening
    }

    private fun getOpeningBracket(closing: Char): Char = when (closing) {
        ')' -> '('
        ']' -> '['
        '}' -> '{'
        else -> closing
    }

    private fun findClosingMatch(text: String, start: Int, opening: Char, closing: Char): Int {
        var depth = 1
        var i = start + 1
        while (i < text.length && depth > 0) {
            when (text[i]) {
                opening -> depth++
                closing -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return -1
    }

    private fun findOpeningMatch(text: String, start: Int, closing: Char, opening: Char): Int {
        var depth = 1
        var i = start - 1
        while (i >= 0 && depth > 0) {
            when (text[i]) {
                closing -> depth++
                opening -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i--
        }
        return -1
    }

    private fun findSyntaxErrors(text: String): Set<Int> {
        val errors = mutableSetOf<Int>()

        // Check for mismatched parentheses
        val stack = mutableListOf<Pair<Char, Int>>()
        for ((i, char) in text.withIndex()) {
            when (char) {
                '(', '[', '{' -> stack.add(char to i)
                ')', ']', '}' -> {
                    if (stack.isEmpty()) {
                        errors.add(i)
                    } else {
                        val (last, _) = stack.removeAt(stack.lastIndex)
                        if (!areMatchingBrackets(last, char)) {
                            errors.add(i)
                        }
                    }
                }
            }
        }
        // Unclosed opening brackets are errors
        stack.forEach { (_, index) -> errors.add(index) }

        // Check for invalid number formats (e.g., multiple decimals)
        var inNumber = false
        var hasDecimal = false
        var numberStart = 0

        for ((i, char) in text.withIndex()) {
            when {
                char.isDigit() || char == '_' -> {
                    if (!inNumber) {
                        inNumber = true
                        numberStart = i
                        hasDecimal = false
                    }
                }
                char == '.' -> {
                    if (inNumber) {
                        if (hasDecimal) {
                            // Second decimal in same number - mark as error
                            for (j in numberStart..i) errors.add(j)
                        }
                        hasDecimal = true
                    }
                }
                else -> {
                    inNumber = false
                    hasDecimal = false
                }
            }
        }

        // Check for consecutive operators (except negative sign)
        val operators = setOf('+', '-', '*', '/', '^', '%', '÷', '×')
        for (i in 1 until text.length) {
            if (text[i] in operators && text[i - 1] in operators) {
                if (text[i] != '-' || (i > 0 && text[i - 1] in setOf('e', 'E'))) {
                    errors.add(i - 1)
                    errors.add(i)
                }
            }
        }

        return errors
    }

    private fun areMatchingBrackets(opening: Char, closing: Char): Boolean {
        return (opening == '(' && closing == ')') ||
               (opening == '[' && closing == ']') ||
               (opening == '{' && closing == '}')
    }

    private fun isOperator(char: Char): Boolean {
        return char in setOf(
            '+', '-', '*', '/', '÷', '×', '·',
            '^', '%', '=', '!', '<', '>',
            '≤', '≥', '≠', '∧', '∨', '⊕', '&', '|', '~'
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
            "deg", "rad", "re", "im", "arg",
            "sum", "prod", "product", "int", "integrate",
            "diff", "derivative", "lim", "limit"
        )
    }

    private fun isKnownConstant(word: String): Boolean {
        val lowerWord = word.lowercase()
        return lowerWord in setOf(
            "e", "pi", "π", "i", "j",
            "inf", "infinity", "∞", "nan",
            "true", "false", "yes", "no"
        )
    }
}
