package jscl.math.operator.matrix

import jscl.math.Generic
import jscl.math.function.FunctionsRegistry
import jscl.math.operator.*
import org.solovyev.common.math.AbstractMathRegistry

/**
 * User: serso
 * Date: 11/17/11
 * Time: 10:22 AM
 */
class OperatorsRegistry private constructor() : AbstractMathRegistry<Operator>() {

    operator fun get(name: String, parameters: Array<Generic>): Operator? {
        val operator = super.get(name) ?: return null

        return if (operator.getMinParameters() <= parameters.size && operator.getMaxParameters() >= parameters.size) {
            operator.newInstance(parameters)
        } else {
            null
        }
    }

    override fun get(name: String): Operator? {
        val operator = super.get(name)
        return if (operator == null) null else FunctionsRegistry.copy(operator)
    }

    override fun onInit() {
        add(Derivative(null, null, null, null))
        add(Sum(null, null, null, null))
        add(Product(null, null, null, null))
        add(Modulo(null, null))
        add(Integral(null, null, null, null))
        add(IndefiniteIntegral(null, null))
    }

    companion object {
        private val instance = OperatorsRegistry()

        fun getInstance(): OperatorsRegistry {
            instance.init()
            return instance
        }

        fun lazyInstance(): OperatorsRegistry {
            return instance
        }
    }
}
