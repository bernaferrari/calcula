package jscl.math.function.trigonometric

import jscl.math.function.Constant

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NumericWrapper
import jscl.math.Variable
import jscl.math.function.*

open class Asin(generic: Generic?) : ArcTrigonometric("asin", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    override fun derivative(n: Int): Generic {
        return Inverse(
            Sqrt(
                JsclInteger.valueOf(1).subtract(parameters!![0].pow(2))
            ).selfExpand()
        ).selfExpand()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Asin(parameters!![0].negate()).selfExpand().negate()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(0)
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Constants.Generic.I.multiply(
            Ln(
                Root(
                    arrayOf(
                        JsclInteger.valueOf(-1),
                        JsclInteger.valueOf(2).multiply(Constants.Generic.I.multiply(parameters!![0])),
                        JsclInteger.valueOf(1)
                    ),
                    0
                ).selfElementary()
            ).selfElementary()
        )
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).asin()
    }

    override fun newInstance(): Variable {
        return Asin(null)
    }
}
