package jscl.math.function.trigonometric

import jscl.math.function.Constant

import jscl.math.*
import jscl.math.function.Constants
import jscl.math.function.Exp
import jscl.math.function.Trigonometric
import jscl.math.function.Constants.Generic.HALF
import jscl.math.function.Constants.Generic.I

open class Sin(generic: Generic?) : Trigonometric("sin", generic?.let { arrayOf(it) }) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()


    @Throws(NotIntegrableException::class)
    override fun antiDerivative(n: Int): Generic {
        return Cos(parameters!![0]).selfExpand().negate()
    }

    override fun derivative(n: Int): Generic {
        return Cos(parameters!![0]).selfExpand()
    }

    override fun selfExpand(): Generic {
        val result = trySimplify()
        return result ?: expressionValue()
    }

    override fun selfElementary(): Generic {
        val power = I.multiply(parameters!![0])
        val e = Exp(power).selfElementary()
            .subtract(Exp(I.multiply(parameters!![0].negate())).selfElementary())
            .multiply(I.negate().multiply(HALF))
        return e
    }

    override fun selfSimplify(): Generic {
        val result = trySimplify()

        if (result != null) {
            return result
        } else {
            try {
                val v = parameters!![0].variableValue()
                if (v is Asin) {
                    val g = v.getParameters()
                    return g!![0]
                }
            } catch (e: NotVariableException) {
            }
            return identity()
        }
    }

    private fun trySimplify(): Generic? {
        var result: Generic? = null

        if (parameters!![0].signum() < 0) {
            result = Sin(parameters!![0].negate()).selfExpand().negate()
        } else if (parameters!![0].signum() == 0) {
            result = JsclInteger.valueOf(0)
        } else if (parameters!![0].compareTo(Constants.Generic.PI) == 0) {
            result = JsclInteger.valueOf(0)
        }

        return result
    }

    override fun identity(a: Generic, b: Generic): Generic {
        return Cos(b).selfSimplify().multiply(
            Sin(a).selfSimplify()
        ).add(
            Cos(a).selfSimplify().multiply(
                Sin(b).selfSimplify()
            )
        )
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).sin()
    }

    override fun newInstance(): Variable {
        return Sin(null)
    }
}
