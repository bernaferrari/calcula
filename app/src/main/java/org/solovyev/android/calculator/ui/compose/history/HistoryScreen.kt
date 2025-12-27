package org.solovyev.android.calculator.ui.compose.history

import android.text.format.DateUtils
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    var selectedTab by remember { mutableStateOf(1) }
    val tabs = listOf(
        stringResource(R.string.cpp_history_tab_saved) + " " + stringResource(R.string.c_history),
        stringResource(R.string.c_history)
    )
    val currentList = if (selectedTab == 1) recent else saved

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
                    IconButton(onClick = { if (selectedTab == 1) onClearRecent() else onClearSaved() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.cpp_clear_history)
                        )
                    }
                }
            )
        }
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
                        text = { Text(text = title) }
                    )
                }
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.cpp_history_empty),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentList, key = { it.hashCode().toLong() }) { state ->
                        HistoryRow(
                            state = state,
                            isRecent = selectedTab == 1,
                            onUse = onUse,
                            onCopyExpression = onCopyExpression,
                            onCopyResult = onCopyResult,
                            onSave = onSave,
                            onEdit = onEdit,
                            onDelete = onDelete
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
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
    val expression = HistoryTextFormatter.format(state)
    val timestamp = DateUtils.formatDateTime(
        context,
        state.time,
        DateUtils.FORMAT_SHOW_TIME or
            DateUtils.FORMAT_SHOW_DATE or
            DateUtils.FORMAT_ABBREV_MONTH or
            DateUtils.FORMAT_ABBREV_TIME
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onUse(state) },
                onLongClick = { menuExpanded = true }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = timestamp,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                            imageVector = Icons.Default.MoreVert,
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.c_use)) },
                        onClick = {
                            menuExpanded = false
                            onUse(state)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.c_copy_expression)) },
                        onClick = {
                            menuExpanded = false
                            onCopyExpression(state)
                        }
                    )
                    if (!state.display.valid || state.display.text.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.c_copy_result)) },
                            onClick = {
                                menuExpanded = false
                                onCopyResult(state)
                            }
                        )
                    }
                    if (isRecent) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.c_save)) },
                            onClick = {
                                menuExpanded = false
                                onSave(state)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.cpp_edit)) },
                            onClick = {
                                menuExpanded = false
                                onEdit(state)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.cpp_delete)) },
                            onClick = {
                                menuExpanded = false
                                onDelete(state)
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = expression,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isRecent) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.cpp_status))
                    }
                    append(": ")
                    append(stringResource(R.string.cpp_not_saved))
                },
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (state.comment.isNotEmpty()) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.c_history_comment))
                    }
                    append(": ")
                    append(state.comment)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
