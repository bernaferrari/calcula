package jscl.text

import jscl.math.Generic
import jscl.math.function.Function
import jscl.math.function.Root
import jscl.text.msg.Messages

class RootParser private constructor() : Parser<Function> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Function {
        val pos0 = p.position.toInt()

        val name = Identifier.parser.parse(p, previousSumElement)
        if (name.compareTo("root") != 0) {
            ParserUtils.throwParseException(p, pos0, Messages.msg_11, "root")
        }

        val subscript = ParserUtils.parseWithRollback(Subscript.parser, pos0, previousSumElement, p)
        val parameters = ParserUtils.parseWithRollback(ParameterListParser.parser1, pos0, previousSumElement, p)

        return Root(parameters, subscript)
    }

    companion object {
        @JvmField
        val parser: Parser<Function> = RootParser()
    }
}
