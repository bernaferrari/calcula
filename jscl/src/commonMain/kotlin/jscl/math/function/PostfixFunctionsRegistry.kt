package jscl.math.function

import jscl.math.Generic
import jscl.math.operator.Degree
import jscl.math.operator.DoubleFactorial
import jscl.math.operator.Factorial
import jscl.math.operator.Operator
import jscl.math.operator.Percent
import org.solovyev.common.math.AbstractMathRegistry

/**
 * User: serso
 * Date: 10/31/11
 * Time: 10:56 PM
 */
class PostfixFunctionsRegistry : AbstractMathRegistry<Operator>() {

    operator fun get(name: String, parameters: Array<Generic>): Operator? {
        val operator = super.get(name)
        return operator?.newInstance(parameters)
    }

    override fun get(name: String): Operator? {
        val operator = super.get(name)
        return if (operator == null) null else FunctionsRegistry.copy(operator)
    }

    override fun onInit() {
        add(DoubleFactorial(null))
        add(Factorial(null))
        add(Degree(null))
        add(Percent(null, null))
    }

    companion object {
        private val instance = PostfixFunctionsRegistry()

        fun getInstance(): PostfixFunctionsRegistry {
            instance.init()
            return instance
        }

        fun lazyInstance(): PostfixFunctionsRegistry {
            return instance
        }
    }
}
