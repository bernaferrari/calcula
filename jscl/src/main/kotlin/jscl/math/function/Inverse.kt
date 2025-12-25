package jscl.math.function

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NotDivisibleException
import jscl.math.Variable

class Inverse(generic: Generic?) : Fraction(JsclInteger.valueOf(1), generic) {

    override fun selfExpand(): Generic {
        try {
            val parameter = parameter()
            return JsclInteger.ONE.divide(parameter)
        } catch (e: NotDivisibleException) {
        }
        return expressionValue()
    }

    fun parameter(): Generic {
        return parameters!![1]
    }

    override fun newInstance(): Variable {
        return Inverse(null)
    }

    override val constants: Set<Constant>
        get() {
            val result = HashSet<Constant>()
            for (parameter in parameters!!) {
                result.addAll(parameter.constants)
            }
            return result
        }
}
