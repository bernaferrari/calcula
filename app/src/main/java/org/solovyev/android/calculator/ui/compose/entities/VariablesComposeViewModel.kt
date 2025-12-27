package org.solovyev.android.calculator.ui.compose.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jscl.math.function.IConstant
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.VariablesRegistry
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.variables.VariableCategory
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class VariablesComposeViewModel @Inject constructor(
    private val registry: VariablesRegistry,
    private val keyboard: Keyboard
) : ViewModel() {

    private val _refreshTick = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = _refreshTick.asStateFlow()

    init {
        viewModelScope.launch {
            registry.addedEvents.collect { bump() }
        }
        viewModelScope.launch {
            registry.changedEvents.collect { bump() }
        }
        viewModelScope.launch {
            registry.removedEvents.collect { bump() }
        }
    }

    fun getCategories(): List<VariableCategory> = VariableCategory.values().toList()

    fun getVariablesFor(category: VariableCategory): List<IConstant> {
        return registry.getEntities()
            .filterNot { it.name == MathType.INFINITY_JSCL || it.name == MathType.NAN }
            .filter { category.isInCategory(it) }
    }

    fun getDescription(variable: IConstant): String? = registry.getDescription(variable.name)

    fun getDisplayName(variable: IConstant): String {
        if (!variable.isDefined()) return variable.name
        val value = variable.getValue()
        return if (value.isNullOrEmpty()) variable.name else "${variable.name} = $value"
    }

    fun useName(name: String) {
        keyboard.buttonPressed(name)
    }

    fun remove(variable: IConstant) {
        registry.remove(variable)
    }

    private fun bump() {
        _refreshTick.value = _refreshTick.value + 1
    }
}
