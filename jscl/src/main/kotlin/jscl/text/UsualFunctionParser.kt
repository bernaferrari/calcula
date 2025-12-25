package jscl.text

import jscl.math.Generic
import jscl.math.function.Function
import jscl.math.function.FunctionsRegistry
import jscl.text.msg.Messages
import org.solovyev.common.math.MathRegistry

/**
 * User: serso
 * Date: 10/29/11
 * Time: 1:05 PM
 */
internal class UsualFunctionParser private constructor() : Parser<Function> {

    private val functionsRegistry: MathRegistry<Function> = FunctionsRegistry.getInstance()

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Function {
        val pos0 = p.position.toInt()

        val name = Identifier.parser.parse(p, previousSumElement)

        if (!valid(name)) {
            ParserUtils.throwParseException(p, pos0, Messages.msg_13)
        }

        val result = functionsRegistry.get(name)

        if (result != null) {
            val parameters = ParserUtils.parseWithRollback(ParameterListParser(result.getMinParameters()), pos0, previousSumElement, p)

            if (result.getMinParameters() <= parameters.size && result.getMaxParameters() >= parameters.size) {
                result.setParameters(parameters)
            } else {
                ParserUtils.throwParseException(p, pos0, Messages.msg_14, parameters.size)
            }
        } else {
            ParserUtils.throwParseException(p, pos0, Messages.msg_13)
        }

        return result!!
    }

    companion object {
        @JvmField
        val parser: Parser<Function> = UsualFunctionParser()

        @JvmStatic
        fun valid(name: String?): Boolean {
            return name != null && FunctionsRegistry.getInstance().getNames().contains(name)
        }
    }
}
