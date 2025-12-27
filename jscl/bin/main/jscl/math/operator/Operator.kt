package jscl.math.operator

import jscl.math.*

abstract class Operator protected constructor(name: String, parameters: Array<Generic>?) :
    AbstractFunction(name, parameters) {

    override fun antiDerivative(variable: Variable): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(variable: Variable): Generic {
        return if (isIdentity(variable)) {
            JsclInteger.valueOf(1)
        } else {
            JsclInteger.valueOf(0)
        }
    }

    override fun selfElementary(): Generic {
        return expressionValue()
    }

    override fun selfSimplify(): Generic {
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return numeric()
    }

    override fun numeric(): Generic {
        throw ArithmeticException()
    }

    override fun isConstant(variable: Variable): Boolean {
        return !isIdentity(variable)
    }

    abstract fun newInstance(parameters: Array<Generic>): Operator

    companion object {
        @JvmStatic
        @Throws(NotVariableException::class)
        protected fun toVariables(vector: Generic): Array<Variable> {
            return toVariables(vector as JsclVector)
        }

        @JvmStatic
        @Throws(NotVariableException::class)
        protected fun toVariables(vector: JsclVector): Array<Variable> {
            val element = vector.elements()
            val variable = Array(element.size) { i ->
                element[i].variableValue()
            }
            return variable
        }
    }
}
