package org.solovyev.android.calculator.ui.adaptive

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
// Orientation is imported from adaptive.FoldableSupport
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.TapeEntry
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.adaptive.Orientation
import org.solovyev.android.calculator.ui.history.HistoryViewModel
import org.jetbrains.compose.resources.stringResource

/**
 * Adaptive calculator layout supporting phones, foldables, tablets, and desktops.
 *
 * Layout configurations:
 * - **Compact** (Phone): Single column, bottom sheet for history
 * - **Medium** (Foldables, small tablets): Two-pane with draggable divider
 * - **Expanded** (Tablets, large screens): Three-pane with persistent side panels
 *
 * @param displayState Current calculator display state
 * @param editorState Current editor state
 * @param keyboard Keyboard composable to display
 * @param historyViewModel History data for side panels
 * @param modifier Modifier for the layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveCalculatorLayout(
    displayState: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    calculationLatencyMs: Long?,
    rpnMode: Boolean,
    rpnStack: List<String>,
    tapeMode: Boolean,
    tapeEntries: List<TapeEntry>,
    liveTapeEntry: TapeEntry?,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenVars: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenSettings: () -> Unit,
    onPrevious: () -> Unit,
    onPreviousStart: () -> Unit,
    onNext: () -> Unit,
    onNextEnd: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onEquals: () -> Unit,
    onSimplify: () -> Unit,
    onClearTape: () -> Unit,
    showBottomToolbar: Boolean,
    highlightExpressions: Boolean,
    highContrast: Boolean,
    hapticsEnabled: Boolean,
    reduceMotion: Boolean,
    extendedHaptics: Boolean,
    fontScale: Float,
    showScientificNotation: Boolean,
    onToggleScientificNotation: () -> Unit,
    keyboard: @Composable (Modifier) -> Unit,
    historyViewModel: HistoryViewModel? = null,
    onHistoryItemClick: ((HistoryState) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = calculateWindowSizeClass()
    val foldableState = rememberFoldableState()
    val orientation = windowSizeClass.orientation

    CompositionLocalProvider(
        LocalCalculatorHighContrast provides highContrast,
        LocalCalculatorHapticsEnabled provides hapticsEnabled,
        LocalCalculatorReduceMotion provides reduceMotion,
        LocalCalculatorExtendedHaptics provides extendedHaptics,
        LocalCalculatorFontScale provides fontScale,
        LocalAdaptiveLayout provides AdaptiveLayoutConfiguration(
            windowWidthClass = windowSizeClass.widthClass,
            windowHeightClass = windowSizeClass.heightClass,
            orientation = orientation,
            foldableState = foldableState
        )
    ) {
        when {
            // Foldable half-opened: adapt for dual-screen
            foldableState.isHalfOpened -> {
                FoldableHalfOpenLayout(
                    displayState = displayState,
                    editorState = editorState,
                    keyboard = keyboard,
                    foldableState = foldableState,
                    modifier = modifier
                )
            }
            // Compact (phone) portrait: single column
            windowSizeClass.widthClass == WindowWidthClass.COMPACT && orientation == Orientation.Portrait -> {
                CompactCalculatorLayout(
                    displayState = displayState,
                    editorState = editorState,
                    previewResult = previewResult,
                    unitHint = unitHint,
                    calculationLatencyMs = calculationLatencyMs,
                    rpnMode = rpnMode,
                    rpnStack = rpnStack,
                    tapeMode = tapeMode,
                    tapeEntries = tapeEntries,
                    liveTapeEntry = liveTapeEntry,
                    onEditorTextChange = onEditorTextChange,
                    onEditorSelectionChange = onEditorSelectionChange,
                    onOpenHistory = onOpenHistory,
                    onOpenConverter = onOpenConverter,
                    onOpenFunctions = onOpenFunctions,
                    onOpenVars = onOpenVars,
                    onOpenGraph = onOpenGraph,
                    onOpenSettings = onOpenSettings,
                    onPrevious = onPrevious,
                    onPreviousStart = onPreviousStart,
                    onNext = onNext,
                    onNextEnd = onNextEnd,
                    onCopy = onCopy,
                    onPaste = onPaste,
                    onEquals = onEquals,
                    onSimplify = onSimplify,
                    onClearTape = onClearTape,
                    showBottomToolbar = showBottomToolbar,
                    highlightExpressions = highlightExpressions,
                    showScientificNotation = showScientificNotation,
                    onToggleScientificNotation = onToggleScientificNotation,
                    keyboard = keyboard,
                    modifier = modifier
                )
            }
            // Medium width (foldables, small tablets)
            windowSizeClass.widthClass == WindowWidthClass.MEDIUM -> {
                TwoPaneCalculatorLayout(
                    primaryContent = {
                        CompactCalculatorLayout(
                            displayState = displayState,
                            editorState = editorState,
                            previewResult = previewResult,
                            unitHint = unitHint,
                            calculationLatencyMs = calculationLatencyMs,
                            rpnMode = rpnMode,
                            rpnStack = rpnStack,
                            tapeMode = tapeMode,
                            tapeEntries = tapeEntries,
                            liveTapeEntry = liveTapeEntry,
                            onEditorTextChange = onEditorTextChange,
                            onEditorSelectionChange = onEditorSelectionChange,
                            onOpenHistory = onOpenHistory,
                            onOpenConverter = onOpenConverter,
                            onOpenFunctions = onOpenFunctions,
                            onOpenVars = onOpenVars,
                            onOpenGraph = onOpenGraph,
                            onOpenSettings = onOpenSettings,
                            onPrevious = onPrevious,
                            onPreviousStart = onPreviousStart,
                            onNext = onNext,
                            onNextEnd = onNextEnd,
                            onCopy = onCopy,
                            onPaste = onPaste,
                            onEquals = onEquals,
                            onSimplify = onSimplify,
                            onClearTape = onClearTape,
                            showBottomToolbar = showBottomToolbar,
                            highlightExpressions = highlightExpressions,
                            showScientificNotation = showScientificNotation,
                            onToggleScientificNotation = onToggleScientificNotation,
                            keyboard = keyboard,
                            modifier = Modifier
                        )
                    },
                    secondaryContent = {
                        if (historyViewModel != null && onHistoryItemClick != null) {
                            HistorySidePanelAdaptive(
                                viewModel = historyViewModel,
                                onItemClick = onHistoryItemClick,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptySidePanel()
                        }
                    },
                    initialSplit = 0.6f,
                    modifier = modifier
                )
            }
            // Expanded width (tablets, desktops)
            windowSizeClass.widthClass == WindowWidthClass.EXPANDED -> {
                ThreePaneCalculatorLayout(
                    leftPane = {
                        if (historyViewModel != null && onHistoryItemClick != null) {
                            HistorySidePanelAdaptive(
                                viewModel = historyViewModel,
                                onItemClick = onHistoryItemClick,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptySidePanel()
                        }
                    },
                    centerPane = {
                        CompactCalculatorLayout(
                            displayState = displayState,
                            editorState = editorState,
                            previewResult = previewResult,
                            unitHint = unitHint,
                            calculationLatencyMs = calculationLatencyMs,
                            rpnMode = rpnMode,
                            rpnStack = rpnStack,
                            tapeMode = tapeMode,
                            tapeEntries = tapeEntries,
                            liveTapeEntry = liveTapeEntry,
                            onEditorTextChange = onEditorTextChange,
                            onEditorSelectionChange = onEditorSelectionChange,
                            onOpenHistory = onOpenHistory,
                            onOpenConverter = onOpenConverter,
                            onOpenFunctions = onOpenFunctions,
                            onOpenVars = onOpenVars,
                            onOpenGraph = onOpenGraph,
                            onOpenSettings = onOpenSettings,
                            onPrevious = onPrevious,
                            onPreviousStart = onPreviousStart,
                            onNext = onNext,
                            onNextEnd = onNextEnd,
                            onCopy = onCopy,
                            onPaste = onPaste,
                            onEquals = onEquals,
                            onSimplify = onSimplify,
                            onClearTape = onClearTape,
                            showBottomToolbar = showBottomToolbar,
                            highlightExpressions = highlightExpressions,
                            showScientificNotation = showScientificNotation,
                            onToggleScientificNotation = onToggleScientificNotation,
                            keyboard = keyboard,
                            modifier = Modifier
                        )
                    },
                    rightPane = {
                        ScientificFunctionsPanel(
                            onOpenFunctions = onOpenFunctions,
                            onOpenVars = onOpenVars,
                            onOpenConverter = onOpenConverter,
                            onOpenGraph = onOpenGraph,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    modifier = modifier
                )
            }
            // Fallback to compact for landscape phones
            else -> {
                CompactCalculatorLayout(
                    displayState = displayState,
                    editorState = editorState,
                    previewResult = previewResult,
                    unitHint = unitHint,
                    calculationLatencyMs = calculationLatencyMs,
                    rpnMode = rpnMode,
                    rpnStack = rpnStack,
                    tapeMode = tapeMode,
                    tapeEntries = tapeEntries,
                    liveTapeEntry = liveTapeEntry,
                    onEditorTextChange = onEditorTextChange,
                    onEditorSelectionChange = onEditorSelectionChange,
                    onOpenHistory = onOpenHistory,
                    onOpenConverter = onOpenConverter,
                    onOpenFunctions = onOpenFunctions,
                    onOpenVars = onOpenVars,
                    onOpenGraph = onOpenGraph,
                    onOpenSettings = onOpenSettings,
                    onPrevious = onPrevious,
                    onPreviousStart = onPreviousStart,
                    onNext = onNext,
                    onNextEnd = onNextEnd,
                    onCopy = onCopy,
                    onPaste = onPaste,
                    onEquals = onEquals,
                    onSimplify = onSimplify,
                    onClearTape = onClearTape,
                    showBottomToolbar = showBottomToolbar,
                    highlightExpressions = highlightExpressions,
                    showScientificNotation = showScientificNotation,
                    onToggleScientificNotation = onToggleScientificNotation,
                    keyboard = keyboard,
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * Compact layout for phones - single column with bottom sheet navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactCalculatorLayout(
    displayState: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    calculationLatencyMs: Long?,
    rpnMode: Boolean,
    rpnStack: List<String>,
    tapeMode: Boolean,
    tapeEntries: List<TapeEntry>,
    liveTapeEntry: TapeEntry?,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenVars: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenSettings: () -> Unit,
    onPrevious: () -> Unit,
    onPreviousStart: () -> Unit,
    onNext: () -> Unit,
    onNextEnd: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onEquals: () -> Unit,
    onSimplify: () -> Unit,
    onClearTape: () -> Unit,
    showBottomToolbar: Boolean,
    highlightExpressions: Boolean,
    showScientificNotation: Boolean,
    onToggleScientificNotation: () -> Unit,
    keyboard: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                }
            )
        },
        bottomBar = {
            if (showBottomToolbar) {
                Surface(
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    ModernModeBottomBar(
                        onPrevious = onPrevious,
                        onPreviousStart = onPreviousStart,
                        onNext = onNext,
                        onNextEnd = onNextEnd,
                        onCopy = onCopy,
                        onPaste = onPaste,
                        onOpenConverter = onOpenConverter,
                        onOpenGraph = onOpenGraph,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
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
            // Display card
            EnhancedDisplayCardCompact(
                state = displayState,
                editorState = editorState,
                previewResult = previewResult,
                unitHint = unitHint,
                calculationLatencyMs = calculationLatencyMs,
                rpnMode = rpnMode,
                rpnStack = rpnStack,
                tapeMode = tapeMode,
                tapeEntries = tapeEntries,
                liveTapeEntry = liveTapeEntry,
                highlightExpressions = highlightExpressions,
                showScientificNotation = showScientificNotation,
                onToggleScientificNotation = onToggleScientificNotation,
                onEquals = onEquals,
                onSimplify = onSimplify,
                onOpenGraph = onOpenGraph,
                onClearTape = onClearTape,
                onEditorTextChange = onEditorTextChange,
                onEditorSelectionChange = onEditorSelectionChange,
                onCopy = onCopy,
                modifier = Modifier.fillMaxWidth()
            )

            // Keyboard area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                keyboard(Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * Layout optimized for foldable half-opened state.
 * Puts display on top screen and keyboard on bottom screen.
 */
