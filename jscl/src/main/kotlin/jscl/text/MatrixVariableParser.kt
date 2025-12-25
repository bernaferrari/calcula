package jscl.text

import jscl.math.Generic
import jscl.math.MatrixVariable
import jscl.math.Variable

internal class MatrixVariableParser private constructor() : Parser<Variable> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Variable {
        return MatrixVariable(MatrixParser.parser.parse(p, previousSumElement))
    }

    companion object {
        @JvmField
        val parser: Parser<Variable> = MatrixVariableParser()
    }
}
