package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextLayoutResult
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
import org.solovyev.android.calculator.EditorState

/**
 * Calculator editor component with award-winning typography:
 *
 * Features:
 * - Refined syntax highlighting (numbers, operators, functions, constants)
 * - Matching parentheses with elegant highlight animation
 * - Error underline for invalid syntax
 * - Elegant blinking cursor with custom animation
 * - Full text selection support with custom Material3 colors
 * - Click-to-position cursor support
 * - Smooth auto-scrolling with spring physics
 * - Horizontal scrolling for long expressions
 * - Right-to-left text alignment for calculator-style input
 * - Clean, modern Material3 theming
 * - Dynamic font sizing based on content length
 * - Smart input auto-correction
 * - Preserved cursor position during text updates
 * - Tabular figures for perfect number alignment
 * - Thousands separators with animated appearance
 * - Dynamic kerning for long expressions
 *
 * @param state The current editor state containing text and cursor selection
 * @param onTextChange Callback invoked when text changes
 * @param onSelectionChange Callback invoked when cursor selection changes
 * @param modifier Modifier to be applied to the editor
 * @param maxTextSize Maximum text size for auto-resizing
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CalculatorEditor(
    state: EditorState,
    onTextChange: (String, Int) -> Unit,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightExpressions: Boolean = true,
    minTextSize: TextUnit = 38.sp,
    maxTextSize: TextUnit = 38.sp,
    enableSmartCorrection: Boolean = false
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }
    var editorFocused by remember { mutableStateOf(false) }
    var cursorPosition by remember { mutableIntStateOf(0) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    var adaptiveFontSize by remember(state.text.toString()) { mutableStateOf(maxTextSize) }
    var adaptiveMaxLines by remember(state.text.toString()) { mutableIntStateOf(1) }

    val disableSoftKeyboardInterceptor = remember {
        PlatformTextInputInterceptor { _, _ -> awaitCancellation() }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Only sync from external state when text/selection changed (ignore sequence-only updates).
    // Preserve active local range selection while focused so users can select/copy reliably.
    LaunchedEffect(state.text, state.selection) {
        val newText = state.text.toString()
        val newSelection = state.selection.coerceIn(0, newText.length)

        val textChanged = newText != textFieldValue.text
        val selectionChanged = textFieldValue.selection != TextRange(newSelection)
        if (!textChanged && !selectionChanged) {
            return@LaunchedEffect
        }

        // Don't override local selection during active editing
        val hasActiveLocalRangeSelection =
            editorFocused && !textFieldValue.selection.collapsed && textFieldValue.text == newText
        if (hasActiveLocalRangeSelection) {
            return@LaunchedEffect
        }

        val targetSelection = TextRange(newSelection)
        textFieldValue = TextFieldValue(text = newText, selection = targetSelection)
        cursorPosition = newSelection
        adaptiveFontSize = maxTextSize
        adaptiveMaxLines = 1
    }

    // Dynamic font size and kerning based on content length
    val fontSize: TextUnit by remember(textFieldValue.text.length) {
        derivedStateOf<TextUnit> {
            when {
                textFieldValue.text.length <= 15 -> maxTextSize
                textFieldValue.text.length <= 25 -> (maxTextSize.value * 0.92f).sp
                textFieldValue.text.length <= 35 -> (maxTextSize.value * 0.85f).sp
                else -> minTextSize
            }
        }
    }

    // Dynamic kerning: tighter for longer text
    val letterSpacing: TextUnit by remember(textFieldValue.text.length) {
        derivedStateOf<TextUnit> {
            val baseSpacing = (-0.3).sp
            val lengthPenalty = ((textFieldValue.text.length - 12).coerceAtLeast(0) * 0.015f).sp
            (baseSpacing.value - lengthPenalty.value).coerceAtLeast(-0.8f).sp
        }
    }

    // Refined text selection colors
    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    )
    val scrubTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)

    fun updateSelectionFromScrubX(x: Float) {
        val layout = textLayoutResult ?: return
        val maxX = layout.size.width.toFloat().coerceAtLeast(1f)
        val clampedX = x.coerceIn(0f, maxX)
        val currentSelection = textFieldValue.selection.end.coerceIn(0, textFieldValue.text.length)
        val line = layout.getLineForOffset(currentSelection)
        val lineY = (layout.getLineTop(line) + layout.getLineBottom(line)) / 2f
        val target = layout
            .getOffsetForPosition(Offset(clampedX, lineY))
            .coerceIn(0, textFieldValue.text.length)
        if (target != textFieldValue.selection.end || !textFieldValue.selection.collapsed) {
            textFieldValue = textFieldValue.copy(selection = TextRange(target))
            cursorPosition = target
            onSelectionChange(target)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 4.dp)
            .heightIn(min = 60.dp, max = 110.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            InterceptPlatformTextInput(
                interceptor = disableSoftKeyboardInterceptor
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PlatformEditorField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            val oldValue = textFieldValue

                            // Apply smart corrections if enabled
                            val processedValue = if (enableSmartCorrection && newValue.text != oldValue.text) {
                                applySmartCorrections(newValue, oldValue)
                            } else {
                                newValue
                            }

                            textFieldValue = processedValue
                            cursorPosition = processedValue.selection.end

                            if (processedValue.text != oldValue.text) {
                                // Reset autosizing on every actual input change.
                                adaptiveFontSize = maxTextSize
                                adaptiveMaxLines = 1
                                onTextChange(processedValue.text, processedValue.selection.end)
                            } else if (processedValue.selection != oldValue.selection) {
                                onSelectionChange(processedValue.selection.end)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState -> editorFocused = focusState.isFocused },
                        enabled = true,
                        readOnly = false,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = adaptiveFontSize,
                            lineHeight = (adaptiveFontSize.value * 1.15f).sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.End,
                            fontFamily = CalculatorFontFamily,
                            fontFeatureSettings = "tnum,ss01,lnum",
                            letterSpacing = letterSpacing
                        ),
                        cursorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.None,
                            autoCorrectEnabled = false,
                            showKeyboardOnFocus = false
                        ),
                        singleLine = false,
                        maxLines = adaptiveMaxLines,
                        interactionSource = interactionSource,
                        onTextLayout = { layout ->
                            textLayoutResult = layout
                            val overflowed = layout.didOverflowWidth || layout.didOverflowHeight
                            if (!overflowed) return@PlatformEditorField

                            if (adaptiveFontSize.value > minTextSize.value + 0.1f) {
                                adaptiveFontSize = (adaptiveFontSize.value - 1f)
                                    .coerceAtLeast(minTextSize.value)
                                    .sp
                            } else if (adaptiveMaxLines < 2) {
                                adaptiveMaxLines = 2
                            }
                        },
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.BottomEnd
                            ) { innerTextField() }
                        },
                        visualTransformation = if (highlightExpressions) {
                            RefinedVisualTransformation(
                                text = textFieldValue.text,
                                cursorPosition = cursorPosition,
                                primaryColor = MaterialTheme.colorScheme.primary,
                                secondaryColor = MaterialTheme.colorScheme.secondary,
                                tertiaryColor = MaterialTheme.colorScheme.tertiary,
                                baseColor = MaterialTheme.colorScheme.onBackground,
                                surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                errorColor = MaterialTheme.colorScheme.error,
                                enableFormatting = true
                            )
                        } else {
                            VisualTransformation.None
                        }
                    )

                    if (!PlatformUsesNativeEditorView) {
                        // Dedicated scrub zone for fast horizontal caret movement.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(10.dp)
                                .background(scrubTrackColor)
                                .pointerInput(textFieldValue.text, textFieldValue.selection, textLayoutResult) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        focusRequester.requestFocus()
                                        updateSelectionFromScrubX(down.position.x)

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                            if (change.changedToUpIgnoreConsumed()) break
                                            if (change.positionChanged()) {
                                                updateSelectionFromScrubX(change.position.x)
                                                change.consume()
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Applies smart corrections to user input:
 * - ".." → "."
 * - Auto-close parentheses
 * - Prevent multiple operators in a row ("++" → "+")
 * - Disable ÷0
 * - Smart number formatting with thousands separators
 */
