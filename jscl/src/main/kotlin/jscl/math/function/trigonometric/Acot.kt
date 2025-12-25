package jscl.math.function.trigonometric

import jscl.math.function.Constant

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NumericWrapper
import jscl.math.Variable
import jscl.math.function.*

open class Acot(generic: Generic?) : ArcTrigonometric("acot", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    override fun derivative(n: Int): Generic {
        return Inverse(
            JsclInteger.valueOf(1).add(parameters!![0].pow(2))
        ).selfExpand().negate()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Constants.Generic.PI.subtract(Acot(parameters!![0].negate()).selfExpand())
        }

        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Constants.Generic.I.multiply(
            Ln(
                Root(
                    arrayOf(
                        Constants.Generic.I.add(parameters!![0]),
                        JsclInteger.valueOf(0),
                        Constants.Generic.I.subtract(parameters!![0])
                    ),
                    0
                ).selfElementary()
            ).selfElementary()
        )
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).acot()
    }

    override fun newInstance(): Variable {
        return Acot(null)
    }
}
