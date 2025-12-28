package jscl.math.operator

import jscl.math.Generic
import jscl.math.Variable
import jscl.math.genericArrayOf

class Division : Operator {

    constructor(expression1: Generic?, expression2: Generic?) :
            super(NAME, genericArrayOf(expression1, expression2))

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        return parameters!![0].divideAndRemainder(parameters!![1])[0]
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Division(parameters)
    }

    override fun newInstance(): Variable {
        return Division(null, null)
    }

    companion object {
        const val NAME = "div"
    }
}
