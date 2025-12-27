package jscl.math.function.trigonometric

import jscl.math.function.Constant

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NumericWrapper
import jscl.math.Variable
import jscl.math.function.*

open class Atan(generic: Generic?) : ArcTrigonometric("atan", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    override fun derivative(n: Int): Generic {
        return Inverse(
            JsclInteger.valueOf(1).add(parameters!![0].pow(2))
        ).selfExpand()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Atan(parameters!![0].negate()).selfExpand().negate()
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
                        JsclInteger.valueOf(-1).add(Constants.Generic.I.multiply(parameters!![0])),
                        JsclInteger.valueOf(0),
                        JsclInteger.valueOf(1).add(Constants.Generic.I.multiply(parameters!![0]))
                    ),
                    0
                ).selfElementary()
            ).selfElementary()
        )
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).atan()
    }

    override fun newInstance(): Variable {
        return Atan(null)
    }
}
