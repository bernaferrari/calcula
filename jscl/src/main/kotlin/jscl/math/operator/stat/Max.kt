package jscl.math.operator.stat

import jscl.math.*
import jscl.math.operator.Operator

/**
 * User: serso
 * Date: 12/26/11
 * Time: 11:09 AM
 */
class Max : AbstractStatFunction {

    constructor(vector: JsclVector?) : this(vector?.let { arrayOf<Generic>(it) })

    private constructor(parameters: Array<Generic>?) : super(NAME, parameters)

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Max(parameters)
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
                        val candidate = elements[i].numeric()
                        if (result.subtract(candidate).signum() > 0) {
                            result = candidate
                        }
                    }

                    result
                }
            }
        } else {
            expressionValue()
        }
    }

    override fun newInstance(): Variable {
        return Max(null as JsclVector?)
    }

    companion object {
        const val NAME = "max"
    }
}
