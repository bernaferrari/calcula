package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
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
    layerUpEnabled: Boolean,
    layerDownEnabled: Boolean,
    layerEngineerEnabled: Boolean,
    onSetLayerUpEnabled: (Boolean) -> Unit,
    onSetLayerDownEnabled: (Boolean) -> Unit,
    onSetLayerEngineerEnabled: (Boolean) -> Unit,
    onCursorLeft: () -> Unit,
    onCursorRight: () -> Unit,
    onCursorToStart: () -> Unit,
    onCursorToEnd: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onEquals: () -> Unit,
    onClearTape: () -> Unit = {},
    hapticsEnabled: Boolean = true,
    keyboard: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    val flyingAnimationState = rememberFlyingAnimationState()
    var showLayersDialog by remember { mutableStateOf(false) }

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
                                onOpenAbout = onOpenAbout,
                                onOpenLayers = { showLayersDialog = true }
                            )
                        }
                    )
                },
                bottomBar = {
                    val keyboardIcons = LocalKeyboardIcons.current
                    var bottomOverflowExpanded by remember { mutableStateOf(false) }
                    val cursorToolbarItems = listOf(
                        FloatingToolbarItem(
                            label = stringResource(Res.string.cpp_cursor_previous),
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            },
                            onClick = onCursorLeft,
                            onLongClick = onCursorToStart
                        ),
                        FloatingToolbarItem(
                            label = stringResource(Res.string.cpp_cursor_next),
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null
                                )
                            },
                            onClick = onCursorRight,
                            onLongClick = onCursorToEnd
                        )
                    )
                    val bottomOverflowEntries = calculatorOverflowEntries(
                        onOpenVariables = onOpenVariables,
                        onOpenFunctions = onOpenFunctions,
                        onOpenFormulas = onOpenFormulas,
                        onOpenGraph = onOpenGraph,
                        onOpenConverter = onOpenConverter,
                        onOpenSettings = onOpenSettings,
                        onOpenAbout = onOpenAbout,
                        onOpenLayers = { showLayersDialog = true },
                        onBeforeNavigate = { bottomOverflowExpanded = false }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingToolbar(
                            items = cursorToolbarItems,
                            layout = FloatingToolbarLayout.HORIZONTAL,
                            colorScheme = FloatingToolbarColor.STANDARD,
                            expanded = true,
                            leadingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box {
                                        IconButton(onClick = { bottomOverflowExpanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = stringResource(Res.string.cpp_a11y_more_options)
                                            )
                                        }
                                        CalculatorOverflowDropdownMenu(
                                            expanded = bottomOverflowExpanded,
                                            onDismissRequest = { bottomOverflowExpanded = false },
                                            entries = bottomOverflowEntries
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    ToolbarSectionDivider()
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    ToolbarSectionDivider()
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = onDelete,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            painter = keyboardIcons.backspace,
                                            contentDescription = stringResource(Res.string.cpp_delete),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
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
                                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
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

            if (showLayersDialog) {
                LayersDialog(
                    layerUpEnabled = layerUpEnabled,
                    layerDownEnabled = layerDownEnabled,
                    layerEngineerEnabled = layerEngineerEnabled,
                    onSetLayerUpEnabled = onSetLayerUpEnabled,
                    onSetLayerDownEnabled = onSetLayerDownEnabled,
                    onSetLayerEngineerEnabled = onSetLayerEngineerEnabled,
                    onDismiss = { showLayersDialog = false }
                )
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
    onOpenAbout: () -> Unit,
    onOpenLayers: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val entries = calculatorOverflowEntries(
        onOpenVariables = onOpenVariables,
        onOpenFunctions = onOpenFunctions,
        onOpenFormulas = onOpenFormulas,
        onOpenGraph = onOpenGraph,
        onOpenConverter = onOpenConverter,
        onOpenSettings = onOpenSettings,
        onOpenAbout = onOpenAbout,
        onOpenLayers = onOpenLayers,
        onBeforeNavigate = { expanded = false }
    )

    Box {
        CalculatorOverflowIconButton(onClick = { expanded = true })
        CalculatorOverflowDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            entries = entries
        )
    }
}

@Composable
private fun calculatorOverflowEntries(
    onOpenVariables: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenFormulas: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLayers: () -> Unit,
    onBeforeNavigate: () -> Unit
): List<CalculatorMenuEntry> = listOf(
    CalculatorMenuEntry.Action(
        label = stringResource(Res.string.cpp_variables),
        icon = Icons.Default.TextFields,
        onClick = {
            onBeforeNavigate()
            onOpenVariables()
        }
    ),
    CalculatorMenuEntry.Action(
        label = stringResource(Res.string.c_functions),
        icon = Icons.Default.Code,
        onClick = {
            onBeforeNavigate()
            onOpenFunctions()
        }
    ),
    CalculatorMenuEntry.Action(
        label = stringResource(Res.string.cpp_formula_library),
        icon = Icons.Default.Calculate,
        onClick = {
            onBeforeNavigate()
            onOpenFormulas()
        }
    ),
    CalculatorMenuEntry.Divider,
    CalculatorMenuEntry.Action(
        label = stringResource(Res.string.cpp_plotter),
        icon = Icons.Default.Speed,
        onClick = {
            onBeforeNavigate()
            onOpenGraph()
        }
    ),
    CalculatorMenuEntry.Action(
        label = stringResource(Res.string.c_conversion_tool),
        icon = Icons.Default.Tune,
        onClick = {
            onBeforeNavigate()
            onOpenConverter()
        }
    ),
    CalculatorMenuEntry.Divider,
    CalculatorMenuEntry.Action(
        label = stringResource(Res.string.cpp_layers),
        onClick = {
            onBeforeNavigate()
            onOpenLayers()
        },
        showTrailingArrow = false
    ),
    CalculatorMenuEntry.Divider,
    CalculatorMenuEntry.Action(
        label = stringResource(Res.string.cpp_settings),
        icon = Icons.Default.Settings,
        onClick = {
            onBeforeNavigate()
            onOpenSettings()
        }
    ),
    CalculatorMenuEntry.Action(
        label = stringResource(Res.string.cpp_about),
        icon = Icons.Default.Info,
        onClick = {
            onBeforeNavigate()
            onOpenAbout()
        }
    )
)

@Composable
private fun LayersDialog(
    layerUpEnabled: Boolean,
    layerDownEnabled: Boolean,
    layerEngineerEnabled: Boolean,
    onSetLayerUpEnabled: (Boolean) -> Unit,
    onSetLayerDownEnabled: (Boolean) -> Unit,
    onSetLayerEngineerEnabled: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(Res.string.cpp_layers_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LayerToggleRow(
                    title = stringResource(Res.string.cpp_layer_up),
                    summary = stringResource(Res.string.cpp_layer_up_summary),
                    checked = layerUpEnabled,
                    onCheckedChange = onSetLayerUpEnabled
                )
                LayerToggleRow(
                    title = stringResource(Res.string.cpp_layer_down),
                    summary = stringResource(Res.string.cpp_layer_down_summary),
                    checked = layerDownEnabled,
                    onCheckedChange = onSetLayerDownEnabled
                )
                LayerToggleRow(
                    title = stringResource(Res.string.cpp_layer_engineer),
                    summary = stringResource(Res.string.cpp_layer_engineer_summary),
                    checked = layerEngineerEnabled,
                    onCheckedChange = onSetLayerEngineerEnabled
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cpp_done))
            }
        }
    )
}

