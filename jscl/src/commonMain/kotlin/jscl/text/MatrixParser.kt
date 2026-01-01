package jscl.text

import jscl.math.Generic
import jscl.math.JsclVector
import jscl.math.Matrix
import jscl.util.ArrayUtils

class MatrixParser private constructor() : Parser<Matrix> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Matrix {
        val pos0 = p.position.toInt()

        val vectors = ArrayList<Generic>()

        ParserUtils.tryToParse(p, pos0, '[')

        // Parse first vector - failure resets position and propagates
        when (val firstVector = VectorParser.parser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> vectors.add(firstVector.value)
            is ParseResult.Failure -> {
                p.position.value = pos0
                throw firstVector.toException()
            }
        }

        // Parse additional comma-separated vectors
        CommaAndVector.parser.parseWhileSuccessful(p, previousSumElement, Unit) { _, vector ->
            vectors.add(vector)
        }

        ParserUtils.tryToParse(p, pos0, ']')

        @Suppress("UNCHECKED_CAST")
        val vectorArray = ArrayUtils.toArray(vectors, arrayOfNulls<Generic>(vectors.size)) as Array<JsclVector>
        return Matrix.frame(vectorArray).transpose()
    }

    companion object {
        val parser: Parser<Matrix> = MatrixParser()
    }
}
