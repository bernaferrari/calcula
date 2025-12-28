package jscl.math.function

import jscl.math.Variable
import jscl.math.function.hyperbolic.*
import jscl.math.function.trigonometric.*
import org.solovyev.common.math.AbstractMathRegistry

/**
 * User: serso
 * Date: 10/29/11
 * Time: 12:54 PM
 */
class FunctionsRegistry : AbstractMathRegistry<Function>() {

    override fun get(name: String): Function? {
        val function = super.get(name)
        return if (function == null) null else copy(function)
    }

    override fun onInit() {
        add(Deg(null))
        add(Rad(null, null, null))
        add(Dms(null, null, null))

        add(Sin(null))
        add(Cos(null))
        add(Tan(null))
        add(Cot(null))

        add(Asin(null))
        add(Acos(null))
        add(Atan(null))
        add(Acot(null))

        add(Ln(null))
        add(Lg(null))
        add(Exp(null))
        add(Sqrt(null))
        add(SqrtAlias(null))
        add(Cubic(null))

        add(Sinh(null))
        add(Cosh(null))
        add(Tanh(null))
        add(Coth(null))

        add(Asinh(null))
        add(Acosh(null))
        add(Atanh(null))
        add(Acoth(null))

        add(Abs(null))
        add(Sgn(null))

        add(Conjugate(null))

        for (name in Comparison.names) {
            add(Comparison(name, null, null))
        }
    }

    companion object {
        private val instance = FunctionsRegistry()

        fun getInstance(): FunctionsRegistry {
            instance.init()
            return instance
        }

        fun lazyInstance(): FunctionsRegistry {
            return instance
        }

        fun <T : Variable> copy(variable: T): T {
            @Suppress("UNCHECKED_CAST")
            val result = variable.newInstance() as T
            if (variable.isIdDefined()) {
                result.setId(variable.getId())
            }
            result.setSystem(variable.isSystem())
            return result
        }
    }
}
