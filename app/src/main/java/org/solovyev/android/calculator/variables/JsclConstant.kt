package org.solovyev.android.calculator.variables

import jscl.math.function.Constant
import jscl.math.function.IConstant
import org.solovyev.common.math.MathEntity

internal class JsclConstant(private var variable: CppVariable) : IConstant {

    private var doubleValue: Double? = null
    private var constant: Constant? = null

    override val name: String
        get() = variable.name

    override fun getConstant(): Constant =
        constant ?: Constant(variable.name).also { constant = it }

    override fun getDescription(): String? = variable.description

    override fun isDefined(): Boolean = variable.value.isNotEmpty()

    override fun getValue(): String = variable.value

    override fun getDoubleValue(): Double? {
        doubleValue?.let { return it }
        if (variable.value.isNotEmpty()) {
            try {
                doubleValue = variable.value.toDouble()
            } catch (e: NumberFormatException) {
                // string is not a double
            }
        }
        return doubleValue
    }

    override fun toJava(): String = variable.value

    override fun isSystem(): Boolean = variable.system

    override fun getId(): Int = if (variable.id == CppVariable.NO_ID) 0 else variable.id

    override fun setId(id: Int) {
        variable.id = id
    }

    override fun isIdDefined(): Boolean = variable.id != CppVariable.NO_ID

    override fun copy(that: MathEntity) {
        require(that is IConstant) { "Trying to make a copy of unsupported type: ${that::class.java}" }

        variable = variable.copy(
            name = that.name,
            value = that.getValue().orEmpty(),
            description = that.getDescription().orEmpty(),
            system = that.isSystem(),
            id = if (that.isIdDefined()) that.getId() else CppVariable.NO_ID
        )
        doubleValue = null
        constant = null
    }
}
