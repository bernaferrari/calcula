@file:OptIn(ExperimentalMaterial3Api::class)

package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
    persistedTabsState: String? = null,
    onPersistTabsState: (String) -> Unit = {},
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
    var hasRestoredTabs by remember { mutableStateOf(false) }
    var nextTabId by remember { mutableIntStateOf(2) }
    var activeTabId by remember { mutableStateOf("1") }
    var tabs by remember {
        mutableStateOf(
            listOf(
                CalculatorWorkspaceTab(
                    id = "1",
                    expression = editorState.text.toString(),
                    selection = editorState.selection.coerceAtLeast(0),
                    result = displayState.text.takeIf { displayState.valid } ?: ""
                )
            )
        )
    }

    LaunchedEffect(persistedTabsState) {
        if (hasRestoredTabs || persistedTabsState == null) return@LaunchedEffect

        val restored = persistedTabsState
            .takeIf { it.isNotBlank() }
            ?.let(::decodeCalculatorTabsState)
        if (restored != null) {
            val restoredTabs = restored.tabs
                .filter { it.id.isNotBlank() }
                .distinctBy { it.id }
                .take(MAX_CALCULATOR_TABS)
                .map { tab ->
                    tab.copy(selection = tab.selection.coerceIn(0, tab.expression.length))
                }
            if (restoredTabs.isNotEmpty()) {
                tabs = restoredTabs
                val restoredActiveTabId = restoredTabs
                    .firstOrNull { it.id == restored.activeTabId }
                    ?.id
                    ?: restoredTabs.first().id
                activeTabId = restoredActiveTabId
                nextTabId = restored.nextTabId.coerceAtLeast(
                    (restoredTabs.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 1) + 1
                )
                val activeTab = restoredTabs.firstOrNull { it.id == restoredActiveTabId } ?: restoredTabs.first()
                onEditorTextChange(activeTab.expression, activeTab.selection)
            }
        }

        hasRestoredTabs = true
    }

    fun snapshotFor(id: String): CalculatorWorkspaceTab {
        val expression = editorState.text.toString()
        return CalculatorWorkspaceTab(
            id = id,
            expression = expression,
            selection = editorState.selection.coerceIn(0, expression.length),
            result = displayState.text.takeIf { displayState.valid } ?: ""
        )
    }

    fun persistActiveTab(list: List<CalculatorWorkspaceTab>): List<CalculatorWorkspaceTab> {
        if (list.isEmpty()) return list
        val index = list.indexOfFirst { it.id == activeTabId }
        if (index < 0) return list
        val updated = snapshotFor(activeTabId)
        if (list[index] == updated) return list
        return list.toMutableList().also { it[index] = updated }
    }

    LaunchedEffect(
        activeTabId,
        editorState.text,
        editorState.selection,
        displayState.text,
        displayState.valid
    ) {
        tabs = persistActiveTab(tabs)
    }

    LaunchedEffect(hasRestoredTabs) {
        if (!hasRestoredTabs) return@LaunchedEffect
        snapshotFlow {
            PersistedCalculatorTabsState(
                activeTabId = activeTabId,
                nextTabId = nextTabId,
                tabs = tabs
            )
        }
            .map(::encodeCalculatorTabsState)
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .debounce(200)
            .collect(onPersistTabsState)
    }

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
                        onOpenHistory = onOpenHistory,
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
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CalculatorTabBar(
                                tabs = tabs.map { tab ->
                                    CalculatorTabData(
                                        id = tab.id,
                                        expression = tab.expression,
                                        result = tab.result
                                    )
                                },
                                activeTabId = activeTabId,
                                onTabSelect = { tabId ->
                                    val persisted = persistActiveTab(tabs)
                                    val target = persisted.firstOrNull { it.id == tabId } ?: return@CalculatorTabBar
                                    tabs = persisted
                                    activeTabId = target.id
                                    onEditorTextChange(target.expression, target.selection)
                                },
                                onTabClose = { tabId ->
                                    if (tabs.size <= 1) return@CalculatorTabBar
                                    val persisted = persistActiveTab(tabs)
                                    val closeIndex = persisted.indexOfFirst { it.id == tabId }
                                    if (closeIndex < 0) return@CalculatorTabBar

                                    val remaining = persisted.toMutableList().apply { removeAt(closeIndex) }
                                    tabs = remaining
                                    if (activeTabId == tabId) {
                                        val fallbackIndex = (closeIndex - 1).coerceAtLeast(0)
                                        val nextActive = remaining.getOrNull(fallbackIndex) ?: remaining.first()
                                        activeTabId = nextActive.id
                                        onEditorTextChange(nextActive.expression, nextActive.selection)
                                    }
                                },
                                onAddTab = {
                                    if (tabs.size >= MAX_CALCULATOR_TABS) return@CalculatorTabBar
                                    val persisted = persistActiveTab(tabs)
                                    val newId = nextTabId.toString()
                                    nextTabId += 1
                                    tabs = persisted + CalculatorWorkspaceTab(id = newId)
                                    activeTabId = newId
                                    onEditorTextChange("", 0)
                                },
                                onTabMove = { fromIndex, toIndex ->
                                    if (
                                        fromIndex !in tabs.indices ||
                                        toIndex !in tabs.indices ||
                                        fromIndex == toIndex
                                    ) {
                                        return@CalculatorTabBar
                                    }
                                    val mutableTabs = tabs.toMutableList()
                                    val moved = mutableTabs.removeAt(fromIndex)
                                    mutableTabs.add(toIndex, moved)
                                    tabs = mutableTabs
                                },
                                canAddMore = tabs.size < MAX_CALCULATOR_TABS,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 30.dp,
                                bottomEnd = 30.dp
                            ),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            tonalElevation = 1.dp,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
                            )
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (tapeMode) {
                                    TapePanel(
                                        tapeEntries = tapeEntries,
                                        liveTapeEntry = liveTapeEntry,
                                        onClearTape = onClearTape,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                DisplayCard(
                                    state = displayState,
                                    editorState = editorState,
                                    previewResult = previewResult,
                                    unitHint = unitHint,
                                    rpnMode = rpnMode,
                                rpnStack = rpnStack,
                                onEditorTextChange = onEditorTextChange,
                                onEditorSelectionChange = onEditorSelectionChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = if (tapeMode) 8.dp else 12.dp,
                                        bottom = 12.dp
                                    )
                            )
                        }
                    }
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
private fun calculatorOverflowEntries(
    onOpenHistory: () -> Unit,
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
        label = stringResource(Res.string.c_history),
        icon = Icons.Default.History,
        onClick = {
            onBeforeNavigate()
            onOpenHistory()
        }
    ),
    CalculatorMenuEntry.Divider,
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
        icon = Icons.Default.Layers,
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

