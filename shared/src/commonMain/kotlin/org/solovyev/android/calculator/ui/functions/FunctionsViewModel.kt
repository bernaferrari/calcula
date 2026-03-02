package org.solovyev.android.calculator.ui.functions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jscl.math.function.Function
import jscl.math.operator.Operator
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.FunctionsRegistry
import org.solovyev.android.calculator.OperatorsRegistry
import org.solovyev.android.calculator.PostfixFunctionsRegistry
import org.solovyev.android.calculator.functions.CppFunction
import org.solovyev.android.calculator.functions.FunctionCategory
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.operators.OperatorCategory
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

    private val refreshTickState = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = refreshTickState.asStateFlow()

    init {
        viewModelScope.launch { functionsRegistry.events.collect { incrementRefreshTick() } }
    }

    private fun incrementRefreshTick() {
        refreshTickState.value++
    }

    fun getFunctionCategories(): List<FunctionCategory> = FunctionCategory.values().toList()
    fun getOperatorCategories(): List<OperatorCategory> = OperatorCategory.values().toList()

    fun getFunctionsFor(category: FunctionCategory): List<Function> {
        return functionsRegistry.getEntities()
            .filter { category.isInCategory(it) }
            .sortedBy { it.name }
    }

    fun getOperatorsFor(category: OperatorCategory): List<Operator> {
        val operators = operatorsRegistry.getEntities() + postfixFunctionsRegistry.getEntities()
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
        val tokenType = MathType.getType(name, 0, false, engine).type
        if (tokenType == MathType.function || tokenType == MathType.operator) {
            editor.insert("$name()", -1)
        } else {
            editor.insert(name)
        }
    }

    fun add(name: String, body: String, parameters: List<String>, description: String) {
        save(null, name, body, parameters, description)
    }

    fun save(
        original: Function?,
        name: String,
        body: String,
        parameters: List<String>,
        description: String
    ): String? {
        val normalizedName = name.trim()
        val normalizedBody = body.trim()
        val normalizedDescription = description.trim()
        if (!Engine.isValidName(normalizedName)) {
            return "Name contains invalid characters"
        }

        val existing = functionsRegistry.get(normalizedName)
        val originalId = original?.takeIf { it.isIdDefined() }?.getId()
        if (existing != null) {
            if (originalId == null || !existing.isIdDefined() || existing.getId() != originalId) {
                return "Function already exists"
            }
        }

        val builder = CppFunction.builder(normalizedName, normalizedBody)
            .withParameters(parameters)
            .withDescription(normalizedDescription)

        if (originalId != null) {
            builder.withId(originalId)
        }

        return runCatching {
            val oldById = originalId?.let { functionsRegistry.getById(it) }
            if (oldById != null && oldById.name != normalizedName) {
                functionsRegistry.remove(oldById)
            }
            functionsRegistry.addOrUpdate(builder.build().toJsclBuilder().create())
        }.exceptionOrNull()?.message
    }

    fun update(
        original: Function,
        name: String,
        body: String,
        parameters: List<String>,
        description: String
    ): String? {
        return save(original, name, body, parameters, description)
    }

    fun remove(function: Function) {
        functionsRegistry.remove(function)
    }

}
