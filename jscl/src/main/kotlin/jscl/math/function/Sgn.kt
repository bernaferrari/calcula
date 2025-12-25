package jscl.math.function

import jscl.math.*

class Sgn(generic: Generic?) : Function("sgn", if (generic != null) arrayOf(generic) else null) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun antiDerivative(n: Int): Generic {
        return Abs(parameters!![0]).selfExpand()
    }

    override fun derivative(n: Int): Generic {
        return JsclInteger.valueOf(0)
    }

    override fun selfExpand(): Generic {
        val result = selfEvaluate()
        return result ?: expressionValue()
    }

    private fun selfEvaluate(): Generic? {
        var result: Generic? = null

        if (parameters!![0].signum() < 0) {
            result = Sgn(parameters!![0].negate()).selfExpand().negate()
        } else if (parameters!![0].signum() == 0) {
            result = JsclInteger.valueOf(0)
        }

        if (result == null) {
            try {
                val intValue = parameters!![0].integerValue()
                result = JsclInteger.valueOf(intValue.signum().toLong())
            } catch (e: NotIntegerException) {
            }
        }

        return result
    }

    override fun selfElementary(): Generic {
        return Fraction(parameters!![0], Abs(parameters!![0]).selfElementary()).selfElementary()
    }

    override fun selfSimplify(): Generic {
        val result = selfEvaluate()

        if (result == null) {
            try {
                val v = parameters!![0].variableValue()
                if (v is Abs) {
                    return JsclInteger.valueOf(1)
                } else if (v is Sgn) {
                    return v.selfSimplify()
                }
            } catch (e: NotVariableException) {
            }

            return expressionValue()
        } else {
            return result
        }
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).sgn()
    }

    override fun toJava(): String {
        val result = StringBuilder()
        result.append(parameters!![0].toJava())
        result.append(".sgn()")
        return result.toString()
    }

    override fun newInstance(): Variable {
        return Sgn(null)
    }
}