@Serializable
private data class CalculatorWorkspaceTab(
    val id: String,
    val expression: String = "",
    val selection: Int = 0,
    val result: String = ""
)

@Serializable
private data class PersistedCalculatorTabsState(
    val activeTabId: String,
    val nextTabId: Int,
    val tabs: List<CalculatorWorkspaceTab>
)

private const val MAX_CALCULATOR_TABS = 24
private val DisplayCardFixedHeight = 188.dp
private val DisplayCardCompactHeight = 124.dp
private const val TapePreviewMaxChars = 5
private val calculatorTabsStateJson = Json { ignoreUnknownKeys = true }

private fun encodeCalculatorTabsState(state: PersistedCalculatorTabsState): String {
    return runCatching { calculatorTabsStateJson.encodeToString(state) }.getOrDefault("")
}

private fun decodeCalculatorTabsState(state: String): PersistedCalculatorTabsState? {
    return runCatching { calculatorTabsStateJson.decodeFromString<PersistedCalculatorTabsState>(state) }.getOrNull()
}

private fun compactTapePreviewToken(value: String): String {
    val normalized = value.trim().replace('\n', ' ')
    if (normalized.isEmpty()) return "—"
    return if (normalized.length <= TapePreviewMaxChars) {
        normalized
    } else {
        normalized.take((TapePreviewMaxChars - 1).coerceAtLeast(1)) + "…"
    }
}

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
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(Res.string.cpp_layers_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LayerToggleCard(
                        title = stringResource(Res.string.cpp_layer_up),
                        summary = stringResource(Res.string.cpp_layer_up_summary),
                        icon = Icons.Default.KeyboardArrowUp,
                        checked = layerUpEnabled,
                        onCheckedChange = onSetLayerUpEnabled
                    )
                    LayerToggleCard(
                        title = stringResource(Res.string.cpp_layer_down),
                        summary = stringResource(Res.string.cpp_layer_down_summary),
                        icon = Icons.Default.KeyboardArrowDown,
                        checked = layerDownEnabled,
                        onCheckedChange = onSetLayerDownEnabled
                    )
                    LayerToggleCard(
                        title = stringResource(Res.string.cpp_layer_engineer),
                        summary = stringResource(Res.string.cpp_layer_engineer_summary),
                        icon = Icons.Default.Code,
                        checked = layerEngineerEnabled,
                        onCheckedChange = onSetLayerEngineerEnabled
                    )
                }

                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.cpp_done))
                }
            }
        }
    }
}

