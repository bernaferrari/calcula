package jscl.math.operator.number

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.NotIntegerException
import jscl.math.Variable
import jscl.math.operator.Operator

class ModInverse : Operator {

    constructor(integer: Generic?, modulo: Generic?) : super(NAME, genericArrayOf(integer, modulo))

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        try {
            val en = parameters!![0].integerValue()
            val modulo = parameters!![1].integerValue()
            return en.modInverse(modulo)
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return ModInverse(parameters)
    }

    override fun newInstance(): Variable {
        return ModInverse(null, null)
    }

    companion object {
        const val NAME = "modinv"
    }
}
