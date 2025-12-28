package jscl.math.function.hyperbolic

import jscl.math.*
import jscl.math.function.Constant
import jscl.math.function.Constants
import jscl.math.function.Exp
import jscl.math.function.Trigonometric

open class Cosh(generic: Generic?) : Trigonometric("cosh", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    @Throws(NotIntegrableException::class)
    override fun antiDerivative(n: Int): Generic {
        return Sinh(parameters!![0]).selfExpand()
    }

    override fun derivative(n: Int): Generic {
        return Sinh(parameters!![0]).selfExpand()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Cosh(parameters!![0].negate()).selfExpand()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(1)
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Exp(
            parameters!![0]
        ).selfElementary().add(
            Exp(
                parameters!![0].negate()
            ).selfElementary()
        ).multiply(Constants.Generic.HALF)
    }

    override fun selfSimplify(): Generic {
        if (parameters!![0].signum() < 0) {
            return Cosh(parameters!![0].negate()).selfExpand()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(1)
        }
        try {
            val v = parameters!![0].variableValue()
            if (v is Acosh) {
                val g = v.getParameters()
                return g!![0]
            }
        } catch (e: NotVariableException) {
        }
        return identity()
    }

    override fun identity(a: Generic, b: Generic): Generic {
        return Cosh(a).selfSimplify().multiply(
            Cosh(b).selfSimplify()
        ).add(
            Sinh(a).selfSimplify().multiply(
                Sinh(b).selfSimplify()
            )
        )
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).cosh()
    }

    override fun newInstance(): Variable {
        return Cosh(null)
    }
}
