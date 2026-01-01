package org.solovyev.android.calculator.variables

import jscl.math.function.IConstant
import kotlinx.serialization.Serializable
import org.solovyev.android.calculator.functions.CppFunction
import kotlin.jvm.JvmStatic

@Serializable
data class CppVariable(
    var id: Int = NO_ID,
    val name: String,
    val value: String = "",
    val description: String = "",
    val system: Boolean = false
) {

    fun toJsclConstant(): IConstant = JsclConstant(this)

    override fun toString(): String =
        if (id == NO_ID) "$name=$value"
        else "$name[#$id]=$value"

    class Builder private constructor(private var variable: CppVariable) {
        private var built = false

        constructor(name: String) : this(CppVariable(name = name))
        constructor(constant: IConstant) : this(CppVariable(
            id = if (constant.isIdDefined()) constant.getId() else NO_ID,
            name = constant.name,
            value = constant.getValue().orEmpty(),
            description = constant.getDescription().orEmpty(),
            system = constant.isSystem()
        ))

        fun withDescription(description: String) = apply {
            check(!built)
            variable = variable.copy(description = description)
        }

        fun withValue(value: String) = apply {
            check(!built)
            variable = variable.copy(value = value)
        }

        fun withValue(value: Double) = withValue(value.toString())

        fun withSystem(system: Boolean) = apply {
            check(!built)
            variable = variable.copy(system = system)
        }

        fun withId(id: Int) = apply {
            check(!built)
            variable.id = id
        }

        fun build(): CppVariable {
            built = true
            return variable
        }
    }

    companion object {
        const val NO_ID = CppFunction.NO_ID

        
        fun builder(name: String) = Builder(name)

        
        fun builder(name: String, value: Double) = Builder(name).withValue(value)

        
        fun builder(constant: IConstant) = Builder(constant)
    }
}
