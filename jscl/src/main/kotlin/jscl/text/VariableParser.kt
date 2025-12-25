package jscl.text

import jscl.math.Generic
import jscl.math.Variable

class VariableParser private constructor() : Parser<Variable> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Variable {
        return internalParser.parse(p, previousSumElement)
    }

    companion object {
        @JvmField
        val parser: Parser<Variable> = VariableParser()

        private val parsers: List<Parser<out Variable>> = listOf(
            OperatorParser.parser,
            FunctionParser.parser,
            ConstantParser.parser
        )

        private val internalParser: MultiTryParser<Variable> = MultiTryParser(parsers.toMutableList())
    }
}
