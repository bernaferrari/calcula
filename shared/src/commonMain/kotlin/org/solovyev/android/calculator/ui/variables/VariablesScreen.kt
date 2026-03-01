package org.solovyev.android.calculator.ui.variables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jscl.math.function.IConstant
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.solovyev.android.calculator.ui.Res
import org.solovyev.android.calculator.ui.StandardTopAppBar
import org.solovyev.android.calculator.ui.c_var_create_var
import org.solovyev.android.calculator.ui.c_var_removal_confirmation_question
import org.solovyev.android.calculator.ui.c_var_value
import org.solovyev.android.calculator.ui.cpp_back
import org.solovyev.android.calculator.ui.cpp_description
import org.solovyev.android.calculator.ui.cpp_done
import org.solovyev.android.calculator.ui.cpp_entities_empty
import org.solovyev.android.calculator.ui.cpp_name
import org.solovyev.android.calculator.ui.cpp_search
import org.solovyev.android.calculator.ui.cpp_variables
import org.solovyev.android.calculator.ui.removal_confirmation
import org.solovyev.android.calculator.variables.VariableCategory

private enum class VariableCardPosition {
    Single,
    First,
    Middle,
    Last
}

private sealed interface VariableListItem {
    data class Header(val id: String, val label: String) : VariableListItem
    data class Item(val variable: IConstant, val position: VariableCardPosition) : VariableListItem
}

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun VariablesScreen(
    onBack: () -> Unit,
    viewModel: VariablesViewModel = koinViewModel()
) {
    val tick by viewModel.refreshTick.collectAsState()
    val categories = remember { viewModel.getCategories() }
    val categoryTitles = categories.map { stringResource(it.title) }

    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var variableToDelete by remember { mutableStateOf<IConstant?>(null) }

    val selectedCategory = categories.getOrElse(selectedCategoryIndex) { VariableCategory.my }
    val categoryCounts = remember(tick, categories) {
        categories.associateWith { category -> viewModel.getVariablesFor(category).size }
    }
    val variables = remember(tick, selectedCategory) {
        viewModel.getVariablesFor(selectedCategory)
    }
    val filteredVariables = remember(variables, query, tick) {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) {
            variables
        } else {
            variables.filter { variable ->
                val name = variable.name.lowercase()
                val value = viewModel.getValue(variable)?.lowercase().orEmpty()
                val description = viewModel.getDescription(variable)?.lowercase().orEmpty()
                name.contains(normalized) || value.contains(normalized) || description.contains(normalized)
            }
        }
    }
    val groupedItems = remember(filteredVariables) { toVariableListItems(filteredVariables) }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(Res.string.cpp_variables),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Text(text = "+")
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
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    item(key = category.name) {
                        val title = categoryTitles[index]
                        val count = categoryCounts[category] ?: 0
                        val isSelected = selectedCategoryIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryIndex = index },
                            label = {
                                Text(
                                    text = "$title ($count)",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = {
                    Text(text = "🔍")
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }) {
                            Text(text = "✕")
                        }
                    }
                } else {
                    null
                },
                placeholder = { Text(stringResource(Res.string.cpp_search)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            if (groupedItems.isEmpty()) {
                EmptyVariablesState(
                    onCreate = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(groupedItems, key = {
                        when (it) {
                            is VariableListItem.Header -> it.id
                            is VariableListItem.Item -> it.variable.name
                        }
                    }) { item ->
                        when (item) {
                            is VariableListItem.Header -> VariableSectionHeader(item.label)
                            is VariableListItem.Item -> VariableCard(
                                variable = item.variable,
                                position = item.position,
                                value = viewModel.getValue(item.variable),
                                description = viewModel.getDescription(item.variable),
                                onUse = {
                                    viewModel.useName(item.variable.name)
                                    onBack()
                                },
                                onDelete = { variableToDelete = item.variable }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        VariableEditDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, value, desc ->
                viewModel.add(name, value, desc)
                showAddDialog = false
            }
        )
    }

    val pendingDelete = variableToDelete
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { variableToDelete = null },
            title = { Text(stringResource(Res.string.removal_confirmation)) },
            text = {
                Text(
                    text = stringResource(
                        Res.string.c_var_removal_confirmation_question,
                        pendingDelete.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.remove(pendingDelete)
                        variableToDelete = null
                    }
                ) {
                    Text(stringResource(Res.string.cpp_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { variableToDelete = null }) {
                    Text(stringResource(Res.string.cpp_back))
                }
            }
        )
    }
}

@Composable
private fun EmptyVariablesState(
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.cpp_variables),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.cpp_entities_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        TextButton(onClick = onCreate) {
            Text(text = "+")
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(Res.string.c_var_create_var))
        }
    }
}

@Composable
private fun VariableSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp, start = 8.dp)
    )
}

@Composable
private fun VariableCard(
    variable: IConstant,
    position: VariableCardPosition,
    value: String?,
    description: String?,
    onUse: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = when (position) {
        VariableCardPosition.Single -> RoundedCornerShape(18.dp)
        VariableCardPosition.First -> RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
        VariableCardPosition.Middle -> RoundedCornerShape(6.dp)
        VariableCardPosition.Last -> RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUse),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = variable.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!value.isNullOrBlank()) {
                    Text(
                        text = "= $value",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!variable.isSystem()) {
                IconButton(onClick = onDelete) {
                    Text(text = "🗑")
                }
            }
        }
    }
}

@Composable
fun VariableEditDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val canSave = name.isNotBlank() && value.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.c_var_create_var)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.cpp_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(Res.string.c_var_value)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(Res.string.cpp_description)) },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), value.trim(), description.trim()) },
                enabled = canSave,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(Res.string.cpp_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cpp_back))
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

private fun toVariableListItems(variables: List<IConstant>): List<VariableListItem> {
    if (variables.isEmpty()) return emptyList()
    val grouped = variables.groupBy { variable ->
        val c = variable.name.firstOrNull()?.uppercaseChar()
        if (c != null && c.isLetter()) c.toString() else "#"
    }.toList().sortedBy { it.first }.toMap()

    return buildList {
        grouped.forEach { (header, group) ->
            add(VariableListItem.Header(id = "header_$header", label = header))
            group.forEachIndexed { index, variable ->
                val position = when {
                    group.size == 1 -> VariableCardPosition.Single
                    index == 0 -> VariableCardPosition.First
                    index == group.lastIndex -> VariableCardPosition.Last
                    else -> VariableCardPosition.Middle
                }
                add(VariableListItem.Item(variable = variable, position = position))
            }
        }
    }
}
