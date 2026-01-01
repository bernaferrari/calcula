package jscl

import jscl.math.function.Function
import jscl.math.function.IConstant
import jscl.math.operator.Operator
import jscl.common.math.MathRegistry
import com.ionspin.kotlin.bignum.integer.BigInteger

interface MathContext {

    fun getFunctionsRegistry(): MathRegistry<Function>

    fun getOperatorsRegistry(): MathRegistry<Operator>

    fun getConstantsRegistry(): MathRegistry<IConstant>

    fun getPostfixFunctionsRegistry(): MathRegistry<Operator>

    fun getAngleUnits(): AngleUnit

    fun setAngleUnits(defaultAngleUnits: AngleUnit)

    fun getNumeralBase(): NumeralBase

    // OUTPUT NUMBER FORMATTING
    // todo serso: maybe gather all formatting data in one object?

    fun setNumeralBase(numeralBase: NumeralBase)

    fun setPrecision(precision: Int)

    fun setGroupingSeparator(separator: Char)

    fun format(value: Double): String

    fun format(value: BigInteger): String

    fun format(value: Double, nb: NumeralBase): String

    fun format(value: String, nb: NumeralBase): String

    fun setNotation(notation: Int)
}
