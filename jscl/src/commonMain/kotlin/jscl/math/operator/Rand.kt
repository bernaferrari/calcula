package jscl.math.operator

import jscl.math.Generic
import jscl.math.NumericWrapper
import jscl.math.TimeDependent
import jscl.math.Variable
import jscl.math.numeric.Real

/**
 * User: serso
 * Date: 12/26/11
 * Time: 9:54 AM
 */
class Rand : Operator, TimeDependent {

    constructor() : super(NAME, emptyArray())

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Rand()
    }

    override fun getMinParameters(): Int = 0

    override fun selfExpand(): Generic {
        return NumericWrapper(Real.valueOf(kotlin.random.Random.nextDouble()))
    }

    override fun numeric(): Generic {
        return selfExpand().numeric()
    }

    override fun newInstance(): Variable {
        return Rand()
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun compareTo(other: Any): Int {
        if (this === other) return 0
        return -1
    }

    override fun compareTo(variable: Variable): Int {
        if (this === variable) return 0
        return -1
    }

    companion object {
        const val NAME = "rand"
    }
}
