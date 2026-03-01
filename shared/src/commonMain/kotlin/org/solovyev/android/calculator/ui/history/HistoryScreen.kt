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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.ui.*

// =============================================================================
// REFINED HISTORY SCREEN - Beautiful list UI with delightful interactions
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
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

    // Show copy feedback
    fun showCopyFeedback(message: String) {
        copyFeedbackMessage = message
        copyFeedbackVisible = true
        scope.launch {
            delay(2000)
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
                            Text(
                                "🗑",
                                style = MaterialTheme.typography.titleMedium
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
                    shadowElevation = 2.dp
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                        Text(
                                            if (index == 0) "⏱" else "🔖",
                                            style = MaterialTheme.typography.bodySmall
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
                                showCopyFeedback("Expression copied")
                            },
                            onCopyResult = { 
                                onCopyResult(it)
                                showCopyFeedback("Result copied")
                            },
                            onSave = onSave,
                            onEdit = onEdit,
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
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.bodyMedium
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
}

// =============================================================================
// HISTORY LIST - Animated list with staggered entrance
// =============================================================================

@OptIn(ExperimentalFoundationApi::class)
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = items,
            key = { _, state -> state.id }
        ) { index, state ->
            HistoryItemCard(
                state = state,
                isRecent = isRecent,
                index = index,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryItemCard(
    state: HistoryState,
    isRecent: Boolean,
    index: Int,
    onUse: (HistoryState) -> Unit,
    onCopyExpression: (HistoryState) -> Unit,
    onCopyResult: (HistoryState) -> Unit,
    onSave: (HistoryState) -> Unit,
    onEdit: (HistoryState) -> Unit,
    onDelete: (HistoryState) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    var showMenu by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    
    val expression = state.editor.getTextString()
    val result = state.display.text
    val timestamp = remember(state.time) { formatHistoryTimestamp(state.time) }

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 50L)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible && !isDeleting) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible && !isDeleting) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // Press animation
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    AnimatedVisibility(
        visible = !isDeleting,
        exit = shrinkVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    this.scaleX = scale * pressScale
                    this.scaleY = scale * pressScale
                    this.alpha = alpha
                }
                .shadow(
                    elevation = if (isPressed) 2.dp else 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { onUse(state) },
                        onClickLabel = stringResource(Res.string.c_use)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Expression with monospace font
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

                    // Result with emphasis
                    if (result.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "=",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Light
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = result,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom row: timestamp + actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timestamp chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⏱",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Quick action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Copy expression
                            SmallTextButton(
                                text = "📋",
                                contentDescription = stringResource(Res.string.c_copy_expression),
                                onClick = { 
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onCopyExpression(state)
                                }
                            )

                            // Copy result (if available)
                            if (result.isNotEmpty()) {
                                SmallTextButton(
                                    text = "🔢",
                                    contentDescription = stringResource(Res.string.c_copy_result),
                                    onClick = { 
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onCopyResult(state)
                                    }
                                )
                            }

                            // More options
                            Box {
                                SmallTextButton(
                                    text = "⋮",
                                    contentDescription = "More options",
                                    onClick = { 
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showMenu = true 
                                    }
                                )

                                // Dropdown menu
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    shape = RoundedCornerShape(16.dp),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("▶ Use in calculator") },
                                        onClick = {
                                            showMenu = false
                                            onUse(state)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("📋 Copy expression") },
                                        onClick = {
                                            showMenu = false
                                            onCopyExpression(state)
                                        }
                                    )
                                    if (result.isNotEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("🔢 Copy result") },
                                            onClick = {
                                                showMenu = false
                                                onCopyResult(state)
                                            }
                                        )
                                    }
                                    Divider()
                                    if (isRecent) {
                                        DropdownMenuItem(
                                            text = { Text("🔖 Save calculation") },
                                            onClick = {
                                                showMenu = false
                                                onSave(state)
                                            }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("✏️ Edit") },
                                            onClick = {
                                                showMenu = false
                                                onEdit(state)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    "🗑 Delete",
                                                    color = MaterialTheme.colorScheme.error
                                                ) 
                                            },
                                            onClick = {
                                                showMenu = false
                                                isDeleting = true
                                                scope.launch {
                                                    delay(300)
                                                    onDelete(state)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Comment for saved items
                    if (!isRecent && state.comment.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📝",
                                    style = MaterialTheme.typography.bodyMedium
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
            }
        }
    }
}

// =============================================================================
// SMALL TEXT BUTTON - Compact action button using emoji/text
// =============================================================================

@Composable
private fun SmallTextButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable { 
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

// =============================================================================
// EMPTY HISTORY STATE - Beautiful illustration with CTA
// =============================================================================

@Composable
private fun EmptyHistoryState(
    isRecent: Boolean, 
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
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Surface(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isRecent) "⏱" else "🔖",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
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
                Text("🔢")
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
