package org.solovyev.android.calculator.ui.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.tokens.CalculatorCornerRadius
import org.solovyev.android.calculator.ui.tokens.CalculatorElevation
import org.solovyev.android.calculator.ui.tokens.CalculatorPadding
import org.solovyev.android.calculator.ui.tokens.CalculatorSpacing

// =============================================================================
// REFINED HISTORY SCREEN - Beautiful list UI with delightful interactions
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(
    recent: List<HistoryState>,
    saved: List<HistoryState>,
    onUse: (HistoryState) -> Unit,
    onCopyExpression: (HistoryState) -> Unit,
    onCopyResult: (HistoryState) -> Unit,
    onSave: (HistoryState) -> Unit,
    onEdit: (HistoryState) -> Unit,
    onDelete: (HistoryState) -> Unit,
    onClearRecent: () -> Unit,
    onClearSaved: () -> Unit,
    onBack: () -> Unit
) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val selectedTab = pagerState.currentPage
    val tabs = listOf(
        stringResource(Res.string.c_history),
        stringResource(Res.string.cpp_history_tab_saved)
    )
    val currentList = if (selectedTab == 0) recent else saved

    // Track items being deleted for animation
    var deletingItems by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
    // Copy feedback state
    var copyFeedbackVisible by remember { mutableStateOf(false) }
    var copyFeedbackMessage by remember { mutableStateOf("") }
    var editingState by remember { mutableStateOf<HistoryState?>(null) }
    val expressionCopiedMessage = stringResource(Res.string.cpp_expression_copied)
    val resultCopiedMessage = stringResource(Res.string.cpp_result_copied)

    // Show copy feedback
    fun showCopyFeedback(message: String) {
        copyFeedbackMessage = message
        copyFeedbackVisible = true
        scope.launch {
            delay(if (reduceMotion) 900 else 2000)
            copyFeedbackVisible = false
        }
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(Res.string.c_history),
                onBack = onBack,
                actions = {
                    if (currentList.isNotEmpty()) {
                        IconButton(
                            onClick = { 
                                if (selectedTab == 0) onClearRecent() else onClearSaved() 
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = stringResource(Res.string.cpp_clear_history)
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Elegant segmented control
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = CalculatorElevation.Standard
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = CalculatorPadding.Standard,
                                vertical = CalculatorPadding.Medium
                            )
                    ) {
                        tabs.forEachIndexed { index, title ->
                            SegmentedButton(
                                selected = selectedTab == index,
                                onClick = { 
                                    scope.launch { 
                                        pagerState.animateScrollToPage(index) 
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index, 
                                    count = tabs.size
                                ),
                                icon = {
                                    SegmentedButtonDefaults.Icon(active = selectedTab == index) {
                                        Icon(
                                            imageVector = if (index == 0) {
                                                Icons.Filled.History
                                            } else {
                                                Icons.Filled.Star
                                            },
                                            contentDescription = null
                                        )
                                    }
                                }
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = true,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pageList = if (page == 0) recent else saved
                    val isRecent = page == 0
                    
                    if (pageList.isEmpty()) {
                        EmptyHistoryState(
                            isRecent = isRecent, 
                            reduceMotion = reduceMotion,
                            onStartCalculating = onBack
                        )
                    } else {
                        HistoryList(
                            items = pageList.filter { it.id !in deletingItems },
                            isRecent = isRecent,
                            onUse = { 
                                onUse(it)
                            },
                            onCopyExpression = { 
                                onCopyExpression(it)
                                showCopyFeedback(expressionCopiedMessage)
                            },
                            onCopyResult = { 
                                onCopyResult(it)
                                showCopyFeedback(resultCopiedMessage)
                            },
                            onSave = onSave,
                            onEdit = { editingState = it },
                            onDelete = { item ->
                                deletingItems = deletingItems + item.id
                                scope.launch {
                                    delay(300)
                                    onDelete(item)
                                    deletingItems = deletingItems - item.id
                                }
                            }
                        )
                    }
                }
            }

            // Copy feedback toast
            AnimatedVisibility(
                visible = copyFeedbackVisible,
                enter = fadeIn() + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = if (reduceMotion) fadeOut(tween(80)) else fadeOut() + scaleOut(targetScale = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(CalculatorCornerRadius.Standard),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    tonalElevation = CalculatorElevation.Elevated
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = CalculatorPadding.Standard,
                            vertical = CalculatorPadding.Medium
                        ),
                        horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface
                        )
                        Text(
                            text = copyFeedbackMessage,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    val stateToEdit = editingState
    if (stateToEdit != null) {
        HistoryEditDialog(
            initialState = stateToEdit,
            onDismiss = { editingState = null },
            onSave = { updated ->
                onEdit(updated)
                editingState = null
            }
        )
    }
}

// =============================================================================
// HISTORY LIST - Animated list with staggered entrance
// =============================================================================

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HistoryList(
    items: List<HistoryState>,
    isRecent: Boolean,
    onUse: (HistoryState) -> Unit,
    onCopyExpression: (HistoryState) -> Unit,
    onCopyResult: (HistoryState) -> Unit,
    onSave: (HistoryState) -> Unit,
    onEdit: (HistoryState) -> Unit,
    onDelete: (HistoryState) -> Unit
) {
    val itemCount = items.size
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        itemsIndexed(
            items = items,
            key = { _, state -> state.id }
        ) { index, state ->
            HistoryItemCard(
                state = state,
                isRecent = isRecent,
                index = index,
                totalCount = itemCount,
                onUse = onUse,
                onCopyExpression = onCopyExpression,
                onCopyResult = onCopyResult,
                onSave = onSave,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}

// =============================================================================
// HISTORY ITEM CARD - Beautiful card with interactions
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HistoryItemCard(
    state: HistoryState,
    isRecent: Boolean,
    index: Int,
    totalCount: Int,
    onUse: (HistoryState) -> Unit,
    onCopyExpression: (HistoryState) -> Unit,
    onCopyResult: (HistoryState) -> Unit,
    onSave: (HistoryState) -> Unit,
    onEdit: (HistoryState) -> Unit,
    onDelete: (HistoryState) -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val expression = state.editor.getTextString()
    val result = state.display.text
    val timestamp = remember(state.time) { formatHistoryTimestamp(state.time) }

    AnimatedVisibility(
        visible = !isDeleting,
        exit = if (reduceMotion) {
            fadeOut(tween(80))
        } else {
            shrinkVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        },
        modifier = modifier
    ) {
        SegmentedListItem(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { onUse(state) },
            shapes = ListItemDefaults.segmentedShapes(index = index, count = totalCount),
            colors = ListItemDefaults.segmentedColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SmallTextButton(
                        imageVector = Icons.Filled.TextFields,
                        contentDescription = stringResource(Res.string.c_copy_expression),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCopyExpression(state)
                        }
                    )

                    if (result.isNotEmpty()) {
                        SmallTextButton(
                            imageVector = Icons.Filled.Calculate,
                            contentDescription = stringResource(Res.string.c_copy_result),
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCopyResult(state)
                            }
                        )
                    }

                    Box {
                        SmallTextButton(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(Res.string.cpp_a11y_more_options),
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showMenu = true
                            }
                        )
                        val menuEntries = buildList<CalculatorMenuEntry> {
                            add(
                                CalculatorMenuEntry.Action(
                                    label = stringResource(Res.string.c_use),
                                    icon = Icons.Filled.Check,
                                    onClick = {
                                        showMenu = false
                                        onUse(state)
                                    }
                                )
                            )
                            add(
                                CalculatorMenuEntry.Action(
                                    label = stringResource(Res.string.c_copy_expression),
                                    icon = Icons.Filled.TextFields,
                                    onClick = {
                                        showMenu = false
                                        onCopyExpression(state)
                                    }
                                )
                            )
                            if (result.isNotEmpty()) {
                                add(
                                    CalculatorMenuEntry.Action(
                                        label = stringResource(Res.string.c_copy_result),
                                        icon = Icons.Filled.Calculate,
                                        onClick = {
                                            showMenu = false
                                            onCopyResult(state)
                                        }
                                    )
                                )
                            }
                            add(CalculatorMenuEntry.Divider)
                            if (isRecent) {
                                add(
                                    CalculatorMenuEntry.Action(
                                        label = stringResource(Res.string.c_save),
                                        icon = Icons.Filled.Star,
                                        onClick = {
                                            showMenu = false
                                            onSave(state)
                                        }
                                    )
                                )
                            } else {
                                add(
                                    CalculatorMenuEntry.Action(
                                        label = stringResource(Res.string.cpp_edit),
                                        icon = Icons.Filled.Edit,
                                        onClick = {
                                            showMenu = false
                                            onEdit(state)
                                        }
                                    )
                                )
                                add(
                                    CalculatorMenuEntry.Action(
                                        label = stringResource(Res.string.cpp_delete),
                                        icon = Icons.Filled.DeleteOutline,
                                        destructive = true,
                                        showTrailingArrow = false,
                                        onClick = {
                                            showMenu = false
                                            isDeleting = true
                                            scope.launch {
                                                delay(300)
                                                onDelete(state)
                                            }
                                        }
                                    )
                                )
                            }
                        }
                        CalculatorOverflowDropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            entries = menuEntries
                        )
                    }
                }
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    if (!isRecent && state.comment.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.comment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = expression,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (result.isNotEmpty()) {
                        Text(
                            text = "= $result",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        )
    }
}

// =============================================================================
// SMALL TEXT BUTTON - Compact action button using emoji/text
// =============================================================================

@Composable
private fun SmallTextButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (reduceMotion) 1f else if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable { 
                if (!reduceMotion) isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed && !reduceMotion) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun HistoryEditDialog(
    initialState: HistoryState,
    onDismiss: () -> Unit,
    onSave: (HistoryState) -> Unit
) {
    var comment by remember(initialState.id) { mutableStateOf(initialState.comment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.cpp_history_edit_saved_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = initialState.editor.getTextString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (initialState.display.text.isNotBlank()) {
                    Text(
                        text = "= ${initialState.display.text}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(stringResource(Res.string.cpp_comment)) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave@{
                    onSave(initialState.copy(comment = comment.trim()))
                }
            ) {
                Text(stringResource(Res.string.cpp_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cpp_back))
            }
        }
    )
}

// =============================================================================
// EMPTY HISTORY STATE - Beautiful illustration with CTA
// =============================================================================

@Composable
private fun EmptyHistoryState(
    isRecent: Boolean, 
    reduceMotion: Boolean,
    onStartCalculating: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon container
        val pulseScale = if (reduceMotion) {
            1f
        } else {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            ).value
        }

        Surface(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isRecent) Icons.Filled.History else Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = if (isRecent) {
                stringResource(Res.string.cpp_history_empty)
            } else {
                stringResource(Res.string.cpp_history_empty_saved)
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = if (isRecent) {
                stringResource(Res.string.cpp_history_empty_recent_subtitle)
            } else {
                stringResource(Res.string.cpp_history_empty_saved_subtitle)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (isRecent) {
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onStartCalculating,
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(Res.string.cpp_start_calculating),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// =============================================================================
// TIMESTAMP FORMATTING - Elegant relative time
// =============================================================================

private fun formatHistoryTimestamp(epochMillis: Long): String {
    return runCatching {
        val time = Instant.fromEpochMilliseconds(epochMillis)
        val localDateTime = time.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.month.name
            .take(3)
            .lowercase()
            .replaceFirstChar { it.uppercase() }
        val day = localDateTime.dayOfMonth
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        "$month $day, $hour:$minute"
    }.getOrDefault("--")
}
