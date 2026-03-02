package org.solovyev.android.calculator.ui.formulas

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.formulas.*
import org.solovyev.android.calculator.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaDetailSheet(
    formula: Formula,
    viewModel: FormulaViewModel,
    onDismiss: () -> Unit,
    onUseInCalculator: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showInfo by remember { mutableStateOf(false) }
    val reduceMotion = LocalCalculatorReduceMotion.current
    var variableValues by remember(formula.id) {
        mutableStateOf(formula.variables.associate { it.id to it.defaultValue })
    }

    val result by remember(variableValues) {
        derivedStateOf {
            var expression = formula.expression
            formula.variables.forEach { variable ->
                val value = variableValues[variable.id] ?: variable.defaultValue
                expression = expression.replace(variable.id, value)
            }
            try {
                viewModel.evaluate(expression)
            } catch (e: Exception) {
                ""
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formula.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formula.category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(onClick = { showInfo = !showInfo }) {
                    Text(stringResource(Res.string.cpp_formula_description))
                }
            }

            // Description
            AnimatedVisibility(
                visible = showInfo,
                enter = if (reduceMotion) fadeIn(tween(durationMillis = 70)) else fadeIn() + expandVertically(),
                exit = if (reduceMotion) fadeOut(tween(durationMillis = 50)) else fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = formula.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Formula display
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = formula.expression,
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Variables section
            Text(
                text = stringResource(Res.string.cpp_formula_variables),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            formula.variables.forEach { variable ->
                VariableInputField(
                    variable = variable,
                    value = variableValues[variable.id] ?: variable.defaultValue,
                    onValueChange = { value ->
                        variableValues = variableValues + (variable.id to value)
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Result display
            AnimatedVisibility(
                visible = result.isNotEmpty(),
                enter = if (reduceMotion) fadeIn(tween(durationMillis = 70)) else fadeIn() + expandVertically(),
                exit = if (reduceMotion) fadeOut(tween(durationMillis = 50)) else fadeOut() + shrinkVertically()
            ) {
                ResultCard(
                    result = result,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.cpp_a11y_close))
                }

                Button(
                    onClick = {
                        onUseInCalculator(formula.expression)
                    },
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Calculate,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.cpp_formula_use))
                }
            }

            // Secondary action for existing result
            if (result.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onUseInCalculator(result)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.c_use))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VariableInputField(
    variable: FormulaVariable,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var hasError by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Validate numeric input
            hasError = newValue.isNotEmpty() && newValue.toDoubleOrNull() == null
            onValueChange(newValue)
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text("${variable.name} (${variable.symbol})") },
        placeholder = { Text(variable.defaultValue) },
        leadingIcon = {
            Text(
                text = variable.symbol,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        },
        isError = hasError,
        supportingText = {
            if (hasError) {
                Text(stringResource(Res.string.c_value_is_not_a_number))
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun ResultCard(
    result: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.cpp_formula_result),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )

            Text(
                text = result,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFormulaSelector(
    onFormulaSelected: (Formula) -> Unit,
    onDismiss: () -> Unit
) {
    val allFormulas = remember { FormulaLibrary.getAll() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(Res.string.cpp_formula_quick_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Show all formulas (simplified - no recent/favorites tracking)
            allFormulas.take(8).forEach { formula ->
                ListItem(
                    headlineContent = { Text(formula.name) },
                    supportingContent = { Text(formula.category.displayName, maxLines = 1) },
                    leadingContent = {
                        Text(
                            text = "f(x)",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.clickable(onClick = { onFormulaSelected(formula) })
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Browse all button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.cpp_formula_library))
            }
        }
    }
}
