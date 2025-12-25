package jscl.math

import jscl.math.function.Constant
import jscl.mathml.MathML

abstract class GenericVariable(var content: Generic?) : Variable("") {

    override fun antiDerivative(variable: Variable): Generic {
        return content!!.antiDerivative(variable)
    }

    override fun derivative(variable: Variable): Generic {
        return content!!.derivative(variable)
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        val v = newInstance() as GenericVariable
        v.content = content!!.substitute(variable, generic)
        return if (v.isIdentity(variable)) generic else v.expressionValue()
    }

    override fun expand(): Generic {
        return content!!.expand()
    }

    override fun factorize(): Generic {
        val v = newInstance() as GenericVariable
        v.content = content!!.factorize()
        return v.expressionValue()
    }

    override fun elementary(): Generic {
        val v = newInstance() as GenericVariable
        v.content = content!!.elementary()
        return v.expressionValue()
    }

    override fun simplify(): Generic {
        val v = newInstance() as GenericVariable
        v.content = content!!.simplify()
        return v.expressionValue()
    }

    override fun numeric(): Generic {
        return content!!.numeric()
    }

    override fun isConstant(variable: Variable): Boolean {
        return content!!.isConstant(variable)
    }

    override fun compareTo(variable: Variable): Int {
        if (this === variable) return 0
        val c = comparator.compare(this, variable)
        return when {
            c < 0 -> -1
            c > 0 -> 1
            else -> {
                val v = variable as GenericVariable
                content!!.compareTo(v.content!!)
            }
        }
    }

    override fun toString(): String {
        return content.toString()
    }

    override fun toJava(): String {
        return content!!.toJava()
    }

    override fun toMathML(element: MathML, data: Any?) {
        content!!.toMathML(element, data)
    }

    override val constants: Set<Constant>
        get() = content!!.constants

    companion object {
        @JvmStatic
        fun content(generic: Generic): Generic {
            return content(generic, false)
        }

        @JvmStatic
        fun content(generic: Generic, expression: Boolean): Generic {
            var result = generic
            try {
                val v = result.variableValue()
                if (expression) {
                    if (v is ExpressionVariable) result = v.content!!
                } else {
                    if (v is GenericVariable) result = v.content!!
                }
            } catch (e: NotVariableException) {
                // ignore
            }
            return result
        }

        @JvmStatic
        @JvmOverloads
        fun valueOf(generic: Generic, integer: Boolean = false): GenericVariable {
            return if (integer) IntegerVariable(generic) else ExpressionVariable(generic)
        }
    }
}
