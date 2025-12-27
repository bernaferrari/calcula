package jscl.math.function

import jscl.math.Generic
import jscl.math.NotIntegrableException

abstract class ArcTrigonometric(name: String, parameter: Array<Generic>?) : Function(name, parameter) {

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun selfSimplify(): Generic {
        return selfExpand()
    }
}
