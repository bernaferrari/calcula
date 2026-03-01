package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.TapeEntry
import org.solovyev.android.calculator.ui.animations.FlyingAnimationEvent
import org.solovyev.android.calculator.ui.animations.FlyingAnimationHost
import org.solovyev.android.calculator.ui.animations.LocalFlyingAnimationHost
import org.solovyev.android.calculator.ui.animations.rememberFlyingAnimationState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalculatorScreen(
    displayState: DisplayState,
    editorState: EditorState,
    previewResult: String? = null,
    unitHint: String? = null,
    rpnMode: Boolean = false,
    rpnStack: List<String> = emptyList(),
    tapeMode: Boolean = false,
    tapeEntries: List<TapeEntry> = emptyList(),
    liveTapeEntry: TapeEntry? = null,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenVariables: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenFormulas: () -> Unit,
    onOpenAbout: () -> Unit,
    onCopy: () -> Unit,
    onEquals: () -> Unit,
    onClearTape: () -> Unit = {},
    hapticsEnabled: Boolean = true,
    keyboard: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    val flyingAnimationState = rememberFlyingAnimationState()

    CompositionLocalProvider(
        LocalCalculatorHapticsEnabled provides hapticsEnabled,
        LocalFlyingAnimationHost provides { event: FlyingAnimationEvent ->
            flyingAnimationState.triggerAnimation(event)
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    StandardTopAppBar(
                        title = stringResource(Res.string.cpp_app_name),
                        actions = {
                            FilledTonalIconButton(onClick = onOpenHistory) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = stringResource(Res.string.c_history)
                                )
                            }
                            FilledTonalIconButton(onClick = onOpenFunctions) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = stringResource(Res.string.c_functions)
                                )
                            }
                            FilledTonalIconButton(onClick = onOpenSettings) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(Res.string.cpp_settings)
                                )
                            }
                            CalculatorTopOverflowMenu(
                                onOpenVariables = onOpenVariables,
                                onOpenFunctions = onOpenFunctions,
                                onOpenFormulas = onOpenFormulas,
                                onOpenGraph = onOpenGraph,
                                onOpenConverter = onOpenConverter,
                                onOpenSettings = onOpenSettings,
                                onOpenAbout = onOpenAbout
                            )
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        DisplayCard(
                            state = displayState,
                            editorState = editorState,
                            previewResult = previewResult,
                            unitHint = unitHint,
                            rpnMode = rpnMode,
                            rpnStack = rpnStack,
                            tapeMode = tapeMode,
                            tapeEntries = tapeEntries,
                            liveTapeEntry = liveTapeEntry,
                            onEquals = onEquals,
                            onClearTape = onClearTape,
                            onEditorTextChange = onEditorTextChange,
                            onEditorSelectionChange = onEditorSelectionChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        keyboard(Modifier.fillMaxSize())
                    }
                }
            }

            FlyingAnimationHost(
                state = flyingAnimationState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CalculatorTopOverflowMenu(
    onOpenVariables: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenFormulas: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.cpp_variables)) },
                onClick = {
                    expanded = false
                    onOpenVariables()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.c_functions)) },
                onClick = {
                    expanded = false
                    onOpenFunctions()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.cpp_formula_library)) },
                onClick = {
                    expanded = false
                    onOpenFormulas()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.cpp_plotter)) },
                onClick = {
                    expanded = false
                    onOpenGraph()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.c_conversion_tool)) },
                onClick = {
                    expanded = false
                    onOpenConverter()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.cpp_settings)) },
                onClick = {
                    expanded = false
                    onOpenSettings()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.cpp_about)) },
                onClick = {
                    expanded = false
                    onOpenAbout()
                }
            )
        }
    }
}

