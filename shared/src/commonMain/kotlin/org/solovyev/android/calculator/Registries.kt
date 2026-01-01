package org.solovyev.android.calculator

import jscl.math.function.Function
import jscl.math.function.IConstant
import jscl.math.operator.Operator
import kotlinx.coroutines.flow.SharedFlow

interface VariablesRegistry : EntitiesRegistry<IConstant> {
    val addedEvents: SharedFlow<AddedEvent>
    val changedEvents: SharedFlow<ChangedEvent>
    val removedEvents: SharedFlow<RemovedEvent>

    data class AddedEvent(val variable: IConstant)
    data class ChangedEvent(val oldVariable: IConstant, val newVariable: IConstant)
    data class RemovedEvent(val variable: IConstant)
}

interface FunctionsRegistry : EntitiesRegistry<Function> {
    val events: SharedFlow<Unit>
}

interface OperatorsRegistry : EntitiesRegistry<Operator>
interface PostfixFunctionsRegistry : EntitiesRegistry<Function>
