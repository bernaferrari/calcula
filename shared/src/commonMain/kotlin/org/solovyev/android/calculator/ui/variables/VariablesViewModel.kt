package org.solovyev.android.calculator.ui.variables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jscl.math.function.IConstant
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.VariablesRegistry
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.variables.CppVariable
import org.solovyev.android.calculator.variables.VariableCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VariablesViewModel(
    private val registry: VariablesRegistry,
    private val editor: Editor,
    private val engine: Engine
) : ViewModel() {

    private val refreshTickState = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = refreshTickState.asStateFlow()

    init {
        viewModelScope.launch { registry.addedEvents.collect { incrementRefreshTick() } }
        viewModelScope.launch { registry.changedEvents.collect { incrementRefreshTick() } }
        viewModelScope.launch { registry.removedEvents.collect { incrementRefreshTick() } }
    }

    private fun incrementRefreshTick() {
        refreshTickState.value++
    }

    fun getCategories(): List<VariableCategory> = VariableCategory.values().toList()

    fun getVariablesFor(category: VariableCategory): List<IConstant> {
        return registry.getEntities()
            .filterNot { it.name == "infinity" || it.name == "nan" }
            .filter { category.isInCategory(it) }
            .sortedBy { it.name }
    }

    fun getDescription(variable: IConstant): String? = registry.getDescription(variable.name)

    fun getValue(variable: IConstant): String? {
        if (!variable.isDefined()) return null
        return runCatching { variable.toString() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    fun getDisplayName(variable: IConstant): String {
        if (!variable.isDefined()) return variable.name
        val value = variable.toString()
        return if (value.isNotEmpty()) "${variable.name} = $value" else variable.name
    }

    fun useName(name: String) {
        editor.insert(name)
    }

    fun add(name: String, value: String, description: String) {
        save(null, name, value, description)
    }

    fun update(original: IConstant, name: String, value: String, description: String): String? {
        return save(original, name, value, description)
    }

    fun save(original: IConstant?, name: String, value: String, description: String): String? {
        val normalizedName = name.trim()
        val normalizedValue = value.trim()
        val normalizedDescription = description.trim()
        if (!Engine.isValidName(normalizedName)) {
            return "Name contains invalid characters"
        }

        val existing = registry.get(normalizedName)
        val originalId = original?.takeIf { it.isIdDefined() }?.getId()
        if (existing != null) {
            if (originalId == null || !existing.isIdDefined() || existing.getId() != originalId) {
                return "Variable already exists"
            }
        }

        val tokenType = MathType.getType(normalizedName, 0, false, engine).type
        if (tokenType != MathType.text && tokenType != MathType.constant) {
            return "Name clashes with existing symbols"
        }

        if (normalizedValue.isNotEmpty()) {
            val validValue = runCatching {
                engine.getMathEngine().evaluate(normalizedValue)
            }.isSuccess
            if (!validValue) {
                return "Value is not valid"
            }
        }

        val builder = CppVariable.builder(normalizedName)
            .withValue(normalizedValue)
            .withDescription(normalizedDescription)

        if (originalId != null) {
            builder.withId(originalId)
        }

        return runCatching {
            val oldById = originalId?.let { registry.getById(it) }
            if (oldById != null && oldById.name != normalizedName) {
                registry.remove(oldById)
            }
            registry.addOrUpdate(builder.build().toJsclConstant())
        }.exceptionOrNull()?.message
    }

    fun remove(variable: IConstant) {
        registry.remove(variable)
    }
}
