package jscl.text

import jscl.math.ExpressionVariable
import jscl.math.Generic
import jscl.math.Variable

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
class PrimaryExpressionParser private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        return internalParser.parse(p, previousSumElement)
    }

    companion object {
        @JvmField
        val parser: Parser<Generic> = PrimaryExpressionParser()

        private val parsers: List<Parser<out Generic>> = listOf(
            VariableConverter(DoubleVariableParser.parser),
            JsclIntegerParser.parser,
            VariableConverter(VariableParser.parser),
            VariableConverter(MatrixVariableParser.parser),
            VariableConverter(VectorVariableParser.parser),
            VariableConverter<ExpressionVariable>(BracketedExpression.parser)
        )

        private val internalParser: Parser<Generic> = MultiTryParser(parsers.toMutableList())
    }
}
