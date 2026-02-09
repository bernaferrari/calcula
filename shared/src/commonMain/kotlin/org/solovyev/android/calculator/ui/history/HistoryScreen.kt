package org.solovyev.android.calculator.ui.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.ui.*

import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Save
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton

@Composable
@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.c_history),
        stringResource(Res.string.cpp_history_tab_saved)
    )
    val currentList = if (selectedTab == 0) recent else saved

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.c_history)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cpp_back)
                        )
                    }
                },
                actions = {
                    if (currentList.isNotEmpty()) {
                        IconButton(onClick = { if (selectedTab == 0) onClearRecent() else onClearSaved() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.cpp_clear_history)
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Rounded.History else Icons.Rounded.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = title)
                            }
                        }
                    )
                }
            }

            if (currentList.isEmpty()) {
                EmptyHistoryState(isRecent = selectedTab == 0, onStartCalculating = onBack)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.hashCode().toLong() }) { state ->
                        HistoryCard(
                            state = state,
                            isRecent = selectedTab == 0,
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
        }
    }
}

@Composable
private fun EmptyHistoryState(isRecent: Boolean, onStartCalculating: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isRecent) Icons.Rounded.History else Icons.Rounded.Save,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (isRecent) stringResource(Res.string.cpp_history_empty) else "No saved calculations",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isRecent) {
                "Your recent calculations will appear here automatically."
            } else {
                "Save important results for quick access later."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (isRecent) {
            Spacer(modifier = Modifier.height(32.dp))
            FilledTonalButton(
                onClick = onStartCalculating,
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Rounded.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Calculating")
            }
        }
    }
}

@Composable
private fun HistoryCard(
    state: HistoryState,
    isRecent: Boolean,
    onUse: (HistoryState) -> Unit,
    onCopyExpression: (HistoryState) -> Unit,
    onCopyResult: (HistoryState) -> Unit,
    onSave: (HistoryState) -> Unit,
    onEdit: (HistoryState) -> Unit,
    onDelete: (HistoryState) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    
    val expression = state.editor.getTextString()
    val result = state.display.text
    
    // Formatting date using kotlinx-datetime
    val instant = Instant.fromEpochMilliseconds(state.time)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    // Simple format: YYYY-MM-DD HH:MM - can be improved with platform specific formatter if really needed, 
    // but consistent KMP format is often preferred.
    // Let's do a simple manual format: "Jan 01, 14:30" style or "2023-01-01 14:30"
    // Since we don't have java.text.DateFormat, we'll do a simple ISO-like or custom format.
    val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val day = localDateTime.dayOfMonth
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val timestamp = "$month $day, $hour:$minute"


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = { onUse(state) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Expression
            Text(
                text = expression,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Result
            if (result.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row with timestamp and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick action: Copy expression
                    IconButton(
                        onClick = { onCopyExpression(state) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(Res.string.c_copy_expression),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick action: Use
                    IconButton(
                        onClick = { onUse(state) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(Res.string.c_use),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // More options
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(Res.string.c_use)) },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                                onClick = {
                                    menuExpanded = false
                                    onUse(state)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(Res.string.c_copy_expression)) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                onClick = {
                                    menuExpanded = false
                                    onCopyExpression(state)
                                }
                            )
                            if (result.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(Res.string.c_copy_result)) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick = {
                                        menuExpanded = false
                                        onCopyResult(state)
                                    }
                                )
                            }
                            if (isRecent) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(Res.string.c_save)) },
                                    leadingIcon = { Icon(Icons.Default.Save, null) },
                                    onClick = {
                                        menuExpanded = false
                                        onSave(state)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(Res.string.cpp_edit)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit(state)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(Res.string.cpp_delete)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null) },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete(state)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Comment for saved items
            if (!isRecent && state.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = state.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
