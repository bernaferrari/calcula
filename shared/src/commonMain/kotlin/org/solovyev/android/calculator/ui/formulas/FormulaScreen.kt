package org.solovyev.android.calculator.ui.formulas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.formulas.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaScreen(viewModel: FormulaViewModel, onUseResult: (String) -> Unit, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFormula by remember { mutableStateOf<Formula?>(null) }
    val formulas by viewModel.formulas.collectAsState()

    val filtered = remember(formulas, searchQuery) {
        if (searchQuery.isBlank()) formulas
        else formulas.filter { it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) }
    }
    val grouped = filtered.groupBy { it.category }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Formulas") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } })
    }) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(searchQuery, { searchQuery = it },
                placeholder = { Text("Search formulas...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true, shape = RoundedCornerShape(28.dp))

            LazyColumn(Modifier.fillMaxSize()) {
                grouped.forEach { (category, items) ->
                    item {
                        Text(category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                    }
                    items(items) { formula ->
                        ListItem(headlineContent = { Text(formula.name) },
                            supportingContent = { Text(formula.description) },
                            modifier = Modifier.clickable { selectedFormula = formula },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface))
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    selectedFormula?.let { FormulaInputSheet(it, viewModel, { selectedFormula = null }, onUseResult) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormulaInputSheet(formula: Formula, viewModel: FormulaViewModel, onDismiss: () -> Unit, onUseResult: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var values by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var result by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(16.dp)) {
            Text(formula.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(formula.description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text(formula.expression, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            }

            formula.variables.forEach { variable ->
                OutlinedTextField(values[variable.id] ?: variable.defaultValue, { values = values + (variable.id to it); result = null },
                    label = { Text("${variable.symbol} - ${variable.name}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), singleLine = true)
            }

            Button({ result = calculate(formula, values, viewModel) },
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                enabled = formula.variables.all { values[it.id]?.isNotBlank() == true }) { Text("Calculate") }

            result?.let {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Result", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(it, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onDismiss, Modifier.weight(1f)) { Text("Back") }
                    Button({ onUseResult(it); onDismiss() }, Modifier.weight(1f)) { Text("Use Result") }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun calculate(formula: Formula, values: Map<String, String>, viewModel: FormulaViewModel): String? = try {
    var expr = formula.expression
    formula.variables.forEach { expr = expr.replace(it.id, values[it.id] ?: it.defaultValue) }
    viewModel.evaluate(expr)
} catch (e: Exception) { "Error" }
