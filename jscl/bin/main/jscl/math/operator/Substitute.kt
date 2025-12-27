package jscl.math.operator

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.GenericVariable
import jscl.math.JsclVector
import jscl.math.Variable

class Substitute : Operator {

    constructor(expression: Generic?, variable: Generic?, value: Generic?) :
            super(NAME, genericArrayOf(expression, variable, value))

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 3

    override fun selfExpand(): Generic {
        return if (parameters!![1] is JsclVector && parameters!![2] is JsclVector) {
            var a = parameters!![0]
            val variable = toVariables(parameters!![1] as JsclVector)
            val s = (parameters!![2] as JsclVector).elements()
            for (i in variable.indices) {
                a = a.substitute(variable[i], s[i])
            }
            a
        } else {
            val variable = parameters!![1].variableValue()
            parameters!![0].substitute(variable, parameters!![2])
        }
    }

    fun transmute(): Operator {
        val p = arrayOf(
            null,
            GenericVariable.content(parameters!![1]),
            GenericVariable.content(parameters!![2])
        )
        if (p[1] is JsclVector && p[2] is JsclVector) {
            return Substitute(parameters!![0], p[1], p[2])
        }
        return this
    }

    override fun expand(): Generic {
        return selfExpand()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Substitute(parameters).transmute()
    }

    override fun newInstance(): Variable {
        return Substitute(null, null, null)
    }

    companion object {
        const val NAME = "subst"
    }
}
