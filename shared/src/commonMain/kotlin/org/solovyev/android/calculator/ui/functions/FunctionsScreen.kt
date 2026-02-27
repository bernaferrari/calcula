package org.solovyev.android.calculator.ui.functions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
    val body: String?,
    val description: String?,
    val canDelete: Boolean
)

private data class OperatorUiModel(
    val operator: Operator,
    val title: String,
    val signature: String?,
    val description: String?
)

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun FunctionsScreen(
    onBack: () -> Unit,
    viewModel: FunctionsViewModel = koinViewModel()
) {
    val tick by viewModel.refreshTick.collectAsState()
    val functionCategories = remember { viewModel.getFunctionCategories() }
    val operatorCategories = remember { viewModel.getOperatorCategories() }
    val functionCategoryTitles = functionCategories.map { stringResource(it.title) }
    val operatorCategoryTitles = operatorCategories.map { stringResource(it.title) }

    var selectedTypeIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedFunctionCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedOperatorCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var functionToDelete by remember { mutableStateOf<Function?>(null) }

    val selectedType = if (selectedTypeIndex == 0) FunctionEntityType.Functions else FunctionEntityType.Operators
    val selectedFunctionCategory = functionCategories.getOrElse(selectedFunctionCategoryIndex) { FunctionCategory.common }
    val selectedOperatorCategory = operatorCategories.getOrElse(selectedOperatorCategoryIndex) { OperatorCategory.Common }
    val functionAlreadyExists = stringResource(Res.string.function_already_exists)

    val functionCategoryCounts = remember(tick, functionCategories) {
        functionCategories.associateWith { category -> viewModel.getFunctionsFor(category).size }
    }
    val operatorCategoryCounts = remember(tick, operatorCategories) {
        operatorCategories.associateWith { category -> viewModel.getOperatorsFor(category).size }
    }

    val functionModels = remember(tick, selectedFunctionCategory, query) {
        val normalized = query.trim().lowercase()
        viewModel.getFunctionsFor(selectedFunctionCategory)
            .map { function ->
                val signature = buildFunctionSignature(function)
                val body = (function as? IFunction)?.getContent()
                val description = viewModel.getFunctionDescription(function)
                    ?: (function as? IFunction)?.getDescription()
                FunctionUiModel(
                    function = function,
                    title = function.name,
                    signature = signature,
                    body = body,
                    description = description,
                    canDelete = !function.isSystem()
                )
            }
            .filter { model ->
                if (normalized.isEmpty()) {
                    true
                } else {
                    model.title.lowercase().contains(normalized) ||
                        model.signature.lowercase().contains(normalized) ||
                        model.body.orEmpty().lowercase().contains(normalized) ||
                        model.description.orEmpty().lowercase().contains(normalized)
                }
            }
    }

    val operatorModels = remember(tick, selectedOperatorCategory, query) {
        val normalized = query.trim().lowercase()
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
                if (normalized.isEmpty()) {
                    true
                } else {
                    model.title.lowercase().contains(normalized) ||
                        model.signature.orEmpty().lowercase().contains(normalized) ||
                        model.description.orEmpty().lowercase().contains(normalized)
                }
            }
    }

    val listItems = remember(functionModels, operatorModels, selectedType) {
        when (selectedType) {
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
                    if (selectedType == FunctionEntityType.Functions) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
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
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                val labels = listOf(
                    stringResource(Res.string.cpp_functions),
                    stringResource(Res.string.cpp_operators)
                )
                labels.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedTypeIndex == index,
                        onClick = { selectedTypeIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = labels.size)
                    ) {
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val categoryTitles = if (selectedType == FunctionEntityType.Functions) {
                functionCategoryTitles
            } else {
                operatorCategoryTitles
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(categoryTitles.size, key = { it }) { index ->
                    val isSelected = if (selectedType == FunctionEntityType.Functions) {
                        selectedFunctionCategoryIndex == index
                    } else {
                        selectedOperatorCategoryIndex == index
                    }
                    val count = if (selectedType == FunctionEntityType.Functions) {
                        functionCategoryCounts[functionCategories[index]] ?: 0
                    } else {
                        operatorCategoryCounts[operatorCategories[index]] ?: 0
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (selectedType == FunctionEntityType.Functions) {
                                selectedFunctionCategoryIndex = index
                            } else {
                                selectedOperatorCategoryIndex = index
                            }
                        },
                        label = {
                            Text(
                                text = "${categoryTitles[index]} ($count)",
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
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null
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

            if (listItems.isEmpty()) {
                EmptyFunctionsState(
                    selectedType = selectedType,
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
                    items(listItems, key = { item ->
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

    if (showAddDialog) {
        FunctionEditDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, body, parameters, description ->
                runCatching {
                    viewModel.add(name, body, parameters, description)
                }.exceptionOrNull()?.let { throwable ->
                    throwable.message ?: functionAlreadyExists
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
                    imageVector = Icons.Default.Add,
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

@Composable
private fun FunctionCard(
    model: FunctionUiModel,
    position: FunctionCardPosition,
    onUse: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = cardShapeFor(position)

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
                    text = model.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = model.signature,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!model.body.isNullOrBlank()) {
                    Text(
                        text = "= ${model.body}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!model.description.isNullOrBlank()) {
                    Text(
                        text = model.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (model.canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.cpp_delete)
                    )
                }
            }
        }
    }
}

@Composable
private fun OperatorCard(
    model: OperatorUiModel,
    position: FunctionCardPosition,
    onUse: () -> Unit
) {
    val shape = cardShapeFor(position)

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
                    text = model.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!model.signature.isNullOrBlank()) {
                    Text(
                        text = model.signature,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!model.description.isNullOrBlank()) {
                    Text(
                        text = model.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }
    }
}

@Composable
fun FunctionEditDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>, String) -> String?
) {
    var name by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var parametersText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
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
        title = { Text(stringResource(Res.string.function_create_function)) },
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
                    placeholder = { Text("x, y") }
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

private fun cardShapeFor(position: FunctionCardPosition): RoundedCornerShape {
    return when (position) {
        FunctionCardPosition.Single -> RoundedCornerShape(18.dp)
        FunctionCardPosition.First -> RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 6.dp,
            bottomEnd = 6.dp
        )
        FunctionCardPosition.Middle -> RoundedCornerShape(6.dp)
        FunctionCardPosition.Last -> RoundedCornerShape(
            topStart = 6.dp,
            topEnd = 6.dp,
            bottomStart = 18.dp,
            bottomEnd = 18.dp
        )
    }
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
    }.toSortedMap()

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
    }.toSortedMap()

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