@Composable
private fun DisplayCard(
    state: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    rpnMode: Boolean,
    rpnStack: List<String>,
    tapeMode: Boolean,
    tapeEntries: List<TapeEntry>,
    liveTapeEntry: TapeEntry?,
    onEquals: () -> Unit,
    onClearTape: () -> Unit,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val inputFontSize = 30.sp
    val resultFontSize = 54.sp
    val editorText = remember(editorState.text) { editorState.text.toString() }
    var lastValidResultText by remember { mutableStateOf("") }
    LaunchedEffect(state.sequence, state.valid, state.text, editorText) {
        when {
            state.valid && state.text.isNotBlank() -> {
                lastValidResultText = state.text
            }
            state.valid && state.text.isBlank() && editorText.isBlank() -> {
                // Explicit clear/reset: discard cached result.
                lastValidResultText = ""
            }
        }
    }
    val resolvedResultText = when {
        state.valid && state.text.isNotBlank() -> state.text
        state.valid && state.text.isBlank() && editorText.isNotBlank() && lastValidResultText.isNotBlank() -> lastValidResultText
        !state.valid && lastValidResultText.isNotBlank() -> lastValidResultText
        else -> state.text
    }
    val previewText = previewResult?.takeIf { resolvedResultText.isEmpty() }
    val diagnosticsText = unitHint.orEmpty()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        if (rpnMode) {
            RpnStackPanel(
                stack = rpnStack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )
        }

        if (diagnosticsText.isNotEmpty()) {
            Text(
                text = diagnosticsText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )
        }

        if (tapeMode) {
            TapePanel(
                tapeEntries = tapeEntries,
                liveTapeEntry = liveTapeEntry,
                onClearTape = onClearTape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        CalculatorEditor(
            state = editorState,
            onTextChange = onEditorTextChange,
                onSelectionChange = onEditorSelectionChange,
            highlightExpressions = true,
            minTextSize = inputFontSize,
            maxTextSize = inputFontSize,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            val hasResult = resolvedResultText.isNotEmpty()
            val hasPreview = !previewText.isNullOrEmpty()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Surface(
                    modifier = Modifier
                        .width(74.dp)
                        .height(72.dp)
                        .combinedClickable(
                            onClick = onEquals
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        hasResult -> MaterialTheme.colorScheme.secondaryContainer
                        hasPreview -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    tonalElevation = if (hasResult) 3.dp else 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (rpnMode) "⏎" else "=",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (rpnMode) 34.sp else 42.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (hasResult) {
                    AdaptiveResultText(
                        text = resolvedResultText,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        maxFontSize = resultFontSize
                    )
                } else {
                    Text(
                        text = previewText ?: " ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (hasPreview) 0.6f else 0.25f
                            ),
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TapePanel(
    tapeEntries: List<TapeEntry>,
    liveTapeEntry: TapeEntry?,
    onClearTape: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedEntries = remember(tapeEntries, liveTapeEntry) {
        val all = if (liveTapeEntry != null) tapeEntries + liveTapeEntry else tapeEntries
        all.distinctBy { it.id }.takeLast(2)
    }
    val hasEntries = resolvedEntries.isNotEmpty()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.cpp_tape),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onClearTape,
                    enabled = hasEntries
                ) {
                    Text(stringResource(Res.string.cpp_clear_tape))
                }
            }

            if (!hasEntries) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                resolvedEntries.forEach { entry ->
                    val prefix = if (entry.committed) "" else "\u2022 "
                    Text(
                        text = prefix + entry.expression + " = " + entry.result,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (entry.committed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun RpnStackPanel(
    stack: List<String>,
    modifier: Modifier = Modifier
) {
    val x = stack.lastOrNull().orEmpty()
    val y = stack.getOrNull(stack.lastIndex - 1).orEmpty()
    val z = stack.getOrNull(stack.lastIndex - 2).orEmpty()
    val t = stack.getOrNull(stack.lastIndex - 3).orEmpty()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RpnRegisterLine(label = "T", value = t)
            RpnRegisterLine(label = "Z", value = z)
            RpnRegisterLine(label = "Y", value = y)
            RpnRegisterLine(label = "X", value = x)
        }
    }
}

@Composable
private fun RpnRegisterLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (value.isEmpty()) " " else value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AdaptiveResultText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxFontSize: androidx.compose.ui.unit.TextUnit = 54.sp,
    minFontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    step: androidx.compose.ui.unit.TextUnit = 2.sp
) {
    var renderText by remember(text) { mutableStateOf(text) }
    var fontSize by remember(text) { mutableStateOf(maxFontSize) }
    var maxLines by remember(text) { mutableStateOf(1) }
    var settled by remember(text) { mutableStateOf(false) }
    var scientificFallbackApplied by remember(text) { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val multilineThreshold = 24.sp

    LaunchedEffect(text) {
        renderText = text
        scientificFallbackApplied = false
    }

    LaunchedEffect(renderText, settled) {
        if (settled && maxLines == 1) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Text(
        text = renderText,
        style = MaterialTheme.typography.displayMedium.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.7).sp
        ),
        color = color,
        textAlign = TextAlign.End,
        maxLines = maxLines,
        softWrap = maxLines > 1,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            val overflowed = result.didOverflowWidth || result.didOverflowHeight
            if (overflowed && maxLines == 1 && fontSize > multilineThreshold) {
                fontSize = (fontSize.value - step.value).coerceAtLeast(multilineThreshold.value).sp
                settled = false
            } else if (overflowed && maxLines == 1) {
                maxLines = 2
                settled = false
            } else if (overflowed && fontSize > minFontSize) {
                fontSize = (fontSize.value - step.value).coerceAtLeast(minFontSize.value).sp
                settled = false
            } else if (overflowed && !scientificFallbackApplied) {
                val scientificFallback = toScientificFallback(text)
                if (!scientificFallback.isNullOrBlank() && scientificFallback != renderText) {
                    renderText = scientificFallback
                    scientificFallbackApplied = true
                    fontSize = maxFontSize
                    maxLines = 1
                    settled = false
                } else {
                    settled = true
                }
            } else {
                settled = true
            }
        },
        modifier = if (maxLines == 1) {
            modifier.horizontalScroll(scrollState)
        } else {
            modifier
        }
    )
}

private fun toScientificFallback(value: String, maxSignificantDigits: Int = 8): String? {
    val cleaned = value
        .trim()
        .replace(" ", "")
        .replace("\u00A0", "")
        .replace("\u2009", "")
        .replace("_", "")
        .replace(",", "")
    if (cleaned.isEmpty()) return null
    if (cleaned.contains('∞') || cleaned.equals("nan", ignoreCase = true)) return null

    val sign = when (cleaned.first()) {
        '-' -> "-"
        '+' -> ""
        else -> ""
    }
    val unsigned = if (sign.isEmpty()) cleaned else cleaned.drop(1)
    if (unsigned.isEmpty()) return null

    val expSplit = unsigned.split('e', 'E')
    if (expSplit.size !in 1..2) return null
    val significand = expSplit[0]
    val extraExponent = if (expSplit.size == 2) expSplit[1].toIntOrNull() ?: return null else 0

    val significandRegex = Regex("""(?:\d+\.?\d*|\.\d+)""")
    if (!significandRegex.matches(significand)) return null

    val parts = significand.split('.', limit = 2)
    val integerPart = parts[0]
    val fractionalPart = parts.getOrElse(1) { "" }
    val allDigits = integerPart + fractionalPart
    if (allDigits.isEmpty()) return null
    if (allDigits.all { it == '0' }) return "0"

    val firstNonZeroInAll = allDigits.indexOfFirst { it != '0' }
    if (firstNonZeroInAll < 0) return "0"
    val significantDigits = allDigits.substring(firstNonZeroInAll)

    val baseExponent = if (integerPart.any { it != '0' }) {
        val firstNonZeroInInt = integerPart.indexOfFirst { it != '0' }
        integerPart.length - firstNonZeroInInt - 1
    } else {
        val firstNonZeroInFrac = fractionalPart.indexOfFirst { it != '0' }
        -(firstNonZeroInFrac + 1)
    }

    val exponent = baseExponent + extraExponent
    val compactDigits = significantDigits.take(maxSignificantDigits.coerceAtLeast(1))
    if (compactDigits.isEmpty()) return null
    val mantissa = if (compactDigits.length == 1) {
        compactDigits
    } else {
        compactDigits.first() + "." + compactDigits.drop(1)
    }

    return "$sign$mantissa" + "e" + exponent
}
