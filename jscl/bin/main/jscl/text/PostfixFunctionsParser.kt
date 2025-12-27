package jscl.text

import jscl.math.Generic
import jscl.math.function.PostfixFunctionsRegistry
import jscl.math.operator.Operator
import jscl.math.operator.TripleFactorial
import jscl.text.msg.Messages

class PostfixFunctionsParser(
    private val content: Generic
) : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        return parsePostfix(registry.getNames(), content, previousSumElement, p)
    }

    companion object {
        private val registry = PostfixFunctionsRegistry.getInstance()
        private val tripleFactorialParser = PostfixFunctionParser(TripleFactorial.NAME)

        @Throws(ParseException::class)
        private fun parsePostfix(
            names: List<String>,
            content: Generic,
            previousSumElement: Generic?,
            p: Parser.Parameters
        ): Generic {
            checkTripleFactorial(previousSumElement, p)

            for (i in names.indices) {
                val parser = PostfixFunctionParser(names[i])
                val functionName = parser.parse(p, previousSumElement) ?: continue

                val parameters = if (previousSumElement == null) arrayOf(content) else arrayOf(content, previousSumElement)
                val function = registry.get(functionName, parameters)

                if (function != null) {
                    return parsePostfix(names, function.expressionValue(), previousSumElement, p)
                }

                throw p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_4, listOf(functionName))
            }
            return content
        }

        @Throws(ParseException::class)
        private fun checkTripleFactorial(previousSumElement: Generic?, p: Parser.Parameters) {
            if (tripleFactorialParser.parse(p, previousSumElement) != null) {
                throw p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_18)
            }
        }
    }
}
