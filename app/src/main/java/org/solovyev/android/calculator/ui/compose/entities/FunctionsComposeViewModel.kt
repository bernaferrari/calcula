package org.solovyev.android.calculator.ui.compose.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jscl.math.function.Function
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.functions.FunctionCategory
import org.solovyev.android.calculator.functions.FunctionsRegistry
import org.solovyev.android.calculator.operators.OperatorCategory
import org.solovyev.android.calculator.operators.OperatorsRegistry
import org.solovyev.android.calculator.operators.PostfixFunctionsRegistry
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FunctionsComposeViewModel @Inject constructor(
    private val registry: FunctionsRegistry,
    private val operatorsRegistry: OperatorsRegistry,
    private val postfixFunctionsRegistry: PostfixFunctionsRegistry,
    private val keyboard: Keyboard
) : ViewModel() {

    private val _refreshTick = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = _refreshTick.asStateFlow()

    init {
        viewModelScope.launch {
            registry.events.collect {
                bump()
            }
        }
    }

    fun getFunctionCategories(): List<FunctionCategory> = FunctionCategory.values().toList()

    fun getOperatorCategories(): List<OperatorCategory> = OperatorCategory.values().toList()

    fun getFunctionsFor(category: FunctionCategory): List<Function> {
        return registry.getEntities().filter { category.isInCategory(it) }
    }

    fun getOperatorsFor(category: OperatorCategory): List<jscl.math.operator.Operator> {
        val operators = operatorsRegistry.getEntities()
        val postfix = postfixFunctionsRegistry.getEntities()
        return (operators + postfix).filter { category.isInCategory(it) }
    }

    fun getFunctionDescription(function: Function): String? = registry.getDescription(function.name)

    fun getOperatorDescription(operator: jscl.math.operator.Operator): String? {
        val name = operator.name
        return operatorsRegistry.getDescription(name) ?: postfixFunctionsRegistry.getDescription(name)
    }

    fun useName(name: String) {
        keyboard.buttonPressed(name)
    }

    fun remove(function: Function) {
        registry.remove(function)
    }

    private fun bump() {
        _refreshTick.value = _refreshTick.value + 1
    }
}
