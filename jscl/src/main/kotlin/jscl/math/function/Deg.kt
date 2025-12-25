package jscl.math.function

import jscl.AngleUnit
import jscl.math.Generic
import jscl.math.Variable
import jscl.mathml.MathML

/**
 * User: serso
 * Date: 11/12/11
 * Time: 4:16 PM
 */
class Deg(generic: Generic?) : Algebraic("deg", if (generic == null) null else arrayOf(generic)) {

    override fun rootValue(): Root {
        throw UnsupportedOperationException("Root for deg() is not supported!")
    }

    override fun bodyToMathML(element: MathML, fenced: Boolean) {
        val child = element.element("deg")
        parameters!![0].toMathML(child, null)
        element.appendChild(child)
    }

    override fun selfExpand(): Generic {
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        return selfExpand()
    }

    override fun selfNumeric(): Generic {
        return AngleUnit.rad.transform(AngleUnit.deg, parameters!![0])
    }

    override fun derivative(n: Int): Generic {
        throw UnsupportedOperationException("Derivative for deg() is not supported!")
    }

    override fun newInstance(): Variable {
        return Deg(null)
    }

    override val constants: Set<Constant>
        get() {
            val result = HashSet<Constant>()
            for (parameter in parameters!!) {
                result.addAll(parameter.constants)
            }
            return result
        }
}
