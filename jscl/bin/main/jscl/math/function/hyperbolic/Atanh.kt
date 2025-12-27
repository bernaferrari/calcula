package jscl.math.function.hyperbolic

import jscl.math.function.Constant

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NumericWrapper
import jscl.math.Variable
import jscl.math.function.ArcTrigonometric
import jscl.math.function.Inverse
import jscl.math.function.Ln
import jscl.math.function.Root

open class Atanh(generic: Generic?) : ArcTrigonometric("atanh", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    override fun derivative(n: Int): Generic {
        return Inverse(
            JsclInteger.valueOf(1).subtract(
                parameters!![0].pow(2)
            )
        ).selfExpand()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Atanh(parameters!![0].negate()).selfExpand().negate()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(0)
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Ln(
            Root(
                arrayOf(
                    JsclInteger.valueOf(1).add(parameters!![0]),
                    JsclInteger.valueOf(0),
                    JsclInteger.valueOf(-1).add(parameters!![0])
                ),
                0
            ).selfElementary()
        ).selfElementary()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).atanh()
    }

    override fun newInstance(): Variable {
        return Atanh(null)
    }
}