@Composable
private fun FoldableHalfOpenLayout(
    displayState: DisplayState,
    editorState: EditorState,
    keyboard: @Composable (Modifier) -> Unit,
    foldableState: FoldableState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top screen - Display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Simplified display for foldable top screen
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = displayState.text.takeIf { it.isNotEmpty() } ?: "0",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Hinge area indicator (visual only)
        if (foldableState.hingeBounds != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            )
        }

        // Bottom screen - Keyboard
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            keyboard(Modifier.fillMaxSize())
        }
    }
}

/**
 * Three-pane layout for large screens (tablets, desktops).
 */
@Composable
private fun ThreePaneCalculatorLayout(
    leftPane: @Composable () -> Unit,
    centerPane: @Composable () -> Unit,
    rightPane: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left pane - History
        Box(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
        ) {
            leftPane()
        }

        // Center pane - Calculator
        Box(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
        ) {
            centerPane()
        }

        // Right pane - Scientific functions
        Box(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
        ) {
            rightPane()
        }
    }
}

/**
 * Scientific functions side panel for expanded layout.
 */
@Composable
private fun ScientificFunctionsPanel(
    onOpenFunctions: () -> Unit,
    onOpenVars: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenGraph: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val tools = listOf(
                Triple("Functions", Icons.Default.Calculate, onOpenFunctions),
                Triple("Variables", Icons.Default.Folder, onOpenVars),
                Triple("Converter", Icons.Default.SwapHoriz, onOpenConverter),
                Triple("Graph", Icons.Default.ShowChart, onOpenGraph)
            )

            tools.forEach { (label, icon, onClick) ->
                ElevatedButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = label, modifier = Modifier.weight(1f))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Quick constants
            Text(
                text = "Constants",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            val constants = listOf("π" to "pi", "e" to "e", "φ" to "phi", "∞" to "infinity")
            constants.forEach { (symbol, name) ->
                OutlinedButton(
                    onClick = { /* Insert constant */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("$symbol = $name")
                }
            }
        }
    }
}

