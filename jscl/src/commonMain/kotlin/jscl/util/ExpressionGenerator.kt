package jscl.util

class ExpressionGenerator : AbstractExpressionGenerator<String> {

    constructor() : super()

    constructor(depth: Int) : super(depth)

    override fun generate(): String {
        var result = StringBuilder()

        result.append(generateNumber())

        var i = 0
        while (i < depth) {

            val operation = generateOperation()
            val function = generateFunction()
            val brackets = generateBrackets()

            result.append(operation.token)

            if (function == null) {
                result.append(generateNumber())
            } else {
                result.append(function.token).append("(").append(generateNumber()).append(")")
            }

            if (brackets) {
                result = StringBuilder("(").append(result).append(")")
            }

            i++
        }

        return result.toString()
    }

    companion object {
        fun main(args: Array<String>) {
            println(ExpressionGenerator(20).generate())
        }
    }
}
