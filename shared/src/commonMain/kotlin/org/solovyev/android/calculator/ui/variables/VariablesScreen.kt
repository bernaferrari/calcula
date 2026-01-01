package org.solovyev.android.calculator.ui.variables

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.solovyev.android.calculator.ui.entities.*
import org.solovyev.android.calculator.ui.*

@OptIn(KoinExperimentalAPI::class)
@Composable
fun VariablesScreen(
    onBack: () -> Unit,
    viewModel: VariablesViewModel = koinViewModel()
) {
    val tick by viewModel.refreshTick.collectAsState()
    
    val categories = viewModel.getCategories()
    val titles = categories.map { stringResource(it.title) }

    // Dialog State
    var showAddDialog by remember { mutableStateOf(false) }

    val tabs = remember(categories, tick, titles) {
        categories.zip(titles).map { (category, title) ->
            EntityTab(
                title = title,
                items = viewModel.getVariablesFor(category).map { variable ->
                    EntityRowModel(
                        id = variable.name,
                        title = viewModel.getDisplayName(variable),
                        subtitle = viewModel.getDescription(variable),
                        onUse = { viewModel.useName(variable.name); onBack() },
                        menuItems = listOf(
                            EntityMenuItem("Delete") { viewModel.remove(variable) }
                        )
                    )
                }
            )
        }
    }

    EntityListScreen(
        title = "Variables", // stringResource(Res.string.cpp_variables),
        tabs = tabs,
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    )

    if (showAddDialog) {
        VariableEditDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, value, desc ->
                viewModel.add(name, value, desc)
                showAddDialog = false
            }
        )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Variable") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                TextField(value = value, onValueChange = { value = it }, label = { Text("Value") }, singleLine = true)
                TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, value, description) },
                enabled = name.isNotBlank() && value.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
