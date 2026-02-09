package org.solovyev.android.calculator

import jscl.JsclMathEngine
import jscl.common.math.MathRegistry
import jscl.math.function.Function
import jscl.math.function.IFunction
import jscl.math.function.IConstant
import jscl.math.operator.Operator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.solovyev.android.calculator.entities.Category
import org.solovyev.android.calculator.entities.Entities
import org.solovyev.android.calculator.functions.FunctionCategory
import org.solovyev.android.calculator.operators.OperatorCategory
import org.solovyev.android.calculator.variables.CppVariable
import org.solovyev.android.calculator.variables.VariableCategory

class DefaultVariablesRegistry(
    private val mathEngine: JsclMathEngine
) : VariablesRegistry {

    private val delegate: MathRegistry<IConstant> = mathEngine.getConstantsRegistry()

    private val descriptions = mutableMapOf<String, String>()

    private val _addedEvents = MutableSharedFlow<VariablesRegistry.AddedEvent>()
    override val addedEvents: SharedFlow<VariablesRegistry.AddedEvent> = _addedEvents.asSharedFlow()

    private val _changedEvents = MutableSharedFlow<VariablesRegistry.ChangedEvent>()
    override val changedEvents: SharedFlow<VariablesRegistry.ChangedEvent> = _changedEvents.asSharedFlow()

    private val _removedEvents = MutableSharedFlow<VariablesRegistry.RemovedEvent>()
    override val removedEvents: SharedFlow<VariablesRegistry.RemovedEvent> = _removedEvents.asSharedFlow()

    override fun getEntities(): List<IConstant> = delegate.getEntities()

    override fun getSystemEntities(): List<IConstant> = delegate.getSystemEntities()

    override fun addOrUpdate(t: IConstant): IConstant {
        val existing = delegate.get(t.name)
        val result = delegate.addOrUpdate(t)
        if (existing == null) {
            _addedEvents.tryEmit(VariablesRegistry.AddedEvent(result))
        } else {
            _changedEvents.tryEmit(VariablesRegistry.ChangedEvent(existing, result))
        }
        return result
    }

    override fun remove(variable: IConstant) {
        delegate.remove(variable)
        _removedEvents.tryEmit(VariablesRegistry.RemovedEvent(variable))
    }

    override fun getNames(): List<String> = delegate.getNames()

    override fun contains(name: String): Boolean = delegate.contains(name)

    override fun get(name: String): IConstant? = delegate.get(name)

    override fun getById(id: Int): IConstant? = delegate.getById(id)

    override fun init() {
        addSafely("x")
        addSafely("y")
        addSafely("t")
        addSafely("j")
    }

    override fun getDescription(name: String): String? {
        val variable = get(name)
        return if (variable != null && !variable.isSystem()) {
            variable.getDescription()
        } else {
            descriptions[name]
        }
    }

    override fun getCategory(entity: IConstant): Category<*>? {
        return Entities.getCategory(entity, VariableCategory.entries.toTypedArray())
    }

    override fun save() {
        // No-op for now.
    }

    private fun addSafely(name: String) {
        if (!contains(name)) {
            val variable = CppVariable.builder(name).build().toJsclConstant()
            delegate.addOrUpdate(variable)
        }
    }
}

class DefaultFunctionsRegistry(
    private val mathEngine: JsclMathEngine
) : FunctionsRegistry {

    private val delegate: MathRegistry<Function> = mathEngine.getFunctionsRegistry()

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val events: SharedFlow<Unit> = _events.asSharedFlow()

    override fun init() {
        delegate.init()
    }

    override fun getEntities(): List<Function> = delegate.getEntities()

    override fun getSystemEntities(): List<Function> = delegate.getSystemEntities()

    override fun addOrUpdate(t: Function): Function {
        val result = delegate.addOrUpdate(t)
        _events.tryEmit(Unit)
        return result
    }

    override fun remove(variable: Function) {
        delegate.remove(variable)
        _events.tryEmit(Unit)
    }

    override fun getNames(): List<String> = delegate.getNames()

    override fun contains(name: String): Boolean = delegate.contains(name)

    override fun get(name: String): Function? = delegate.get(name)

    override fun getById(id: Int): Function? = delegate.getById(id)

    override fun getDescription(name: String): String? {
        val function = get(name) ?: return null
        return (function as? IFunction)?.getDescription()
    }

    override fun getCategory(entity: Function): Category<*>? {
        return Entities.getCategory(entity, FunctionCategory.values())
    }

    override fun save() {
        // No-op for now.
    }
}

class DefaultOperatorsRegistry(
    private val mathEngine: JsclMathEngine
) : OperatorsRegistry {

    private val delegate: MathRegistry<Operator> = mathEngine.getOperatorsRegistry()

    override fun init() {
        delegate.init()
    }

    override fun getEntities(): List<Operator> = delegate.getEntities()

    override fun getSystemEntities(): List<Operator> = delegate.getSystemEntities()

    override fun addOrUpdate(t: Operator): Operator = delegate.addOrUpdate(t)

    override fun remove(variable: Operator) {
        delegate.remove(variable)
    }

    override fun getNames(): List<String> = delegate.getNames()

    override fun contains(name: String): Boolean = delegate.contains(name)

    override fun get(name: String): Operator? = delegate.get(name)

    override fun getById(id: Int): Operator? = delegate.getById(id)

    override fun getDescription(name: String): String? = null

    override fun getCategory(entity: Operator): Category<*>? {
        return Entities.getCategory(entity, OperatorCategory.values())
    }

    override fun save() {
        // No-op for now.
    }
}

class DefaultPostfixFunctionsRegistry(
    private val mathEngine: JsclMathEngine
) : PostfixFunctionsRegistry {

    private val delegate: MathRegistry<Operator> = mathEngine.getPostfixFunctionsRegistry()

    override fun init() {
        delegate.init()
    }

    override fun getEntities(): List<Operator> = delegate.getEntities()

    override fun getSystemEntities(): List<Operator> = delegate.getSystemEntities()

    override fun addOrUpdate(t: Operator): Operator = delegate.addOrUpdate(t)

    override fun remove(variable: Operator) {
        delegate.remove(variable)
    }

    override fun getNames(): List<String> = delegate.getNames()

    override fun contains(name: String): Boolean = delegate.contains(name)

    override fun get(name: String): Operator? = delegate.get(name)

    override fun getById(id: Int): Operator? = delegate.getById(id)

    override fun getDescription(name: String): String? = null

    override fun getCategory(entity: Operator): Category<*>? {
        return Entities.getCategory(entity, OperatorCategory.values())
    }

    override fun save() {
        // No-op for now.
    }
}
