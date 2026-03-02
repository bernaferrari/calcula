package org.solovyev.android.calculator.ui.functions

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import jscl.math.function.Function
import jscl.math.function.IFunction
import jscl.math.operator.Operator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.solovyev.android.calculator.functions.FunctionCategory
import org.solovyev.android.calculator.ui.*

private sealed interface ChipCategory {
    data class FunctionCat(val category: FunctionCategory) : ChipCategory
    data object Operators : ChipCategory
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
    val operatorsLabel = stringResource(Res.string.cpp_operators)

    // Unified chip list: move "My" (index 0) to end so "Common" (index 1) becomes the default
    val chipCategories: List<ChipCategory> = remember(functionCategories) {
        val myCategory = functionCategories.firstOrNull()
        val others = functionCategories.drop(1)
        buildList {
            others.forEach { add(ChipCategory.FunctionCat(it)) }
            add(ChipCategory.Operators)
            myCategory?.let { add(ChipCategory.FunctionCat(it)) }
        }
    }

    var selectedChipIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedChip = chipCategories.getOrNull(selectedChipIndex)

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var functionToEdit by remember { mutableStateOf<FunctionUiModel?>(null) }
    var functionToDelete by remember { mutableStateOf<Function?>(null) }

    // "My functions" is the last chip (moved from front to end)
    val myCategory = remember(functionCategories) { functionCategories.firstOrNull() }
    val isMyFunctionsSelected = selectedChip is ChipCategory.FunctionCat &&
        selectedChip.category == myCategory

    val functionCategoryCounts = remember(refreshVersion, functionCategories) {
        functionCategories.associateWith { viewModel.getFunctionsFor(it).size }
    }
    val totalOperatorCount = remember(refreshVersion) {
        operatorCategories.sumOf { viewModel.getOperatorsFor(it).size }
    }

    val normalizedQuery = remember(searchQuery) { searchQuery.trim().lowercase() }
    val visibleItems: List<FunctionListItem> = remember(refreshVersion, selectedChip, normalizedQuery) {
        when (val chip = selectedChip) {
            is ChipCategory.Operators -> {
                val allOps = operatorCategories
                    .flatMap { viewModel.getOperatorsFor(it) }
                    .distinctBy { it.name }
                    .map { op ->
                        OperatorUiModel(
                            operator = op,
                            title = op.name,
                            signature = runCatching { op.toString() }.getOrNull(),
                            description = viewModel.getOperatorDescription(op)
                        )
                    }
                    .filter { model ->
                        normalizedQuery.isEmpty() ||
                            model.title.lowercase().contains(normalizedQuery) ||
                            model.signature.orEmpty().lowercase().contains(normalizedQuery) ||
                            model.description.orEmpty().lowercase().contains(normalizedQuery)
                    }
                toOperatorListItems(allOps)
            }
            is ChipCategory.FunctionCat -> {
                viewModel.getFunctionsFor(chip.category)
                    .map { function ->
                        FunctionUiModel(
                            function = function,
                            title = function.name,
                            signature = buildFunctionSignature(function),
                            parameters = (function as? IFunction)?.getParameterNames().orEmpty(),
                            body = (function as? IFunction)?.getContent(),
                            description = viewModel.getFunctionDescription(function)
                                ?: (function as? IFunction)?.getDescription(),
                            canEdit = !function.isSystem(),
                            canDelete = !function.isSystem()
                        )
                    }
                    .filter { model ->
                        normalizedQuery.isEmpty() ||
                            model.title.lowercase().contains(normalizedQuery) ||
                            model.signature.lowercase().contains(normalizedQuery) ||
                            model.body.orEmpty().lowercase().contains(normalizedQuery) ||
                            model.description.orEmpty().lowercase().contains(normalizedQuery)
                    }
                    .let { toFunctionListItems(it) }
            }
            null -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(Res.string.c_functions),
                onBack = onBack,
                actions = {
                    if (isMyFunctionsSelected) {
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

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(chipCategories.size, key = { it }) { index ->
                    val chip = chipCategories[index]
                    val isSelected = selectedChipIndex == index
                    val label = when (chip) {
                        is ChipCategory.Operators -> "$operatorsLabel ($totalOperatorCount)"
                        is ChipCategory.FunctionCat -> {
                            val catIndex = functionCategories.indexOf(chip.category)
                            val title = functionCategoryTitles.getOrElse(catIndex) { "" }
                            val count = functionCategoryCounts[chip.category] ?: 0
                            "$title ($count)"
                        }
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedChipIndex = index },
                        label = {
                            Text(
                                text = label,
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

            if (visibleItems.isEmpty()) {
                EmptyFunctionsState(
                    isMyFunctions = isMyFunctionsSelected,
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
    isMyFunctions: Boolean,
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
                    text = "λ",
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
            text = stringResource(Res.string.cpp_functions),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.cpp_entities_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isMyFunctions) {
            Spacer(modifier = Modifier.height(20.dp))
            FilledTonalButton(onClick = onCreate) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(Res.string.function_create_function))
            }
        }
    }
}

@Composable
private fun FunctionSectionHeader(text: String) {
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
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (model.canEdit)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = model.title.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (model.canEdit)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        },
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
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = model.title.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
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
