package jscl.text

import jscl.math.Generic
import jscl.math.Variable
import jscl.math.VectorVariable

class VectorVariableParser private constructor() : Parser<Variable> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Variable {
        return VectorVariable(VectorParser.parser.parse(p, previousSumElement))
    }

    companion object {
        val parser: Parser<Variable> = VectorVariableParser()
    }
}