/**
 * History side panel with adaptive sizing.
 */
@Composable
private fun HistorySidePanelAdaptive(
    viewModel: HistoryViewModel,
    onItemClick: (HistoryState) -> Unit,
    modifier: Modifier = Modifier
) {
    val history by viewModel.recent.collectAsState()

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No calculations yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { entry ->
                        HistoryItemCard(
                            entry = entry,
                            onClick = { onItemClick(entry) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single history item card.
 */
@Composable
private fun HistoryItemCard(
    entry: HistoryState,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = entry.editor.getTextString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = "= ${entry.display.text}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

/**
 * Empty state for side panels.
 */
@Composable
private fun EmptySidePanel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Panel not available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Simplified display card for compact layouts.
 */
@Composable
private fun EnhancedDisplayCardCompact(
    state: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    calculationLatencyMs: Long?,
    rpnMode: Boolean,
    rpnStack: List<String>,
    tapeMode: Boolean,
    tapeEntries: List<TapeEntry>,
    liveTapeEntry: TapeEntry?,
    highlightExpressions: Boolean,
    showScientificNotation: Boolean,
    onToggleScientificNotation: () -> Unit,
    onEquals: () -> Unit,
    onSimplify: () -> Unit,
    onOpenGraph: () -> Unit,
    onClearTape: () -> Unit,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Reuse existing display card with adaptive sizing
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                // Diagnostics
                DiagnosticsRowCompact(
                    unitHint = unitHint,
                    calculationLatencyMs = calculationLatencyMs,
                    showScientificNotation = showScientificNotation,
                    onToggleScientificNotation = onToggleScientificNotation
                )

                // Editor
                CalculatorEditor(
                    state = editorState,
                    onTextChange = onEditorTextChange,
                    onSelectionChange = onEditorSelectionChange,
                    highlightExpressions = highlightExpressions,
                    minTextSize = 24.sp,
                    maxTextSize = 32.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Result
                Text(
                    text = state.text.takeIf { it.isNotEmpty() && state.valid } ?: previewResult ?: "0",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsRowCompact(
    unitHint: String?,
    calculationLatencyMs: Long?,
    showScientificNotation: Boolean,
    onToggleScientificNotation: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showScientificNotation) {
            FilledTonalIconButton(
                onClick = onToggleScientificNotation,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            calculationLatencyMs?.let {
                Text(
                    text = "${it}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            unitHint?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

// Composition locals for adaptive layout
val LocalAdaptiveLayout = staticCompositionLocalOf<AdaptiveLayoutConfiguration> {
    AdaptiveLayoutConfiguration()
}

data class AdaptiveLayoutConfiguration(
    val windowWidthClass: WindowWidthClass = WindowWidthClass.COMPACT,
    val windowHeightClass: WindowHeightClass = WindowHeightClass.COMPACT,
    val orientation: Orientation = Orientation.Portrait,
    val foldableState: FoldableState = FoldableState()
)
