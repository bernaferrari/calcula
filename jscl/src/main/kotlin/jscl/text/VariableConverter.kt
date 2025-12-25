package jscl.text

import jscl.math.Generic
import jscl.math.Variable

/**
 * User: serso
 * Date: 10/27/11
 * Time: 3:21 PM
 */
internal class VariableConverter<T : Variable>(
    variableParser: Parser<T>
) : AbstractConverter<T, Generic>(variableParser) {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        return parser.parse(p, previousSumElement).expressionValue()
    }
}
