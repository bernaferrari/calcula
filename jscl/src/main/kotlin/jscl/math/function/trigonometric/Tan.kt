package jscl.math.function.trigonometric

import jscl.math.function.Constant

import jscl.math.*
import jscl.math.function.Constants
import jscl.math.function.Fraction
import jscl.math.function.Ln
import jscl.math.function.Trigonometric

open class Tan(generic: Generic?) : Trigonometric("tan", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    @Throws(NotIntegrableException::class)
    override fun antiDerivative(n: Int): Generic {
        return Ln(
            JsclInteger.valueOf(4).multiply(
                Cos(parameters!![0]).selfExpand()
            )
        ).selfExpand().negate()
    }

    override fun derivative(n: Int): Generic {
        return JsclInteger.valueOf(1).add(
            Tan(parameters!![0]).selfExpand().pow(2)
        )
    }

    override fun selfExpand(): Generic {
        val result = trySimplify()
        return result ?: expressionValue()
    }

    override fun selfElementary(): Generic {
        return Fraction(
            Sin(parameters!![0]).selfElementary(),
            Cos(parameters!![0]).selfElementary()
        ).selfElementary()
    }

    override fun selfSimplify(): Generic {
        val result = trySimplify()

        if (result != null) {
            return result
        } else {
            try {
                val v = parameters!![0].variableValue()
                if (v is Atan) {
                    val g = v.getParameters()
                    return g!![0]
                }
            } catch (e: NotVariableException) {
                // ok
            }

            return identity()
        }
    }

    private fun trySimplify(): Generic? {
        var result: Generic? = null

        if (parameters!![0].signum() < 0) {
            result = Tan(parameters!![0].negate()).selfExpand().negate()
        } else if (parameters!![0].signum() == 0) {
            result = JsclInteger.valueOf(0)
        } else if (parameters!![0].compareTo(Constants.Generic.PI) == 0) {
            result = JsclInteger.valueOf(0)
        }

        return result
    }

    override fun identity(a: Generic, b: Generic): Generic {
        val ta = Tan(a).selfSimplify()
        val tb = Tan(b).selfSimplify()
        return Fraction(
            ta.add(tb),
            JsclInteger.valueOf(1).subtract(
                ta.multiply(tb)
            )
        ).selfSimplify()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).tan()
    }

    override fun newInstance(): Variable {
        return Tan(null)
    }
}
