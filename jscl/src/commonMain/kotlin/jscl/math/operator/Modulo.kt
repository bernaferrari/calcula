package jscl.math.operator

import jscl.math.*
import jscl.math.numeric.Real

class Modulo : Operator {

    constructor(first: Generic?, second: Generic?) : super(NAME, genericArrayOf(first, second))

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        try {
            return tryIntegerMod()
        } catch (e: NotIntegerException) {
        }
        return tryRealMod()
    }

    private fun tryRealMod(): Generic {
        val numerator = numericDoubleOrNull(parameters!![0]) ?: return expressionValue()
        val denominator = numericDoubleOrNull(parameters!![1]) ?: return expressionValue()
        return NumericWrapper(Real.valueOf(numerator % denominator))
    }

    @Throws(NotIntegerException::class)
    private fun tryIntegerMod(): Generic {
        val numerator = parameters!![0].integerValue()
        val denominator = parameters!![1].integerValue()
        return numerator.mod(denominator)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Modulo(parameters)
    }

    override fun numeric(): Generic {
        return newNumericFunction().selfNumeric()
    }

    override fun selfNumeric(): Generic {
        return selfExpand()
    }

    override fun newInstance(): Variable {
        return Modulo(null, null)
    }

    companion object {
        const val NAME = "mod"
    }

    private fun numericDoubleOrNull(generic: Generic): Double? {
        val numeric = try {
            generic.numeric()
        } catch (_: Exception) {
            return null
        }
        val wrapper = numeric as? NumericWrapper ?: return null
        val content = wrapper.content()
        return if (content is Real) {
            content.doubleValue()
        } else {
            null
        }
    }
}
