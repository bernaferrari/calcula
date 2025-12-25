package jscl.math.operator.number

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.NotIntegerException
import jscl.math.Variable
import jscl.math.operator.Operator

class ModPow : Operator {

    constructor(integer: Generic?, exponent: Generic?, modulo: Generic?) :
            super(NAME, genericArrayOf(integer, exponent, modulo))

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 3

    override fun selfExpand(): Generic {
        try {
            val en = parameters!![0].integerValue()
            val exponent = parameters!![1].integerValue()
            val modulo = parameters!![2].integerValue()
            return en.modPow(exponent, modulo)
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return ModPow(parameters)
    }

    override fun newInstance(): Variable {
        return ModPow(null, null, null)
    }

    companion object {
        const val NAME = "modpow"
    }
}
