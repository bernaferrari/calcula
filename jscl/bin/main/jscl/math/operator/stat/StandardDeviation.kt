package jscl.math.operator.stat

import jscl.math.*
import jscl.math.function.Sqrt
import jscl.math.operator.Operator

/**
 * User: serso
 * Date: 12/26/11
 * Time: 11:09 AM
 */
class StandardDeviation : AbstractStatFunction {

    constructor(vector: JsclVector?) : this(vector?.let { arrayOf<Generic>(it) })

    private constructor(parameters: Array<Generic>?) : super(NAME, parameters)

    override fun newInstance(parameters: Array<Generic>): Operator {
        return StandardDeviation(parameters)
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
                elements.size == 1 -> NumericWrapper(JsclInteger.ZERO)
                else -> {
                    val mean = Mean(vector).numeric()

                    var result: Generic = NumericWrapper(JsclInteger.ZERO)
                    for (i in elements.indices) {
                        result = result.add(elements[i].numeric().subtract(mean).pow(2))
                    }
                    Sqrt(
                        result.divide(
                            JsclInteger.valueOf(elements.size.toLong()).numeric()
                                .subtract(JsclInteger.ONE.negate().numeric())
                        )
                    ).numeric()
                }
            }
        } else {
            expressionValue()
        }
    }

    override fun newInstance(): Variable {
        return StandardDeviation(null as JsclVector?)
    }

    companion object {
        const val NAME = "st_dev"
    }
}
