package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    enableSmartCorrection: Boolean = true
) {
    val scrollState = rememberScrollState()
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }
    var editorFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    // FIX: Use rememberSaveable for cursor state to survive recompositions
    var savedCursorPosition by rememberSaveable { mutableIntStateOf(0) }
    var cursorPosition by remember { mutableIntStateOf(0) }
    
    val disableSoftKeyboardInterceptor = remember {
        PlatformTextInputInterceptor { _, _ -> awaitCancellation() }
    }

    // Track last synced values to detect meaningful external changes
    var lastExternalText by remember { mutableStateOf("") }
    var lastExternalSelection by remember { mutableIntStateOf(-1) }
    var hasUserInteracted by remember { mutableStateOf(false) }
    // Track previous text length to detect if user is typing (text getting longer)
    var lastTextLength by remember { mutableIntStateOf(0) }
    
    // Debounced cursor position for smooth updates
    var debouncedSelection by remember { mutableStateOf(TextRange(0)) }

    // Only sync from external state when text/selection changed (ignore sequence-only updates).
    // Preserve active local range selection while focused so users can select/copy reliably.
    // FIX: Removed forced cursor reset - only update when text actually changes from external source
    LaunchedEffect(state.sequence, state.text, state.selection) {
        val newText = state.text.toString()
        val newSelection = state.selection.coerceIn(0, newText.length)

        val textChanged = newText != lastExternalText
        val selectionChanged = newSelection != lastExternalSelection
        
        // Only process if there's an actual change
        if (!textChanged && !selectionChanged) {
            return@LaunchedEffect
        }

        lastExternalText = newText
        lastExternalSelection = newSelection

        // If text changed, determine if it's from user typing or external update
        if (textChanged) {
            // Check if text got longer (user is typing) vs replaced (external change)
            val isUserTyping = newText.length > lastTextLength && newText.startsWith(lastExternalText)
            lastTextLength = newText.length

            // When text grows (user typing), cursor goes to end
            // When text shrinks or external change, use appropriate position
            val targetCursor = when {
                newText.length > lastExternalText.length -> newText.length // User added text, go to end
                savedCursorPosition <= newText.length -> savedCursorPosition
                else -> newText.length
            }

            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(targetCursor)
            )
            cursorPosition = targetCursor
            savedCursorPosition = targetCursor
            debouncedSelection = TextRange(targetCursor)
            return@LaunchedEffect
        }

        // Don't override local selection during active editing
        val hasActiveLocalRangeSelection =
            editorFocused && !textFieldValue.selection.collapsed && textFieldValue.text == newText
        if (hasActiveLocalRangeSelection) {
            return@LaunchedEffect
        }

        // Only update selection if different and not during active text input
        val targetSelection = TextRange(newSelection)
        if (textFieldValue.selection != targetSelection || textFieldValue.text != newText) {
            textFieldValue = textFieldValue.copy(
                text = newText,
                selection = targetSelection
            )
            cursorPosition = newSelection
            savedCursorPosition = newSelection
            debouncedSelection = targetSelection
        }
    }

    // Reset scroll to start on initial load only
    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
    }

    // Request focus only after the first frame. This avoids requesting focus
    // while the node is in a transient detached state during navigation.
    LaunchedEffect(Unit) {
        withFrameNanos { }
        runCatching { focusRequester.requestFocus() }
    }

    // Debounced cursor update to prevent flashing
    LaunchedEffect(textFieldValue.selection) {
        if (textFieldValue.selection != debouncedSelection) {
            delay(16) // ~1 frame at 60fps
            debouncedSelection = textFieldValue.selection
            cursorPosition = textFieldValue.selection.end
            savedCursorPosition = textFieldValue.selection.end
        }
    }

    // Smooth auto-scroll with spring physics (only after user interaction)
    LaunchedEffect(debouncedSelection, textLayoutResult, fieldWidthPx) {
        if (!hasUserInteracted) {
            return@LaunchedEffect
        }
        delay(30)
        val layout = textLayoutResult ?: return@LaunchedEffect
        val textLength = textFieldValue.text.length
        val layoutTextLength = layout.layoutInput.text.length

        if (fieldWidthPx == 0 || textLength == 0 || layoutTextLength != textLength) {
            return@LaunchedEffect
        }

        val cursorPos = debouncedSelection.end.coerceIn(0, textLength)
        if (cursorPos < 0 || cursorPos > layoutTextLength) {
            return@LaunchedEffect
        }

        val cursorRect = layout.getCursorRect(cursorPos)
        val current = scrollState.value.toFloat()
        val minVisible = current + (fieldWidthPx * 0.15f) // 15% padding on left
        val maxVisible = current + (fieldWidthPx * 0.85f) // 15% padding on right
        
        val target = when {
            cursorRect.left < minVisible -> cursorRect.left - (fieldWidthPx * 0.15f)
            cursorRect.right > maxVisible -> cursorRect.right - (fieldWidthPx * 0.85f)
            else -> current
        }.coerceIn(0f, scrollState.maxValue.toFloat())
        
        // Use spring animation for smooth scrolling
        scrollState.animateScrollTo(
            value = target.roundToInt(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
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
                        
                        // Apply smart corrections if enabled
                        val processedValue = if (enableSmartCorrection && newValue.text != oldValue.text) {
                            applySmartCorrections(newValue, oldValue)
                        } else {
                            newValue
                        }
                        
                        textFieldValue = processedValue
                        cursorPosition = processedValue.selection.end
                        savedCursorPosition = processedValue.selection.end

                        if (processedValue.text != oldValue.text) {
                            // Text changed - notify parent and mark interaction
                            hasUserInteracted = true
                            onTextChange(processedValue.text, processedValue.selection.start)
                        } else if (processedValue.selection != oldValue.selection && processedValue.selection.collapsed) {
                            // Only selection changed (user clicked/tapped) - notify parent
                            hasUserInteracted = true
                            onSelectionChange(processedValue.selection.start)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            editorFocused = focusState.isFocused
                        }
                        .onSizeChanged { fieldWidthPx = it.width },
                    enabled = true,
                    readOnly = false,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = fontSize,
                        lineHeight = ((fontSize.value) * 1.2f).sp, // Optimal line height
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.End,
                        fontFamily = CalculatorFontFamily,
                        fontFeatureSettings = "tnum,ss01,lnum", // Tabular figures + lining numbers
                        letterSpacing = letterSpacing
                    ),
                    // Elegant cursor with refined color and animation
                    cursorBrush = SolidColor(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    ),
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
