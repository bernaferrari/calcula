package org.solovyev.android.calculator

import jscl.JsclMathEngine
import jscl.common.math.MathRegistry
import jscl.math.function.IConstant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.solovyev.android.calculator.entities.Category
import org.solovyev.android.calculator.variables.CppVariable
import org.solovyev.android.calculator.variables.VariableCategory
import org.solovyev.android.calculator.entities.Entities

/**
 * Simple VariablesRegistry that wraps JsclMathEngine's constants registry.
 */
class AndroidVariablesRegistry(
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

    override fun getById(id: Int): IConstant? {
        return getEntities().find { it.getId() == id }
    }

    override fun init() {
        // Add default variables if not present
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
        // No-op for now - could be implemented with DataStore if needed
    }

    private fun addSafely(name: String) {
        if (!contains(name)) {
            val variable = CppVariable.builder(name).build().toJsclConstant()
            delegate.addOrUpdate(variable)
        }
    }
}
