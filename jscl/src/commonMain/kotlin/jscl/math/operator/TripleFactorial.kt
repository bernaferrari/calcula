package jscl.math.operator

import jscl.NotSupportedException
import jscl.math.Generic
import jscl.math.Variable
import jscl.mathml.MathML
import jscl.text.ParserUtils
import jscl.text.msg.Messages

/**
 * User: serso
 * Date: 12/15/11
 * Time: 10:43 PM
 */
class TripleFactorial : PostfixFunction {

    constructor(expression: Generic?) : super(NAME, expression?.let { arrayOf<Generic>(it) })

    private constructor(parameter: Array<Generic>) : super(NAME, ParserUtils.copyOf<Generic>(parameter, 1))

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        throw NotSupportedException(Messages.msg_18)
    }

    override fun selfNumeric(): Generic {
        throw NotSupportedException(Messages.msg_18)
    }

    override fun toMathML(element: MathML, data: Any?) {
        throw NotSupportedException(Messages.msg_18)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return TripleFactorial(parameters)
    }

    override fun toString(): String {
        throw NotSupportedException(Messages.msg_18)
    }

    override fun newInstance(): Variable {
        return TripleFactorial(null as Generic?)
    }

    companion object {
        const val NAME = "!!!"
    }
}