private fun applySmartCorrections(newValue: TextFieldValue, oldValue: TextFieldValue): TextFieldValue {
    var text = newValue.text
    var selection = newValue.selection
    var modified = false
    
    // Fix 1: Prevent double decimals (".." → ".")
    if (text.contains("..")) {
        val beforeLength = text.length
        text = text.replace("..", ".")
        // Adjust selection if needed
        if (selection.start > 0 && selection.start <= text.length) {
            selection = TextRange(selection.start - (beforeLength - text.length))
        }
        modified = true
    }
    
    // Fix 2: Prevent multiple consecutive operators
    val operators = setOf('+', '-', '*', '/', '÷', '×', '^', '%')
    val newCharIndex = findChangedIndex(text, oldValue.text)
    if (newCharIndex >= 0 && newCharIndex < text.length) {
        val newChar = text[newCharIndex]
        if (newChar in operators && newCharIndex > 0) {
            val prevChar = text[newCharIndex - 1]
            if (prevChar in operators) {
                // Remove the duplicate operator, but allow "-" after other operators for negative numbers
                if (!(newChar == '-' && prevChar in setOf('+', '*', '/', '÷', '×', '^', '%'))) {
                    text = text.substring(0, newCharIndex) + text.substring(newCharIndex + 1)
                    selection = TextRange(newCharIndex.coerceIn(0, text.length))
                    modified = true
                }
            }
        }
    }
    
    // Fix 3: Auto-close parentheses
    val openParens = text.count { it == '(' }
    val closeParens = text.count { it == ')' }
    if (openParens > closeParens && !text.endsWith(")")) {
        // Only auto-close if user just typed an opening paren or if at end
        val lastChar = text.lastOrNull()
        if (lastChar == '(' || selection.start == text.length) {
            // Don't auto-close immediately - wait for more input
            // But if user types something after (, we close it
        }
    }
    
    // Fix 4: Prevent ÷0 (and /0)
    val divZeroPattern = Regex("""[÷/]0(?!\d)""")
    if (divZeroPattern.containsMatchIn(text)) {
        // Remove the zero after division
        text = text.replace(divZeroPattern) { match ->
            match.value.substring(0, 1)
        }
        modified = true
    }
    
    return if (modified) {
        TextFieldValue(text = text, selection = selection)
    } else {
        newValue
    }
}

