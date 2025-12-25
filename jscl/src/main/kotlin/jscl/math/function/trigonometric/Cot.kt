package jscl.math.function.trigonometric

import jscl.math.function.Constant

import jscl.math.*
import jscl.math.function.Fraction
import jscl.math.function.Ln
import jscl.math.function.Trigonometric

open class Cot(generic: Generic?) : Trigonometric("cot", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    @Throws(NotIntegrableException::class)
    override fun antiDerivative(n: Int): Generic {
        return Ln(
            JsclInteger.valueOf(4).multiply(
                Sin(parameters!![0]).selfExpand()
            )
        ).selfExpand()
    }

    override fun derivative(n: Int): Generic {
        return JsclInteger.valueOf(1).add(
            Cot(parameters!![0]).selfExpand().pow(2)
        ).negate()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Cot(parameters!![0].negate()).selfExpand().negate()
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Fraction(
            Cos(parameters!![0]).selfElementary(),
            Sin(parameters!![0]).selfElementary()
        ).selfElementary()
    }

    override fun selfSimplify(): Generic {
        if (parameters!![0].signum() < 0) {
            return Cot(parameters!![0].negate()).selfExpand().negate()
        }
        try {
            val v = parameters!![0].variableValue()
            if (v is Acot) {
                val g = v.getParameters()
                return g!![0]
            }
        } catch (e: NotVariableException) {
        }
        return identity()
    }

    override fun identity(a: Generic, b: Generic): Generic {
        val ta = Cot(a).selfSimplify()
        val tb = Cot(b).selfSimplify()
        return Fraction(
            ta.multiply(tb).subtract(JsclInteger.valueOf(1)),
            ta.add(tb)
        ).selfSimplify()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).cot()
    }

    override fun newInstance(): Variable {
        return Cot(null)
    }
}
