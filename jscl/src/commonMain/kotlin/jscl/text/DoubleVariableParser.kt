package jscl.text

import jscl.math.DoubleVariable
import jscl.math.Generic
import jscl.math.Variable

class DoubleVariableParser private constructor() : Parser<Variable> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Variable {
        return DoubleVariable(DoubleParser.parser.parse(p, previousSumElement))
    }

    companion object {
        val parser: Parser<Variable> = DoubleVariableParser()
    }
}
