package org.solovyev.android.calculator.ui.compose.history

import android.text.format.DateUtils
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.history.HistoryTextFormatter
import org.solovyev.android.calculator.history.HistoryState

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
        stringResource(R.string.c_history),
        stringResource(R.string.cpp_history_tab_saved)
    )
    val currentList = if (selectedTab == 0) recent else saved

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.c_history)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cpp_back)
                        )
                    }
                },
                actions = {
                    if (currentList.isNotEmpty()) {
                        IconButton(onClick = { if (selectedTab == 0) onClearRecent() else onClearSaved() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.cpp_clear_history)
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
                                    imageVector = if (index == 0) Icons.Default.History else Icons.Default.Save,
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
                EmptyHistoryState(isRecent = selectedTab == 0)
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
private fun EmptyHistoryState(isRecent: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(40.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isRecent) Icons.Default.History else Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.cpp_history_empty),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRecent) {
                    "Your calculations will appear here"
                } else {
                    "Save calculations for quick access"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
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
    val context = LocalContext.current
    val expression = state.editor.getTextString()
    val result = state.display.text
    val timestamp = DateUtils.formatDateTime(
        context,
        state.time,
        DateUtils.FORMAT_SHOW_TIME or
            DateUtils.FORMAT_SHOW_DATE or
            DateUtils.FORMAT_ABBREV_MONTH or
            DateUtils.FORMAT_ABBREV_TIME
    )

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
                            contentDescription = stringResource(R.string.c_copy_expression),
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
                            contentDescription = stringResource(R.string.c_use),
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
                                text = { Text(text = stringResource(id = R.string.c_use)) },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                                onClick = {
                                    menuExpanded = false
                                    onUse(state)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.c_copy_expression)) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                onClick = {
                                    menuExpanded = false
                                    onCopyExpression(state)
                                }
                            )
                            if (result.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.c_copy_result)) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick = {
                                        menuExpanded = false
                                        onCopyResult(state)
                                    }
                                )
                            }
                            if (isRecent) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.c_save)) },
                                    leadingIcon = { Icon(Icons.Default.Save, null) },
                                    onClick = {
                                        menuExpanded = false
                                        onSave(state)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.cpp_edit)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit(state)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.cpp_delete)) },
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
