package org.solovyev.android.calculator.ui.functions

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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
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
import jscl.math.function.Function
import jscl.math.function.IFunction
import jscl.math.operator.Operator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.solovyev.android.calculator.functions.FunctionCategory
import org.solovyev.android.calculator.operators.OperatorCategory
import org.solovyev.android.calculator.ui.*

private enum class FunctionEntityType {
    Functions,
    Operators
}

private enum class FunctionCardPosition {
    Single,
    First,
    Middle,
    Last
}

private sealed interface FunctionListItem {
    data class Header(val id: String, val label: String) : FunctionListItem
    data class FunctionEntry(val model: FunctionUiModel, val position: FunctionCardPosition) : FunctionListItem
    data class OperatorEntry(val model: OperatorUiModel, val position: FunctionCardPosition) : FunctionListItem
}

private data class FunctionUiModel(
    val function: Function,
    val title: String,
    val signature: String,
    val parameters: List<String>,
    val body: String?,
    val description: String?,
    val canEdit: Boolean,
    val canDelete: Boolean
)

private data class OperatorUiModel(
    val operator: Operator,
    val title: String,
    val signature: String?,
    val description: String?
)

@OptIn(
    KoinExperimentalAPI::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun FunctionsScreen(
    onBack: () -> Unit,
    viewModel: FunctionsViewModel = koinViewModel()
) {
    val refreshVersion by viewModel.refreshTick.collectAsState()
    val functionCategories = remember { viewModel.getFunctionCategories() }
    val operatorCategories = remember { viewModel.getOperatorCategories() }
    val functionCategoryTitles = functionCategories.map { stringResource(it.title) }
    val operatorCategoryTitles = operatorCategories.map { stringResource(it.title) }

    var selectedEntityTypeIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedFunctionCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedOperatorCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var functionToEdit by remember { mutableStateOf<FunctionUiModel?>(null) }
    var functionToDelete by remember { mutableStateOf<Function?>(null) }

    val selectedEntityType = if (selectedEntityTypeIndex == 0) FunctionEntityType.Functions else FunctionEntityType.Operators
    val selectedFunctionCategory = functionCategories.getOrElse(selectedFunctionCategoryIndex) { FunctionCategory.common }
    val selectedOperatorCategory = operatorCategories.getOrElse(selectedOperatorCategoryIndex) { OperatorCategory.Common }

    val functionCategoryCounts = remember(refreshVersion, functionCategories) {
        functionCategories.associateWith { category -> viewModel.getFunctionsFor(category).size }
    }
    val operatorCategoryCounts = remember(refreshVersion, operatorCategories) {
        operatorCategories.associateWith { category -> viewModel.getOperatorsFor(category).size }
    }

    val functionModels = remember(refreshVersion, selectedFunctionCategory, searchQuery) {
        val normalizedQuery = searchQuery.trim().lowercase()
        viewModel.getFunctionsFor(selectedFunctionCategory)
            .map { function ->
                val signature = buildFunctionSignature(function)
                val parameters = (function as? IFunction)?.getParameterNames().orEmpty()
                val body = (function as? IFunction)?.getContent()
                val description = viewModel.getFunctionDescription(function)
                    ?: (function as? IFunction)?.getDescription()
                FunctionUiModel(
                    function = function,
                    title = function.name,
                    signature = signature,
                    parameters = parameters,
                    body = body,
                    description = description,
                    canEdit = !function.isSystem(),
                    canDelete = !function.isSystem()
                )
            }
            .filter { model ->
                if (normalizedQuery.isEmpty()) {
                    true
                } else {
                    model.title.lowercase().contains(normalizedQuery) ||
                        model.signature.lowercase().contains(normalizedQuery) ||
                        model.body.orEmpty().lowercase().contains(normalizedQuery) ||
                        model.description.orEmpty().lowercase().contains(normalizedQuery)
                }
            }
    }

    val operatorModels = remember(refreshVersion, selectedOperatorCategory, searchQuery) {
        val normalizedQuery = searchQuery.trim().lowercase()
        viewModel.getOperatorsFor(selectedOperatorCategory)
            .map { operator ->
                val signature = runCatching { operator.toString() }.getOrNull()
                OperatorUiModel(
                    operator = operator,
                    title = operator.name,
                    signature = signature,
                    description = viewModel.getOperatorDescription(operator)
                )
            }
            .filter { model ->
                if (normalizedQuery.isEmpty()) {
                    true
                } else {
                    model.title.lowercase().contains(normalizedQuery) ||
                        model.signature.orEmpty().lowercase().contains(normalizedQuery) ||
                        model.description.orEmpty().lowercase().contains(normalizedQuery)
                }
            }
    }

    val visibleItems = remember(functionModels, operatorModels, selectedEntityType) {
        when (selectedEntityType) {
            FunctionEntityType.Functions -> toFunctionListItems(functionModels)
            FunctionEntityType.Operators -> toOperatorListItems(operatorModels)
        }
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(Res.string.c_functions),
                onBack = onBack,
                actions = {
                    if (selectedEntityType == FunctionEntityType.Functions) {
                        FilledTonalIconButton(onClick = {
                            functionToEdit = null
                            showAddDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(Res.string.function_create_function)
                            )
                        }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                val labels = listOf(
                    stringResource(Res.string.cpp_functions),
                    stringResource(Res.string.cpp_operators)
                )
                labels.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = selectedEntityTypeIndex == index,
                        onCheckedChange = { checked ->
                            if (checked) selectedEntityTypeIndex = index
                        },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            labels.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
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
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val activeCategoryTitles = if (selectedEntityType == FunctionEntityType.Functions) {
                functionCategoryTitles
            } else {
                operatorCategoryTitles
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(activeCategoryTitles.size, key = { it }) { index ->
                    val isSelected = if (selectedEntityType == FunctionEntityType.Functions) {
                        selectedFunctionCategoryIndex == index
                    } else {
                        selectedOperatorCategoryIndex == index
                    }
                    val count = if (selectedEntityType == FunctionEntityType.Functions) {
                        functionCategoryCounts[functionCategories[index]] ?: 0
                    } else {
                        operatorCategoryCounts[operatorCategories[index]] ?: 0
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (selectedEntityType == FunctionEntityType.Functions) {
                                selectedFunctionCategoryIndex = index
                            } else {
                                selectedOperatorCategoryIndex = index
                            }
                        },
                        label = {
                            Text(
                                text = "${activeCategoryTitles[index]} ($count)",
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

            if (visibleItems.isEmpty()) {
                EmptyFunctionsState(
                    selectedType = selectedEntityType,
                    onCreate = {
                        functionToEdit = null
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
                    items(visibleItems, key = { item ->
                        when (item) {
                            is FunctionListItem.Header -> item.id
                            is FunctionListItem.FunctionEntry -> "f_${item.model.function.name}"
                            is FunctionListItem.OperatorEntry -> "o_${item.model.operator.name}"
                        }
                    }) { item ->
                        when (item) {
                            is FunctionListItem.Header -> FunctionSectionHeader(item.label)
                            is FunctionListItem.FunctionEntry -> FunctionCard(
                                model = item.model,
                                position = item.position,
                                onUse = {
                                    viewModel.useName(item.model.function.name)
                                    onBack()
                                },
                                onEdit = {
                                    functionToEdit = item.model
                                    showAddDialog = false
                                },
                                onDelete = {
                                    functionToDelete = item.model.function
                                }
                            )
                            is FunctionListItem.OperatorEntry -> OperatorCard(
                                model = item.model,
                                position = item.position,
                                onUse = {
                                    viewModel.useName(item.model.operator.name)
                                    onBack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val editingFunction = functionToEdit
    if (showAddDialog || editingFunction != null) {
        FunctionEditDialog(
            initialName = editingFunction?.title.orEmpty(),
            initialBody = editingFunction?.body.orEmpty(),
            initialParameters = editingFunction?.parameters.orEmpty(),
            initialDescription = editingFunction?.description.orEmpty(),
            isEditing = editingFunction != null,
            onDismiss = {
                showAddDialog = false
                functionToEdit = null
            },
            onSave = { name, body, parameters, description ->
                val error = if (editingFunction == null) {
                    viewModel.save(null, name, body, parameters, description)
                } else {
                    viewModel.update(editingFunction.function, name, body, parameters, description)
                }
                error ?: run {
                    showAddDialog = false
                    functionToEdit = null
                    null
                }
            }
        )
    }

    val pendingDelete = functionToDelete
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { functionToDelete = null },
            title = { Text(stringResource(Res.string.removal_confirmation)) },
            text = {
                Text(
                    text = stringResource(
                        Res.string.function_removal_confirmation_question,
                        pendingDelete.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.remove(pendingDelete)
                        functionToDelete = null
                    }
                ) {
                    Text(stringResource(Res.string.cpp_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { functionToDelete = null }) {
                    Text(stringResource(Res.string.cpp_back))
                }
            }
        )
    }
}

@Composable
private fun EmptyFunctionsState(
    selectedType: FunctionEntityType,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (selectedType == FunctionEntityType.Functions) {
                stringResource(Res.string.cpp_functions)
            } else {
                stringResource(Res.string.cpp_operators)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.cpp_entities_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (selectedType == FunctionEntityType.Functions) {
            Spacer(modifier = Modifier.height(18.dp))
            TextButton(onClick = onCreate) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(Res.string.function_create_function))
            }
        }
    }
}

@Composable
private fun FunctionSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp, start = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FunctionCard(
    model: FunctionUiModel,
    position: FunctionCardPosition,
    onUse: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SegmentedListItem(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onUse,
        shapes = segmentedShapesFor(position),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        trailingContent = if (model.canEdit || model.canDelete) {
            {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (model.canEdit) {
                        FilledTonalIconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(Res.string.function_edit_function)
                            )
                        }
                    }
                    if (model.canDelete) {
                        FilledTonalIconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = stringResource(Res.string.cpp_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        } else {
            null
        },
        supportingContent = if (!model.description.isNullOrBlank()) {
            {
                Text(
                    text = model.description,
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
                    text = model.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = normalizeMathDisplay(model.signature),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!model.body.isNullOrBlank()) {
                    Text(
                        text = "= ${normalizeMathDisplay(model.body)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
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
private fun OperatorCard(
    model: OperatorUiModel,
    position: FunctionCardPosition,
    onUse: () -> Unit
) {
    SegmentedListItem(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onUse,
        shapes = segmentedShapesFor(position),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        supportingContent = if (!model.description.isNullOrBlank()) {
            {
                Text(
                    text = model.description,
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
                    text = model.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!model.signature.isNullOrBlank()) {
                    Text(
                        text = normalizeMathDisplay(model.signature),
                        style = MaterialTheme.typography.bodyMedium.copy(
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

@Composable
fun FunctionEditDialog(
    initialName: String = "",
    initialBody: String = "",
    initialParameters: List<String> = emptyList(),
    initialDescription: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>, String) -> String?
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var body by remember(initialBody) { mutableStateOf(initialBody) }
    var parametersText by remember(initialParameters) { mutableStateOf(initialParameters.joinToString(", ")) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val emptyFieldMessage = stringResource(Res.string.cpp_field_cannot_be_empty)
    val duplicateParameterTemplate = stringResource(Res.string.cpp_duplicate_parameter, "{param}")

    val parsedParameters = remember(parametersText) {
        parametersText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    val duplicateParameter = remember(parsedParameters) {
        parsedParameters
            .groupBy { it.lowercase() }
            .entries
            .firstOrNull { it.value.size > 1 }
            ?.value
            ?.firstOrNull()
    }
    val canSave = name.isNotBlank() && body.isNotBlank() && duplicateParameter == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (isEditing) Res.string.function_edit_function
                    else Res.string.function_create_function
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
                    value = body,
                    onValueChange = {
                        body = it
                        errorText = null
                    },
                    label = { Text(stringResource(Res.string.cpp_function_body)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = parametersText,
                    onValueChange = {
                        parametersText = it
                        errorText = null
                    },
                    label = { Text(stringResource(Res.string.cpp_parameters)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text(stringResource(Res.string.cpp_parameters_example)) }
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
                    val duplicate = duplicateParameter
                    if (name.isBlank() || body.isBlank()) {
                        errorText = emptyFieldMessage
                        return@TextButton
                    }
                    if (duplicate != null) {
                        errorText = duplicateParameterTemplate.replace("{param}", duplicate)
                        return@TextButton
                    }
                    errorText = onSave(
                        name.trim(),
                        body.trim(),
                        parsedParameters,
                        description.trim()
                    )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun segmentedShapesFor(position: FunctionCardPosition) = when (position) {
    FunctionCardPosition.Single -> ListItemDefaults.segmentedShapes(index = 0, count = 1)
    FunctionCardPosition.First -> ListItemDefaults.segmentedShapes(index = 0, count = 2)
    FunctionCardPosition.Middle -> ListItemDefaults.segmentedShapes(index = 1, count = 3)
    FunctionCardPosition.Last -> ListItemDefaults.segmentedShapes(index = 1, count = 2)
}

private fun buildFunctionSignature(function: Function): String {
    val customFunction = function as? IFunction
    if (customFunction != null) {
        val names = customFunction.getParameterNames()
        return if (names.isEmpty()) {
            "${function.name}()"
        } else {
            "${function.name}(${names.joinToString(", ")})"
        }
    }
    return runCatching { function.toString() }.getOrDefault("${function.name}(x)")
}

private fun toFunctionListItems(functions: List<FunctionUiModel>): List<FunctionListItem> {
    if (functions.isEmpty()) return emptyList()
    val grouped = functions.groupBy { model ->
        val c = model.title.firstOrNull()?.uppercaseChar()
        if (c != null && c.isLetter()) c.toString() else "#"
    }.toList().sortedBy { it.first }.toMap()

    return buildList {
        grouped.forEach { (header, group) ->
            add(FunctionListItem.Header(id = "f_header_$header", label = header))
            group.forEachIndexed { index, function ->
                add(
                    FunctionListItem.FunctionEntry(
                        model = function,
                        position = cardPositionFor(index, group.size)
                    )
                )
            }
        }
    }
}

private fun toOperatorListItems(operators: List<OperatorUiModel>): List<FunctionListItem> {
    if (operators.isEmpty()) return emptyList()
    val grouped = operators.groupBy { model ->
        val c = model.title.firstOrNull()?.uppercaseChar()
        if (c != null && c.isLetter()) c.toString() else "#"
    }.toList().sortedBy { it.first }.toMap()

    return buildList {
        grouped.forEach { (header, group) ->
            add(FunctionListItem.Header(id = "o_header_$header", label = header))
            group.forEachIndexed { index, operator ->
                add(
                    FunctionListItem.OperatorEntry(
                        model = operator,
                        position = cardPositionFor(index, group.size)
                    )
                )
            }
        }
    }
}

private fun cardPositionFor(index: Int, totalCount: Int): FunctionCardPosition {
    return when {
        totalCount <= 1 -> FunctionCardPosition.Single
        index == 0 -> FunctionCardPosition.First
        index == totalCount - 1 -> FunctionCardPosition.Last
        else -> FunctionCardPosition.Middle
    }
}

private fun normalizeMathDisplay(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return value
        .replace('−', '-')
        .replace('×', '*')
        .replace('÷', '/')
        .replace('·', '*')
        .replace('∙', '*')
        .replace('⋅', '*')
}
