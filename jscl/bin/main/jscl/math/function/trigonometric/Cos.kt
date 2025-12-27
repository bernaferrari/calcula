package jscl.math.function.trigonometric

import jscl.math.function.Constant

import jscl.math.*
import jscl.math.function.Constants
import jscl.math.function.Exp
import jscl.math.function.Trigonometric

open class Cos(generic: Generic?) : Trigonometric("cos", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    @Throws(NotIntegrableException::class)
    override fun antiDerivative(n: Int): Generic {
        return Sin(parameters!![0]).selfExpand()
    }

    override fun derivative(n: Int): Generic {
        return Sin(parameters!![0]).selfExpand().negate()
    }

    override fun selfExpand(): Generic {
        val result = trySimplify()
        return result ?: expressionValue()
    }

    private fun trySimplify(): Generic? {
        var result: Generic? = null

        if (parameters!![0].signum() < 0) {
            result = Cos(parameters!![0].negate()).selfExpand()
        } else if (parameters!![0].signum() == 0) {
            result = JsclInteger.valueOf(1)
        } else if (parameters!![0].compareTo(Constants.Generic.PI) == 0) {
            result = JsclInteger.valueOf(-1)
        }

        return result
    }

    override fun selfElementary(): Generic {
        return Exp(
            Constants.Generic.I.multiply(parameters!![0])
        ).selfElementary().add(
            Exp(
                Constants.Generic.I.multiply(parameters!![0].negate())
            ).selfElementary()
        ).multiply(Constants.Generic.HALF)
    }

    override fun selfSimplify(): Generic {
        val result = trySimplify()

        if (result != null) {
            return result
        } else {
            try {
                val v = parameters!![0].variableValue()
                if (v is Acos) {
                    val g = v.getParameters()
                    return g!![0]
                }
            } catch (e: NotVariableException) {
            }
            return identity()
        }
    }

    override fun identity(a: Generic, b: Generic): Generic {
        return Cos(a).selfSimplify().multiply(
            Cos(b).selfSimplify()
        ).subtract(
            Sin(a).selfSimplify().multiply(
                Sin(b).selfSimplify()
            )
        )
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).cos()
    }

    override fun newInstance(): Variable {
        return Cos(null)
    }
}
