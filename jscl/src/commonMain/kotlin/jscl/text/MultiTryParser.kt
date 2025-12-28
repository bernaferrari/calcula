package jscl.text

import jscl.math.Generic

class MultiTryParser<T>(
    private val parsers: List<Parser<out T>>
) : Parser<T> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): T {
        var result: T? = null

        val it = parsers.iterator()
        while (it.hasNext()) {
            try {
                val parser = it.next()
                result = parser.parse(p, previousSumElement)
            } catch (e: ParseException) {
                p.addException(e)

                if (!it.hasNext()) {
                    throw e
                }
            }

            if (result != null) {
                break
            }
        }

        return result!!
    }
}
