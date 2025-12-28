package jscl.math.operator

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.Variable
import jscl.math.genericArrayOf

/**
 * User: serso
 * Date: 11/14/11
 * Time: 2:05 PM
 */
class Percent : PostfixFunction {

    constructor(content: Generic?, previousSumElement: Generic?) :
            super(NAME, genericArrayOf(content, previousSumElement))

    private constructor(parameters: Array<Generic>) :
            super(NAME, createParameters(getParameter(parameters, 0), getParameter(parameters, 1)))

    override fun getMinParameters(): Int = 1

    override fun getMaxParameters(): Int = 2

    override fun selfExpand(): Generic {
        return expressionValue()
    }

    override fun simplify(): Generic {
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        val percentValue = parameters!![0]
        val undefinedParam = JsclInteger.valueOf(Long.MIN_VALUE + 1)

        val normalizedPercentage = percentValue.divide(JsclInteger.valueOf(100))
        val previousSumElement = parameters!![1]
        if (undefinedParam != previousSumElement) {
            if (percentValue.signum() < 0 && previousSumElement.signum() < 0) {
                return normalizedPercentage
            }
            return previousSumElement.multiply(normalizedPercentage)
        }
        return normalizedPercentage
    }


    override fun newInstance(parameters: Array<Generic>): Operator {
        return Percent(parameters)
    }

    override fun newInstance(): Variable {
        return Percent(null, null)
    }

    companion object {
        const val NAME = "%"

        private fun createParameters(content: Generic?, previousSumElement: Generic?): Array<Generic> {
            val undefinedParam = JsclInteger.valueOf(Long.MIN_VALUE + 1)
            return if (previousSumElement == null) {
                arrayOf(content!!, undefinedParam)
            } else {
                arrayOf(content!!, previousSumElement)
            }
        }
    }
}
