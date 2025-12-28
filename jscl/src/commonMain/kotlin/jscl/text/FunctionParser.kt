package jscl.text

import jscl.math.Generic
import jscl.math.function.Function

class FunctionParser private constructor() : Parser<Function> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Function {
        return MultiTryParser(parsers.toMutableList()).parse(p, previousSumElement)
    }

    companion object {
        val parser: Parser<Function> = FunctionParser()

        private val parsers: List<Parser<Function>> = listOf(
            UsualFunctionParser.parser,
            RootParser.parser,
            ImplicitFunctionParser.parser
        )
    }
}
