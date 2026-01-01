package org.solovyev.android.calculator.ui.functions

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
fun FunctionsScreen(
    onBack: () -> Unit,
    viewModel: FunctionsViewModel = koinViewModel()
) {
    val tick by viewModel.refreshTick.collectAsState()

    val funCategories = viewModel.getFunctionCategories()
    val opCategories = viewModel.getOperatorCategories()
    
    val funTitles = funCategories.map { stringResource(it.title) }
    val opTitles = opCategories.map { stringResource(it.title) }

    // Dialog State
    var showAddDialog by remember { mutableStateOf(false) }

    val tabs = remember(funCategories, opCategories, tick, funTitles, opTitles) {
        val functionTabs = funCategories.zip(funTitles).map { (category, title) ->
            EntityTab(
                title = title,
                items = viewModel.getFunctionsFor(category).map { function ->
                    EntityRowModel(
                        id = function.name,
                        title = function.toString(),
                        subtitle = viewModel.getFunctionDescription(function),
                        onUse = { viewModel.useName(function.name); onBack() },
                        menuItems = listOf(
                            EntityMenuItem("Delete") { viewModel.remove(function) }
                        )
                    )
                }
            )
        }
        val operatorTabs = opCategories.zip(opTitles).map { (category, title) ->
            EntityTab(
                title = title,
                items = viewModel.getOperatorsFor(category).map { operator ->
                    EntityRowModel(
                        id = operator.name,
                        title = operator.name, // or description?
                        subtitle = viewModel.getOperatorDescription(operator),
                        onUse = { viewModel.useName(operator.name); onBack() },
                        menuItems = emptyList() // Operators mostly static
                    )
                }
            )
        }
        functionTabs + operatorTabs
    }

    EntityListScreen(
        title = stringResource(Res.string.c_functions),
        tabs = tabs,
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    )

    if (showAddDialog) {
        FunctionEditDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, body, params, desc ->
                viewModel.add(name, body, params, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FunctionEditDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var params by remember { mutableStateOf("") } // Comma separated
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Function") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                TextField(value = body, onValueChange = { body = it }, label = { Text("Body Expression") }, singleLine = true)
                TextField(value = params, onValueChange = { params = it }, label = { Text("Parameters (x, y...)") }, singleLine = true)
                TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    val paramList = params.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onSave(name, body, paramList, description) 
                },
                enabled = name.isNotBlank() && body.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
