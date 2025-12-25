package org.solovyev.android.calculator

import jscl.math.function.IConstant

data class PreparedExpression(
    val value: String,
    val undefinedVariables: List<IConstant>
) : CharSequence {

    fun hasUndefinedVariables(): Boolean = undefinedVariables.isNotEmpty()

    override val length: Int get() = value.length

    override fun get(index: Int): Char = value[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        value.subSequence(startIndex, endIndex)

    override fun toString(): String = value
}
