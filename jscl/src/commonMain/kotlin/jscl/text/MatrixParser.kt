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

        try {
            vectors.add(VectorParser.parser.parse(p, previousSumElement))
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        while (true) {
            try {
                vectors.add(CommaAndVector.parser.parse(p, previousSumElement))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
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
