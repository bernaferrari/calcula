package org.solovyev.android.calculator.ui.variables

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jscl.math.function.IConstant
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.solovyev.android.calculator.ui.*
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

@OptIn(
    KoinExperimentalAPI::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun VariablesScreen(
    onBack: () -> Unit,
    viewModel: VariablesViewModel = koinViewModel()
) {
    val refreshVersion by viewModel.refreshTick.collectAsState()
    val categories = remember { viewModel.getCategories() }
    val categoryTitles = categories.map { stringResource(it.title) }

    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var variableToEdit by remember { mutableStateOf<IConstant?>(null) }
    var variableToDelete by remember { mutableStateOf<IConstant?>(null) }

    val selectedCategory = categories.getOrElse(selectedCategoryIndex) { VariableCategory.my }
    val categoryCounts = remember(refreshVersion, categories) {
        categories.associateWith { category -> viewModel.getVariablesFor(category).size }
    }
    val variables = remember(refreshVersion, selectedCategory) {
        viewModel.getVariablesFor(selectedCategory)
    }
    val filteredVariables = remember(variables, searchQuery, refreshVersion) {
        val normalizedQuery = searchQuery.trim().lowercase()
        if (normalizedQuery.isEmpty()) {
            variables
        } else {
            variables.filter { variable ->
                val name = variable.name.lowercase()
                val value = viewModel.getValue(variable)?.lowercase().orEmpty()
                val description = viewModel.getDescription(variable)?.lowercase().orEmpty()
                name.contains(normalizedQuery) ||
                    value.contains(normalizedQuery) ||
                    description.contains(normalizedQuery)
            }
        }
    }
    val groupedItems = remember(filteredVariables) { toVariableListItems(filteredVariables) }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(Res.string.cpp_variables),
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    variableToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.c_var_create_var)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                categories.forEachIndexed { index, category ->
                    val count = categoryCounts[category] ?: 0
                    ToggleButton(
                        checked = selectedCategoryIndex == index,
                        onCheckedChange = { checked ->
                            if (checked) selectedCategoryIndex = index
                        },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            categories.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { role = Role.RadioButton }
                    ) {
                        Text(
                            text = "${categoryTitles[index]} ($count)",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(Res.string.cpp_search)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(Res.string.cpp_a11y_clear_search)
                            )
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
                    onCreate = {
                        variableToEdit = null
                        showAddDialog = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
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
                                onEdit = {
                                    variableToEdit = item.variable
                                    showAddDialog = false
                                },
                                onDelete = { variableToDelete = item.variable }
                            )
                        }
                    }
                }
            }
        }
    }

    val pendingEdit = variableToEdit
    if (showAddDialog || pendingEdit != null) {
        VariableEditDialog(
            initialName = pendingEdit?.name.orEmpty(),
            initialValue = pendingEdit?.getValue().orEmpty(),
            initialDescription = pendingEdit?.getDescription().orEmpty(),
            isEditing = pendingEdit != null,
            onDismiss = {
                showAddDialog = false
                variableToEdit = null
            },
            onSave = { name, value, desc ->
                val error = if (pendingEdit == null) {
                    viewModel.save(null, name, value, desc)
                } else {
                    viewModel.update(pendingEdit, name, value, desc)
                }
                error ?: run {
                    showAddDialog = false
                    variableToEdit = null
                    null
                }
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
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "x",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
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
        Spacer(modifier = Modifier.height(20.dp))
        FilledTonalButton(onClick = onCreate) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(Res.string.c_var_create_var))
        }
    }
}

@Composable
private fun VariableSectionHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VariableCard(
    variable: IConstant,
    position: VariableCardPosition,
    value: String?,
    description: String?,
    onUse: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isSystem = variable.isSystem()
    SegmentedListItem(
        modifier = Modifier.fillMaxWidth(),
        onClick = onUse,
        shapes = segmentedShapesFor(position),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSystem)
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = variable.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isSystem)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        },
        trailingContent = if (!isSystem) {
            {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    FilledTonalIconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(Res.string.c_var_edit_var)
                        )
                    }
                    FilledTonalIconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(Res.string.cpp_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            null
        },
        supportingContent = if (!description.isNullOrBlank()) {
            {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else null,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "= ${normalizeMathDisplay(value)}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun segmentedShapesFor(position: VariableCardPosition) = when (position) {
    VariableCardPosition.Single -> ListItemDefaults.segmentedShapes(index = 0, count = 1)
    VariableCardPosition.First -> ListItemDefaults.segmentedShapes(index = 0, count = 2)
    VariableCardPosition.Middle -> ListItemDefaults.segmentedShapes(index = 1, count = 3)
    VariableCardPosition.Last -> ListItemDefaults.segmentedShapes(index = 1, count = 2)
}

@Composable
fun VariableEditDialog(
    initialName: String = "",
    initialValue: String = "",
    initialDescription: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> String?
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val canSave = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (isEditing) Res.string.c_var_edit_var
                    else Res.string.c_var_create_var
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text(stringResource(Res.string.cpp_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        errorText = null
                    },
                    label = { Text(stringResource(Res.string.c_var_value)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        errorText = null
                    },
                    label = { Text(stringResource(Res.string.cpp_description)) },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3
                )
                if (!errorText.isNullOrBlank()) {
                    Text(
                        text = errorText.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    errorText = onSave(name.trim(), value.trim(), description.trim())
                    if (errorText == null) {
                        onDismiss()
                    }
                },
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

private fun normalizeMathDisplay(value: String): String {
    return value
        .replace('−', '-')
        .replace('×', '*')
        .replace('÷', '/')
        .replace('·', '*')
        .replace('∙', '*')
        .replace('⋅', '*')
}