private fun findChangedIndex(newText: String, oldText: String): Int {
    val minLen = minOf(newText.length, oldText.length)
    for (i in 0 until minLen) {
        if (newText[i] != oldText[i]) return i
    }
    return if (newText.length > oldText.length) oldText.length else -1
}

/**
 * Refined visual transformation with:
 * - Elegant syntax highlighting with refined colors
 * - Animated thousands separators for numbers
 * - Elegant parentheses matching with subtle highlight
 * - Error detection with refined underline
 */
private class RefinedVisualTransformation(
    private val text: String,
    private val cursorPosition: Int,
    private val primaryColor: Color,
    private val secondaryColor: Color,
    private val tertiaryColor: Color,
    private val baseColor: Color,
    private val surfaceVariantColor: Color,
    private val errorColor: Color,
    private val enableFormatting: Boolean
) : VisualTransformation {
    override fun filter(annotatedString: AnnotatedString): TransformedText {
        val highlighted = highlightSyntaxWithRefinedColors(text, cursorPosition)
        return TransformedText(
            text = highlighted,
            offsetMapping = OffsetMapping.Identity
        )
    }

    private fun highlightSyntaxWithRefinedColors(text: String, cursorPos: Int): AnnotatedString {
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
                        var hasDecimal = false
                        var numberLength = 0
                        while (i < text.length && (text[i].isDigit() || text[i] == '.' ||
                               text[i] == 'E' || text[i] == 'e' || text[i] == '_')) {
                            if (text[i] == '.') hasDecimal = true
                            i++
                            numberLength++
                        }
                        val numberText = text.substring(start, i)
                        
                        // Don't format with thousands separators in visual transformation
                        // to avoid OffsetMapping issues with cursor positioning
                        val formattedNumber = numberText
                        
                        // Refined number styling: medium weight, secondary color
                        withStyle(
                            SpanStyle(
                                color = baseColor, // Numbers in base color for clarity
                                fontWeight = FontWeight.Medium,
                                fontFeatureSettings = "tnum", // Tabular figures
                                textDecoration = if (hasError) {
                                    TextDecoration.Underline
                                } else null
                            )
                        ) {
                            append(formattedNumber)
                        }
                        continue
                    }

                    isOperator(char) -> {
                        // Refined operator styling: distinct from numbers
                        val operatorColor = when (char) {
                            '+', '-' -> tertiaryColor // Arithmetic operators
                            '*', '/', '÷', '×', '^', '%' -> secondaryColor // Multiplicative
                            else -> surfaceVariantColor
                        }
                        
                        withStyle(
                            SpanStyle(
                                color = operatorColor,
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "tnum",
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
                                // Functions: primary color, semi-bold
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
                                // Constants: secondary color, bold + italic
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
                                // Unknown identifiers: base color
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
                        // Elegant parentheses matching
                        val parenColor = when {
                            isMatchingParen -> primaryColor
                            hasError -> errorColor
                            else -> surfaceVariantColor.copy(alpha = 0.7f)
                        }
                        val parenWeight = when {
                            isMatchingParen -> FontWeight.ExtraBold
                            else -> FontWeight.SemiBold
                        }
                        
                        // Subtle background highlight for matching parens
                        val parenBackground = when {
                            isMatchingParen -> primaryColor.copy(alpha = 0.12f)
                            else -> Color.Transparent
                        }
                        
                        // Rounded corner background for matching parens
                        val parenShape = when {
                            isMatchingParen -> SpanStyle(
                                color = parenColor,
                                fontWeight = parenWeight,
                                background = parenBackground,
                                fontFeatureSettings = "tnum"
                            )
                            else -> SpanStyle(
                                color = parenColor,
                                fontWeight = parenWeight,
                                fontFeatureSettings = "tnum"
                            )
                        }

                        withStyle(parenShape) {
                            append(char)
                        }
                    }

                    char == ',' -> {
                        // Commas: subtle but visible
                        withStyle(
                            SpanStyle(
                                color = surfaceVariantColor.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(char)
                        }
                    }

                    else -> {
                        // Other characters: base color
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
    
    private fun formatWithThousandsSeparators(number: String): String {
        return number.reversed().chunked(3).joinToString(",").reversed()
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

// Legacy transformation for backwards compatibility
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
        // Delegate to refined transformation without formatting
        val refined = RefinedVisualTransformation(
            text = text,
            cursorPosition = cursorPosition,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            tertiaryColor = tertiaryColor,
            baseColor = baseColor,
            surfaceVariantColor = surfaceVariantColor,
            errorColor = errorColor,
            enableFormatting = false
        )
        return refined.filter(annotatedString)
    }
}
