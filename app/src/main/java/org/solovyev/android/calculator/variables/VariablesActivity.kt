package org.solovyev.android.calculator.variables

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.BundleCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dagger.Lazy
import jscl.math.function.IConstant
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ToJsclTextProcessor
import org.solovyev.android.calculator.VariablesRegistry
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.ui.compose.entities.EntityListScreen
import org.solovyev.android.calculator.ui.compose.entities.EntityMenuItem
import org.solovyev.android.calculator.ui.compose.entities.EntityRowModel
import org.solovyev.android.calculator.ui.compose.entities.EntityTab
import org.solovyev.android.calculator.ui.compose.entities.VariablesComposeViewModel
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import javax.inject.Inject

@AndroidEntryPoint
open class VariablesActivity : BaseActivity(R.string.cpp_vars_and_constants) {

    @Inject
    lateinit var variablesRegistry: VariablesRegistry

    @Inject
    lateinit var engine: Engine

    @Inject
    lateinit var toJsclTextProcessor: Lazy<ToJsclTextProcessor>

    @Composable
    override fun Content() {
        val viewModel: VariablesComposeViewModel = hiltViewModel()
        val refreshTick by viewModel.refreshTick.collectAsStateWithLifecycle()
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        val context = LocalContext.current
        var editVariable by remember { mutableStateOf<CppVariable?>(null) }
        var deleteVariable by remember { mutableStateOf<IConstant?>(null) }

        CalculatorTheme(theme = themePreference) {
            val tabs = remember(refreshTick) {
                buildVariableTabs(
                    viewModel = viewModel,
                    context = context,
                    onEdit = { editVariable = it },
                    onDelete = { deleteVariable = it }
                )
            }

            EntityListScreen(
                title = getString(R.string.cpp_vars_and_constants),
                tabs = tabs,
                onBack = { finish() },
                floatingActionButton = {
                    FloatingActionButton(onClick = { editVariable = CppVariable.builder("").build() }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }
            )

            LaunchedEffect(refreshTick) {
                val variable = intent.extras?.let {
                    BundleCompat.getParcelable(it, EXTRA_VARIABLE, CppVariable::class.java)
                }
                if (variable != null) {
                    editVariable = variable
                    intent.removeExtra(EXTRA_VARIABLE)
                }
            }

            editVariable?.let { variable ->
                VariableEditDialog(
                    variable = variable,
                    onDismiss = { editVariable = null },
                    onSave = { updated ->
                        editVariable = null
                        if (updated != null) {
                            val oldVariable = if (updated.id == CppVariable.NO_ID) {
                                null
                            } else {
                                variablesRegistry.getById(updated.id)
                            }
                            variablesRegistry.addOrUpdate(updated.toJsclConstant(), oldVariable)
                        }
                    }
                )
            }

            deleteVariable?.let { variable ->
                AlertDialog(
                    onDismissRequest = { deleteVariable = null },
                    title = { Text(text = getString(R.string.removal_confirmation)) },
                    text = { Text(text = getString(R.string.c_var_removal_confirmation_question, variable.name)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                variablesRegistry.remove(variable)
                                deleteVariable = null
                            }
                        ) {
                            Text(text = getString(R.string.cpp_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteVariable = null }) {
                            Text(text = getString(R.string.cpp_cancel))
                        }
                    }
                )
            }
        }
    }

    private fun buildVariableTabs(
        viewModel: VariablesComposeViewModel,
        context: Context,
        onEdit: (CppVariable) -> Unit,
        onDelete: (IConstant) -> Unit
    ): List<EntityTab> {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return viewModel.getCategories().map { category ->
            val items = viewModel.getVariablesFor(category).map { variable ->
                val menuItems = mutableListOf<EntityMenuItem>()
                menuItems.add(
                    EntityMenuItem(
                        label = getString(R.string.c_use),
                        onClick = {
                            viewModel.useName(variable.name)
                            finish()
                        }
                    )
                )
                if (!variable.isSystem()) {
                    menuItems.add(
                        EntityMenuItem(
                            label = getString(R.string.cpp_edit),
                            onClick = { onEdit(CppVariable.builder(variable).build()) }
                        )
                    )
                    menuItems.add(
                        EntityMenuItem(
                            label = getString(R.string.cpp_delete),
                            onClick = { onDelete(variable) }
                        )
                    )
                }
                if (!variable.getValue().isNullOrEmpty()) {
                    menuItems.add(
                        EntityMenuItem(
                            label = getString(R.string.cpp_copy),
                            onClick = {
                                val value = variable.getValue()
                                if (!value.isNullOrEmpty()) {
                                    clipboard.setPrimaryClip(ClipData.newPlainText("var", value))
                                }
                            }
                        )
                    )
                }

                EntityRowModel(
                    id = "variable:${variable.name}",
                    title = viewModel.getDisplayName(variable),
                    subtitle = viewModel.getDescription(variable),
                    onUse = {
                        viewModel.useName(variable.name)
                        finish()
                    },
                    menuItems = menuItems
                )
            }
            EntityTab(
                title = getString(category.title),
                items = items
            )
        }
    }

    @Composable
    private fun VariableEditDialog(
        variable: CppVariable,
        onDismiss: () -> Unit,
        onSave: (CppVariable?) -> Unit
    ) {
        val nameState = remember(variable.id) { mutableStateOf(variable.name) }
        val valueState = remember(variable.id) { mutableStateOf(variable.value) }
        val descriptionState = remember(variable.id) { mutableStateOf(variable.description) }
        var nameError by remember { mutableStateOf<String?>(null) }
        var valueError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (variable.id == CppVariable.NO_ID) {
                        getString(R.string.c_var_create_var)
                    } else {
                        getString(R.string.c_var_edit_var)
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
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = valueState.value,
                            onValueChange = {
                                valueState.value = it
                                valueError = null
                            },
                            label = { Text(text = getString(R.string.c_var_value)) },
                            isError = valueError != null,
                            supportingText = valueError?.let { { Text(text = it) } },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                valueState.value = valueState.value + "E"
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Exposure, contentDescription = null)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descriptionState.value,
                        onValueChange = { descriptionState.value = it },
                        label = { Text(text = getString(R.string.cpp_description)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = nameState.value.trim()
                        val value = valueState.value.trim()
                        val description = descriptionState.value.trim()

                        val nameValid = validateVariableName(name, variable)
                        if (!nameValid.first) {
                            nameError = nameValid.second
                            return@TextButton
                        }

                        val valueValid = validateVariableValue(value)
                        if (!valueValid.first) {
                            valueError = valueValid.second
                            return@TextButton
                        }

                        val updated = CppVariable.builder(name)
                            .withId(variable.id)
                            .withValue(value)
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

    private fun validateVariableName(name: String, variable: CppVariable): Pair<Boolean, String?> {
        if (name.isEmpty()) {
            return false to getString(R.string.cpp_field_cannot_be_empty)
        }
        if (!Engine.isValidName(name)) {
            return false to getString(R.string.cpp_name_contains_invalid_characters)
        }

        for (c in name) {
            if (!ACCEPTABLE_CHARACTERS.contains(c.lowercaseChar())) {
                return false to getString(R.string.c_char_is_not_accepted, c)
            }
        }

        val existingVariable = variablesRegistry.get(name)
        if (existingVariable != null) {
            if (!existingVariable.isIdDefined()) {
                return false to getString(R.string.c_var_already_exists)
            }
            if (variable.id == CppVariable.NO_ID) {
                return false to getString(R.string.c_var_already_exists)
            }
            if (existingVariable.getId() != variable.id) {
                return false to getString(R.string.c_var_already_exists)
            }
        }

        val type = MathType.getType(name, 0, false, engine)
        if (type.type != MathType.text && type.type != MathType.constant) {
            return false to getString(R.string.c_var_name_clashes)
        }

        return true to null
    }

    private fun validateVariableValue(value: String): Pair<Boolean, String?> {
        if (value.isNotEmpty()) {
            if (!isValidValue(value)) {
                return false to getString(R.string.c_value_is_not_a_number)
            }
        }
        return true to null
    }

    private fun isValidValue(value: String): Boolean {
        return try {
            val pe = toJsclTextProcessor.get().process(value)
            !pe.hasUndefinedVariables()
        } catch (e: RuntimeException) {
            false
        }
    }

    class Dialog : VariablesActivity()

    companion object {
        const val EXTRA_VARIABLE = "variable"

        private const val GREEK_ALPHABET = "αβγδεζηθικλμνξοπρστυφχψω"
        private val ACCEPTABLE_CHARACTERS = (
            "1234567890abcdefghijklmnopqrstuvwxyzйцукенгшщзхъфывапролджэячсмитьбюё_" +
                GREEK_ALPHABET
            ).toList()

        @JvmStatic
        fun getClass(context: Context): Class<out VariablesActivity> =
            if (App.isTablet(context)) Dialog::class.java else VariablesActivity::class.java
    }
}