@Composable
private fun LayerToggleCard(
    title: String,
    summary: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val cardColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(durationMillis = 280),
        label = "cardColor"
    )
    val titleColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 280),
        label = "titleColor"
    )
    val summaryColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 280),
        label = "summaryColor"
    )
    val iconContainerColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(durationMillis = 280),
        label = "iconContainerColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 280),
        label = "iconTint"
    )
    val indicatorBg by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 280),
        label = "indicatorBg"
    )
    val indicatorBorder by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 280),
        label = "indicatorBorder"
    )

    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconContainerColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = summaryColor,
                    maxLines = 2
                )
            }

            Surface(
                shape = CircleShape,
                color = indicatorBg,
                border = BorderStroke(width = 2.dp, color = indicatorBorder),
                modifier = Modifier.size(26.dp)
            ) {
                AnimatedVisibility(
                    visible = checked,
                    enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
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
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val inputFontSize = 32.sp
    val resultFontSize = 56.sp
    val editorText = remember(editorState.text) { editorState.text }
    val resultResolution = rememberResolvedDisplayResult(
        state = state,
        editorText = editorText
    )
    val resolvedResultText = resultResolution.text
    val previewText = previewResult?.takeIf { resolvedResultText.isEmpty() }
    val hasResult = resolvedResultText.isNotEmpty()
    val hasPreview = !previewText.isNullOrEmpty()
    val hasInvalidState = !state.valid
    val isCachedResult = resultResolution.isCachedValue

    val resultColor = when {
        hasInvalidState && !isCachedResult -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    val cardHeight = if (rpnMode) DisplayCardFixedHeight else DisplayCardCompactHeight

    Column(
        modifier = modifier.height(cardHeight),
        horizontalAlignment = Alignment.End
    ) {
        if (rpnMode) {
            RpnStackPanel(
                stack = rpnStack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            CalculatorEditor(
                state = editorState,
                onTextChange = onEditorTextChange,
                onSelectionChange = onEditorSelectionChange,
                highlightExpressions = true,
                minTextSize = 22.sp,
                maxTextSize = inputFontSize,
                containerMinHeight = 40.dp,
                containerMaxHeight = 64.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            when {
                hasResult -> {
                    AdaptiveResultText(
                        text = resolvedResultText,
                        color = resultColor,
                        modifier = Modifier.fillMaxWidth(),
                        maxFontSize = resultFontSize
                    )
                }

                hasPreview -> {
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
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
    val latestEntry = resolvedEntries.lastOrNull()
    val hasEntries = latestEntry != null
    val latestText = latestEntry?.let {
        "${compactTapePreviewToken(it.expression)} = ${compactTapePreviewToken(it.result)}"
    } ?: "—"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.cpp_tape),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
            )
            Text(
                text = latestText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (latestEntry?.committed == false) FontWeight.Medium else FontWeight.Normal
                ),
                color = if (latestEntry?.committed == false) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (hasEntries) {
                TextButton(
                    onClick = onClearTape,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        stringResource(Res.string.cpp_clear_tape),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            thickness = 1.dp
        )
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

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        RpnRegisterLine(label = "T", value = t, isActive = false, modifier = Modifier.weight(1f))
        RpnRegisterLine(label = "Z", value = z, isActive = false, modifier = Modifier.weight(1f))
        RpnRegisterLine(label = "Y", value = y, isActive = false, modifier = Modifier.weight(1f))
        RpnRegisterLine(label = "X", value = x, isActive = true, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RpnRegisterLine(
    label: String,
    value: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.64f)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = if (value.isEmpty()) "—" else value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    fontFeatureSettings = "tnum"
                ),
                color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
                },
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
    val textMeasurer = rememberTextMeasurer()
    val scrollState = rememberScrollState()

    LaunchedEffect(text) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx().toInt() }.coerceAtLeast(1)
        val constraints = Constraints(maxWidth = maxWidthPx)
        val baseStyle = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.7).sp
        )

        fun fits(fontSizeSp: Float): Boolean {
            val measureResult = textMeasurer.measure(
                text = AnnotatedString(text),
                style = baseStyle.copy(fontSize = fontSizeSp.sp),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                constraints = constraints
            )
            return !measureResult.didOverflowWidth
        }

        val resolvedFontSize = remember(
            text,
            maxWidthPx,
            maxFontSize,
            minFontSize
        ) {
            var candidateSp = maxFontSize.value
            while (candidateSp >= minFontSize.value) {
                if (fits(candidateSp)) {
                    return@remember candidateSp.sp
                }
                candidateSp -= 1f
            }

            minFontSize
        }

        Text(
            text = text,
            style = baseStyle.copy(fontSize = resolvedFontSize),
            color = color,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        )
    }
}
