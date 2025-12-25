package jscl.util

class ExpressionGeneratorWithInput : AbstractExpressionGenerator<List<String>> {

    private val subExpressions: List<String>

    constructor(subExpressions: List<String>) : this(subExpressions, 10)

    constructor(subExpressions: List<String>, depth: Int) : super(depth) {
        this.subExpressions = subExpressions.toMutableList()
    }

    override fun generate(): List<String> {
        val expressions = mutableListOf<StringBuilder>()
        for (subExpression in subExpressions) {
            expressions.add(StringBuilder(subExpression))
        }

        var i = 0
        while (i < depth) {

            val operation = generateOperation()
            val function = generateFunction()
            val brackets = generateBrackets()

            for (j in subExpressions.indices) {
                val expression = expressions[j]
                expression.append(operation.token)

                if (function == null) {
                    expression.append(subExpressions[j])
                } else {
                    expression.append(function.token).append("(").append(subExpressions[j]).append(")")
                }

                if (brackets) {
                    expressions[j] = StringBuilder("(").append(expression).append(")")
                }
            }
            i++
        }

        val result = mutableListOf<String>()
        for (expression in expressions) {
            result.add(expression.toString())
        }

        return result
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val input = mutableListOf<String>()
            input.add("3")
            input.add("0x:fed")
            input.add("0b:101")
            for (expression in ExpressionGeneratorWithInput(input, 20).generate()) {
                println(expression)
            }
        }
    }
}
