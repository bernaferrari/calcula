package jscl.math.operator

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.Generic
import jscl.math.Variable
import jscl.text.ParserUtils

/**
 * User: serso
 * Date: 10/31/11
 * Time: 10:58 PM
 */
class Degree : PostfixFunction {

    constructor(expression: Generic?) : super(NAME, expression?.let { arrayOf<Generic>(it) })

    private constructor(parameter: Array<Generic>) : super(NAME, ParserUtils.copyOf<Generic>(parameter, 1))

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return AngleUnit.deg.transform(JsclMathEngine.getInstance().angleUnits, parameters!![0])
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Degree(parameters)
    }

    override fun newInstance(): Variable {
        return Degree(null as Generic?)
    }

    companion object {
        const val NAME = "°"
    }
}