@Composable
private fun LayerToggleRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun ToolbarSectionDivider() {
    VerticalDivider(
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 2.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    )
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
    val resultResolution = rememberResolvedDisplayResult(
        state = state,
        editorText = editorText
    )
    val resolvedResultText = resultResolution.text
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
            minTextSize = 18.sp,
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
            val hasInvalidState = !state.valid
            val isCachedResult = resultResolution.isCachedValue
            val resultTextColor = when {
                hasInvalidState && !isCachedResult -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }.copy(
                alpha = when {
                    isCachedResult -> 0.7f
                    hasInvalidState -> 0.78f
                    else -> 1f
                }
            )

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
                        hasResult && !hasInvalidState && !isCachedResult -> MaterialTheme.colorScheme.secondaryContainer
                        hasResult -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
                        hasPreview -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    tonalElevation = if (hasResult) 3.dp else 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (rpnMode) "ENT" else "=",
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
                        color = resultTextColor,
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
                    text = "-",
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
    minFontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    val multilineThreshold = 24.sp
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx().toInt() }.coerceAtLeast(1)
        val constraints = Constraints(maxWidth = maxWidthPx)
        val baseStyle = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.7).sp
        )

        fun fits(fontSizeSp: Float, lines: Int): Boolean {
            val measureResult = textMeasurer.measure(
                text = AnnotatedString(text),
                style = baseStyle.copy(fontSize = fontSizeSp.sp),
                maxLines = lines,
                softWrap = lines > 1,
                overflow = TextOverflow.Clip,
                constraints = constraints
            )
            return !(measureResult.didOverflowWidth || measureResult.didOverflowHeight)
        }

        val (resolvedFontSize, resolvedLines) = remember(
            text,
            maxWidthPx,
            maxFontSize,
            minFontSize
        ) {
            var candidateSp = maxFontSize.value
            while (candidateSp >= multilineThreshold.value) {
                if (fits(candidateSp, lines = 1)) {
                    return@remember candidateSp.sp to 1
                }
                candidateSp -= 1f
            }

            candidateSp = multilineThreshold.value.coerceAtMost(maxFontSize.value)
            while (candidateSp >= minFontSize.value) {
                if (fits(candidateSp, lines = 2)) {
                    return@remember candidateSp.sp to 2
                }
                candidateSp -= 1f
            }

            minFontSize to 2
        }

        Text(
            text = text,
            style = baseStyle.copy(fontSize = resolvedFontSize),
            color = color,
            textAlign = TextAlign.End,
            maxLines = resolvedLines,
            softWrap = resolvedLines > 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
