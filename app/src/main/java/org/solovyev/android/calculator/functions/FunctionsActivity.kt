package org.solovyev.android.calculator.functions

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.BundleCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import jscl.math.function.Function
import jscl.math.function.IFunction
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.Utils
import org.solovyev.android.calculator.ParseException
import org.solovyev.android.calculator.ui.compose.entities.EntityListScreen
import org.solovyev.android.calculator.ui.compose.entities.EntityMenuItem
import org.solovyev.android.calculator.ui.compose.entities.EntityRowModel
import org.solovyev.android.calculator.ui.compose.entities.EntityTab
import org.solovyev.android.calculator.ui.compose.entities.FunctionsComposeViewModel
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import javax.inject.Inject

@AndroidEntryPoint
open class FunctionsActivity : BaseActivity(R.string.c_functions) {

    @Inject
    lateinit var functionsRegistry: FunctionsRegistry

    @Composable
    override fun Content() {
        val viewModel: FunctionsComposeViewModel = hiltViewModel()
        val refreshTick by viewModel.refreshTick.collectAsStateWithLifecycle()
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        var editFunction by remember { mutableStateOf<CppFunction?>(null) }
        var deleteFunction by remember { mutableStateOf<Function?>(null) }

        CalculatorTheme(theme = themePreference) {
            val tabs = remember(refreshTick) {
                buildFunctionTabs(
                    viewModel,
                    onEdit = { editFunction = it },
                    onDelete = { deleteFunction = it }
                )
            }

            EntityListScreen(
                title = getString(R.string.c_functions),
                tabs = tabs,
                onBack = { finish() },
                floatingActionButton = {
                    FloatingActionButton(onClick = { editFunction = CppFunction.builder("", "").build() }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }
            )

            LaunchedEffect(refreshTick) {
                val function = intent.extras?.let {
                    BundleCompat.getParcelable(it, EXTRA_FUNCTION, CppFunction::class.java)
                }
                if (function != null) {
                    editFunction = function
                    intent.removeExtra(EXTRA_FUNCTION)
                }
            }

            editFunction?.let { function ->
                FunctionEditDialog(
                    function = function,
                    onDismiss = { editFunction = null },
                    onSave = { updated ->
                        editFunction = null
                        if (updated != null) {
                            val oldFunction = if (updated.id == CppFunction.NO_ID) {
                                null
                            } else {
                                functionsRegistry.getById(updated.id)
                            }
                            functionsRegistry.addOrUpdate(updated.toJsclBuilder().create(), oldFunction)
                        }
                    }
                )
            }

            deleteFunction?.let { function ->
                AlertDialog(
                    onDismissRequest = { deleteFunction = null },
                    title = { Text(text = getString(R.string.removal_confirmation)) },
                    text = { Text(text = getString(R.string.function_removal_confirmation_question, function.name)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                functionsRegistry.remove(function)
                                deleteFunction = null
                            }
                        ) {
                            Text(text = getString(R.string.cpp_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteFunction = null }) {
                            Text(text = getString(R.string.cpp_cancel))
                        }
                    }
                )
            }
        }
    }

    private fun buildFunctionTabs(
        viewModel: FunctionsComposeViewModel,
        onEdit: (CppFunction) -> Unit,
        onDelete: (Function) -> Unit
    ): List<EntityTab> {
        val functionTabs = viewModel.getFunctionCategories().map { category ->
            val items = viewModel.getFunctionsFor(category).map { function ->
                EntityRowModel(
                    id = "function:${function.name}",
                    title = function.toString(),
                    subtitle = viewModel.getFunctionDescription(function),
                    onUse = {
                        viewModel.useName(function.name)
                        finish()
                    },
                    menuItems = buildFunctionMenu(viewModel, function, onEdit, onDelete)
                )
            }
            EntityTab(
                title = getString(category.title),
                items = items
            )
        }

        val operatorTabs = viewModel.getOperatorCategories().map { category ->
            val items = viewModel.getOperatorsFor(category).map { operator ->
                EntityRowModel(
                    id = "operator:${operator.name}",
                    title = operator.toString(),
                    subtitle = viewModel.getOperatorDescription(operator),
                    onUse = {
                        viewModel.useName(operator.name)
                        finish()
                    },
                    menuItems = listOf(
                        EntityMenuItem(
                            label = getString(R.string.c_use),
                            onClick = {
                                viewModel.useName(operator.name)
                                finish()
                            }
                        )
                    )
                )
            }
            EntityTab(
                title = getString(category.title),
                items = items
            )
        }

        return functionTabs + operatorTabs
    }

    private fun buildFunctionMenu(
        viewModel: FunctionsComposeViewModel,
        function: Function,
        onEdit: (CppFunction) -> Unit,
        onDelete: (Function) -> Unit
    ): List<EntityMenuItem> {
        val items = mutableListOf<EntityMenuItem>()
        items.add(
            EntityMenuItem(
                label = getString(R.string.c_use),
                onClick = {
                    viewModel.useName(function.name)
                    finish()
                }
            )
        )

        if (!function.isSystem()) {
            items.add(
                EntityMenuItem(
                    label = getString(R.string.cpp_edit),
                    onClick = {
                        if (function is IFunction) {
                            onEdit(CppFunction.builder(function).build())
                        }
                    }
                )
            )
            items.add(
                EntityMenuItem(
                    label = getString(R.string.cpp_delete),
                    onClick = { onDelete(function) }
                )
            )
        }

        return items
    }

    @Composable
    private fun FunctionEditDialog(
        function: CppFunction,
        onDismiss: () -> Unit,
        onSave: (CppFunction?) -> Unit
    ) {
        val initialParams = remember(function.id) { function.parameters.toList() }
        val params = remember(function.id) { mutableStateListOf<String>().apply { addAll(initialParams) } }
        val nameState = remember(function.id) { mutableStateOf(function.name) }
        val bodyState = remember(function.id) { mutableStateOf(function.body) }
        val descriptionState = remember(function.id) { mutableStateOf(function.description) }
        var nameError by remember { mutableStateOf<String?>(null) }
        var bodyError by remember { mutableStateOf<String?>(null) }
        var paramsError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (function.id == CppFunction.NO_ID) {
                        getString(R.string.function_create_function)
                    } else {
                        getString(R.string.function_edit_function)
                    }
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameState.value,
                        onValueChange = {
                            nameState.value = it
                            nameError = null
                        },
                        label = { Text(text = getString(R.string.cpp_name)) },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(text = it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bodyState.value,
                        onValueChange = {
                            bodyState.value = it
                            bodyError = null
                        },
                        label = { Text(text = getString(R.string.cpp_function_body)) },
                        isError = bodyError != null,
                        supportingText = bodyError?.let { { Text(text = it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descriptionState.value,
                        onValueChange = { descriptionState.value = it },
                        label = { Text(text = getString(R.string.cpp_description)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = getString(R.string.cpp_parameters))
                    params.forEachIndexed { index, param ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = param,
                                onValueChange = { value ->
                                    params[index] = value
                                    paramsError = null
                                },
                                label = { Text(text = getString(R.string.cpp_parameter)) },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { params.removeAt(index) }) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                            }
                        }
                    }
                    if (paramsError != null) {
                        Text(
                            text = paramsError.orEmpty(),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error
                        )
                    }
                    TextButton(onClick = { params.add("") }) {
                        Text(text = getString(R.string.cpp_add_parameter))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = nameState.value.trim()
                        val body = bodyState.value.trim()
                        val description = descriptionState.value.trim()
                        val paramList = params.map { it.trim() }.filter { it.isNotEmpty() }

                        val nameValid = validateFunctionName(name, function)
                        if (!nameValid.first) {
                            nameError = nameValid.second
                            return@TextButton
                        }

                        val paramsValid = validateParameters(paramList)
                        if (!paramsValid.first) {
                            paramsError = paramsValid.second
                            return@TextButton
                        }

                        val bodyValid = validateBody(body, paramList)
                        if (!bodyValid.first) {
                            bodyError = bodyValid.second
                            return@TextButton
                        }

                        val prepared = calculator.prepare(body).value
                        val updated = CppFunction.builder(name, prepared)
                            .withId(function.id)
                            .withParameters(paramList)
                            .withDescription(description)
                            .build()
                        onSave(updated)
                    }
                ) {
                    Text(text = getString(R.string.cpp_done))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = getString(R.string.cpp_cancel))
                }
            }
        )
    }

    private fun validateFunctionName(name: String, function: CppFunction): Pair<Boolean, String?> {
        if (name.isEmpty()) {
            return false to getString(R.string.cpp_field_cannot_be_empty)
        }
        if (!Engine.isValidName(name)) {
            return false to getString(R.string.cpp_name_contains_invalid_characters)
        }
        val existing = functionsRegistry.get(name)
        if (existing != null) {
            if (!existing.isIdDefined()) {
                return false to getString(R.string.function_already_exists)
            }
            if (function.id == CppFunction.NO_ID) {
                return false to getString(R.string.function_already_exists)
            }
            if (existing.getId() != function.id) {
                return false to getString(R.string.function_already_exists)
            }
        }
        return true to null
    }

    private fun validateParameters(parameters: List<String>): Pair<Boolean, String?> {
        val seen = mutableSetOf<String>()
        for (param in parameters) {
            if (!Engine.isValidName(param)) {
                return false to getString(R.string.cpp_name_contains_invalid_characters)
            }
            if (!seen.add(param)) {
                return false to getString(R.string.cpp_duplicate_parameter, param)
            }
        }
        return true to null
    }

    private fun validateBody(body: String, parameters: List<String>): Pair<Boolean, String?> {
        if (body.isEmpty()) {
            return false to getString(R.string.cpp_field_cannot_be_empty)
        }
        return try {
            val pe = calculator.prepare(body)
            if (pe.hasUndefinedVariables()) {
                for (undefined in pe.undefinedVariables) {
                    if (!parameters.contains(undefined.name)) {
                        return false to getString(R.string.c_error)
                    }
                }
            }
            true to null
        } catch (e: ParseException) {
            false to Utils.getErrorMessage(e)
        }
    }

    class Dialog : FunctionsActivity()

    companion object {
        const val EXTRA_FUNCTION = "function"

        @JvmStatic
        fun getClass(context: Context): Class<out FunctionsActivity> =
            if (App.isTablet(context)) Dialog::class.java else FunctionsActivity::class.java
    }
}
