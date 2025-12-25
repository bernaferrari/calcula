package jscl.math.function

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NotIntegrableException
import jscl.math.Variable
import jscl.math.operator.AbstractFunction
import jscl.text.ParserUtils
import org.solovyev.common.math.MathEntity

abstract class Function protected constructor(name: String, parameters: Array<Generic>?) : AbstractFunction(name, parameters) {

    override fun getMinParameters(): Int {
        return 1
    }

    override fun copy(that: MathEntity) {
        super.copy(that)
        if (that is Function) {
            this.parameters = if (that.parameters != null) {
                ParserUtils.copyOf<Generic>(that.parameters!!)
            } else {
                null
            }
        }
    }

    override fun antiDerivative(variable: Variable): Generic {
        val parameter = getParameterForAntiDerivation(variable)

        return if (parameter < 0) {
            throw NotIntegrableException(this)
        } else {
            antiDerivative(parameter)
        }
    }

    protected fun getParameterForAntiDerivation(variable: Variable): Int {
        var result = -1

        for (i in parameters!!.indices) {
            if (result == -1 && parameters!![i].isIdentity(variable)) {
                // found!
                result = i
            } else if (!parameters!![i].isConstant(variable)) {
                result = -1
                break
            }
        }

        return result
    }

    abstract fun antiDerivative(n: Int): Generic

    override fun derivative(variable: Variable): Generic {
        return if (isIdentity(variable)) {
            JsclInteger.valueOf(1)
        } else {
            var result: Generic = JsclInteger.valueOf(0)

            for (i in parameters!!.indices) {
                // chain rule: f(x) = g(h(x)) => f'(x) = g'(h(x)) * h'(x)
                // hd = h'(x)
                // gd = g'(x)
                val hd = parameters!![i].derivative(variable)
                val gd = derivative(i)

                result = result.add(hd.multiply(gd))
            }

            result
        }
    }

    abstract fun derivative(n: Int): Generic

    override fun isConstant(variable: Variable): Boolean {
        var result = !isIdentity(variable)

        if (result) {
            for (parameter in parameters!!) {
                if (!parameter.isConstant(variable)) {
                    result = false
                    break
                }
            }
        }

        return result
    }
}
