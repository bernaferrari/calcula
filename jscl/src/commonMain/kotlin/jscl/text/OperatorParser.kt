package jscl.text

import jscl.math.Generic
import jscl.math.operator.Operator
import jscl.math.operator.matrix.OperatorsRegistry
import jscl.text.msg.Messages

class OperatorParser private constructor() : Parser<Operator> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Operator {
        val pos0 = p.position.toInt()

        val operatorName = Identifier.parser.parse(p, previousSumElement)
        if (!valid(operatorName)) {
            ParserUtils.throwParseException(p, pos0, Messages.msg_3, operatorName)
        }

        val operator = OperatorsRegistry.getInstance().get(operatorName)

        var result: Operator? = null
        if (operator != null) {
            val parameters = ParserUtils.parseWithRollback(ParameterListParser(operator.getMinParameters()), pos0, previousSumElement, p)

            result = OperatorsRegistry.getInstance().get(operatorName, parameters)
            if (result == null) {
                ParserUtils.throwParseException(p, pos0, Messages.msg_2, operatorName)
            }
        } else {
            ParserUtils.throwParseException(p, pos0, Messages.msg_3, operatorName)
        }

        assert(result != null)
        return result!!
    }

    companion object {
        val parser: Parser<Operator> = OperatorParser()

        fun valid(name: String?): Boolean {
            return name != null && OperatorsRegistry.getInstance().getNames().contains(name)
        }
    }
}
