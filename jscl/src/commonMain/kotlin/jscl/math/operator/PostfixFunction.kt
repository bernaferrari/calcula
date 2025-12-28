package jscl.math.operator

import jscl.math.Generic
import jscl.math.Variable

/**
 * User: serso
 * Date: 11/2/11
 * Time: 11:07 AM
 */
abstract class PostfixFunction(name: String, parameter: Array<Generic>?) :
    Operator(name, parameter) {

    override fun toString(): String {
        return formatParameter(0) + name
    }

    final override fun numeric(): Generic {
        val result = newInstance() as AbstractFunction
        val params = parameters!!
        if (result.parameters == null) {
            @Suppress("UNCHECKED_CAST")
            result.parameters = arrayOfNulls<Generic>(params.size) as Array<Generic>
        }
        for (i in params.indices) {
            result.parameters!![i] = params[i].numeric()
        }
        return result.selfNumeric()
    }

    abstract override fun selfNumeric(): Generic

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
