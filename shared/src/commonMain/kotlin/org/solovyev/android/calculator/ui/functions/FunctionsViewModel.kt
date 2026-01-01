package org.solovyev.android.calculator.ui.functions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jscl.math.function.Function
import jscl.math.operator.Operator
import org.solovyev.android.calculator.*
import org.solovyev.android.calculator.functions.CppFunction
import org.solovyev.android.calculator.functions.FunctionCategory
import org.solovyev.android.calculator.operators.OperatorCategory
import org.solovyev.android.calculator.math.MathType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FunctionsViewModel(
    private val functionsRegistry: FunctionsRegistry,
    private val operatorsRegistry: OperatorsRegistry,
    private val postfixFunctionsRegistry: PostfixFunctionsRegistry,
    private val editor: Editor,
    private val engine: Engine
) : ViewModel() {

    private val _refreshTick = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = _refreshTick.asStateFlow()

    init {
        viewModelScope.launch { functionsRegistry.events.collect { bump() } }
    }

    private fun bump() {
        _refreshTick.value++
    }

    fun getFunctionCategories(): List<FunctionCategory> = FunctionCategory.values().toList()
    fun getOperatorCategories(): List<OperatorCategory> = OperatorCategory.values().toList()

    fun getFunctionsFor(category: FunctionCategory): List<Function> {
        return functionsRegistry.getEntities()
            .filter { category.isInCategory(it) }
            .sortedBy { it.name }
    }

    fun getOperatorsFor(category: OperatorCategory): List<Operator> {
        val operators = (operatorsRegistry.getEntities() + postfixFunctionsRegistry.getEntities()) as List<Operator>
        return operators
            .filter { category.isInCategory(it) }
            .sortedBy { it.name }
    }

    fun getFunctionDescription(function: Function): String? = functionsRegistry.getDescription(function.name)

    fun getOperatorDescription(operator: Operator): String? {
        val name = operator.name
        return operatorsRegistry.getDescription(name) ?: postfixFunctionsRegistry.getDescription(name)
    }

    fun useName(name: String) {
        val type = MathType.getType(name, 0, false, engine).type
        if (type == MathType.function || type == MathType.operator) {
             editor.insert("$name()", -1)
        } else {
             editor.insert(name)
        }
    }

    fun add(name: String, body: String, parameters: List<String>, description: String) {
        val function = CppFunction.builder(name, body)
            .withParameters(parameters)
            .withDescription(description)
            .build()
            .toJsclBuilder()
            .create()
        functionsRegistry.addOrUpdate(function)
    }

    fun remove(function: Function) {
        functionsRegistry.remove(function)
    }
}
