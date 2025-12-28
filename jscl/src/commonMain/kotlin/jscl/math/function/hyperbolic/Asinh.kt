package jscl.math.function.hyperbolic

import jscl.math.function.Constant

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NumericWrapper
import jscl.math.Variable
import jscl.math.function.*

open class Asinh(generic: Generic?) : ArcTrigonometric("asinh", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    override fun derivative(n: Int): Generic {
        return Inverse(
            Sqrt(
                JsclInteger.valueOf(1).add(
                    parameters!![0].pow(2)
                )
            ).selfExpand()
        ).selfExpand()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Asinh(parameters!![0].negate()).selfExpand().negate()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(0)
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Ln(
            Root(
                arrayOf(
                    JsclInteger.valueOf(1),
                    JsclInteger.valueOf(2).multiply(parameters!![0]),
                    JsclInteger.valueOf(-1)
                ),
                0
            ).selfElementary()
        ).selfElementary()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).asinh()
    }

    override fun newInstance(): Variable {
        return Asinh(null)
    }
}
