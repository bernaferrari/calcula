package org.solovyev.android.calculator.ui.variables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jscl.math.function.IConstant
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.VariablesRegistry
import org.solovyev.android.calculator.variables.CppVariable
import org.solovyev.android.calculator.variables.VariableCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VariablesViewModel(
    private val registry: VariablesRegistry,
    private val editor: Editor
) : ViewModel() {

    private val _refreshTick = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = _refreshTick.asStateFlow()

    init {
        viewModelScope.launch { registry.addedEvents.collect { bump() } }
        viewModelScope.launch { registry.changedEvents.collect { bump() } }
        viewModelScope.launch { registry.removedEvents.collect { bump() } }
    }

    private fun bump() {
        _refreshTick.value++
    }

    fun getCategories(): List<VariableCategory> = VariableCategory.values().toList()

    fun getVariablesFor(category: VariableCategory): List<IConstant> {
        return registry.getEntities()
            .filterNot { it.name == "infinity" || it.name == "nan" }
            .filter { category.isInCategory(it) }
            .sortedBy { it.name }
    }

    fun getDescription(variable: IConstant): String? = registry.getDescription(variable.name)

    fun getDisplayName(variable: IConstant): String {
        if (!variable.isDefined()) return variable.name
        val value = variable.toString()
        return if (value.isNotEmpty()) "${variable.name} = $value" else variable.name
    }

    fun useName(name: String) {
        editor.insert(name)
    }

    fun add(name: String, value: String, description: String) {
        val variable = CppVariable.builder(name)
            .withValue(value)
            .withDescription(description)
            .build()
            .toJsclConstant()
        registry.addOrUpdate(variable)
    }

    fun remove(variable: IConstant) {
        registry.remove(variable)
    }
}
