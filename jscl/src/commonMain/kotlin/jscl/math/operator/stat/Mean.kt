package jscl.math.operator.stat

import jscl.math.*
import jscl.math.operator.Operator

/**
 * User: serso
 * Date: 12/26/11
 * Time: 11:09 AM
 */
class Mean : AbstractStatFunction {

    constructor(vector: JsclVector?) : this(vector?.let { arrayOf<Generic>(it) })

    private constructor(parameters: Array<Generic>?) : super(NAME, parameters)

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Mean(parameters)
    }

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return if (parameters!![0] is JsclVector) {
            val vector = parameters!![0] as JsclVector
            val elements = vector.elements()

            when {
                elements.isEmpty() -> NumericWrapper(JsclInteger.ZERO)
                elements.size == 1 -> elements[0]
                else -> {
                    var result = elements[0].numeric()
                    for (i in 1 until elements.size) {
                        result = result.add(elements[i].numeric())
                    }
                    result.divide(JsclInteger.valueOf(elements.size.toLong()).numeric())
                }
            }
        } else {
            expressionValue()
        }
    }

    override fun newInstance(): Variable {
        return Mean(null as JsclVector?)
    }

    companion object {
        const val NAME = "mean"
    }
}
