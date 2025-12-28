package jscl.math.operator.number

import jscl.math.*
import jscl.math.operator.Operator

class PrimitiveRoots : Operator {

    constructor(integer: Generic?) : super(NAME, integer?.let { arrayOf(it) })

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        try {
            val en = parameters!![0].integerValue()
            val a = en.primitiveRoots()
            return JsclVector(if (a.isNotEmpty()) a.map { it as Generic }.toTypedArray() else arrayOf<Generic>(JsclInteger.valueOf(0)))
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return PrimitiveRoots(parameters)
    }

    override fun newInstance(): Variable {
        return PrimitiveRoots(null as Generic?)
    }

    companion object {
        const val NAME = "primitiveroots"
    }
}
