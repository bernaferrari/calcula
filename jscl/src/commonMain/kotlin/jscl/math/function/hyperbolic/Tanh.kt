package jscl.math.function.hyperbolic

import jscl.math.function.Constant

import jscl.math.*
import jscl.math.function.Fraction
import jscl.math.function.Ln
import jscl.math.function.Trigonometric

open class Tanh(generic: Generic?) : Trigonometric("tanh", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    @Throws(NotIntegrableException::class)
    override fun antiDerivative(n: Int): Generic {
        return Ln(
            JsclInteger.valueOf(4).multiply(
                Cosh(parameters!![0]).selfExpand()
            )
        ).selfExpand()
    }

    override fun derivative(n: Int): Generic {
        return JsclInteger.valueOf(1).subtract(
            Tanh(parameters!![0]).selfExpand().pow(2)
        )
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Tanh(parameters!![0].negate()).selfExpand().negate()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(0)
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Fraction(
            Sinh(parameters!![0]).selfElementary(),
            Cosh(parameters!![0]).selfElementary()
        ).selfElementary()
    }

    override fun selfSimplify(): Generic {
        if (parameters!![0].signum() < 0) {
            return Tanh(parameters!![0].negate()).selfExpand().negate()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(0)
        }
        try {
            val v = parameters!![0].variableValue()
            if (v is Atanh) {
                val g = v.getParameters()
                return g!![0]
            }
        } catch (e: NotVariableException) {
        }
        return identity()
    }

    override fun identity(a: Generic, b: Generic): Generic {
        val ta = Tanh(a).selfSimplify()
        val tb = Tanh(b).selfSimplify()
        return Fraction(
            ta.add(tb),
            JsclInteger.valueOf(1).add(
                ta.multiply(tb)
            )
        ).selfSimplify()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).tanh()
    }

    override fun newInstance(): Variable {
        return Tanh(null)
    }
}
