package jscl.math

import com.opencsv.CSVReader
import jscl.JsclMathEngine
import jscl.MathEngine
import jscl.text.ParseException
import jscl.util.ExpressionGeneratorWithInput
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.InputStreamReader

/**
 * User: serso
 * Date: 12/14/11
 * Time: 4:01 PM
 */
class NumeralBaseConversionTest {

    @Test
    fun testConversion() {
        var reader: CSVReader? = null
        try {
            val me: MathEngine = JsclMathEngine.getInstance()

            reader = com.opencsv.CSVReaderBuilder(InputStreamReader(NumeralBaseConversionTest::class.java.getResourceAsStream("nb_table.csv")!!))
                .withCSVParser(com.opencsv.CSVParserBuilder().withSeparator('\t').build())
                .build()

            // skip first line
            reader!!.readNext()

            var line = reader!!.readNext()
            while (line != null) {
                testExpression(line, DummyExpression())
                testExpression(line, Expression1())
                testExpression(line, Expression2())
                testExpression(line, Expression3())

                val dec = line[0].uppercase()
                val hex = "0x:" + line[1].uppercase()
                val bin = "0b:" + line[2].uppercase()

                val input = mutableListOf<String>()
                input.add(dec)
                input.add(hex)
                input.add(bin)

                // println("Dec: $dec")
                // println("Hex: $hex")
                // println("Bin: $bin")

                val eg = ExpressionGeneratorWithInput(input, 20)
                val expressions = eg.generate()

                val decExpression = expressions[0]
                val hexExpression = expressions[1]
                val binExpression = expressions[2]

                // println("Dec expression: $decExpression")
                // println("Hex expression: $hexExpression")
                // println("Bin expression: $binExpression")

                val decResult = Expression.valueOf(decExpression).numeric().toString()
                // println("Dec result: $decResult")

                val hexResult = Expression.valueOf(hexExpression).numeric().toString()
                // println("Hex result: $hexResult")

                val binResult = Expression.valueOf(binExpression).numeric().toString()
                // println("Bin result: $binResult")

                assertEquals("dec-hex: $decExpression : $hexExpression", decResult, hexResult)
                assertEquals("dec-bin: $decExpression : $binExpression", decResult, binResult)

                line = reader!!.readNext()
            }
        } finally {
            reader?.close()
        }
    }

    private class DummyExpression : (String) -> String {
        override fun invoke(s: String): String {
            return s
        }
    }

    private class Expression1 : (String) -> String {
        override fun invoke(s: String): String {
            return "$s*$s"
        }
    }

    private class Expression2 : (String) -> String {
        override fun invoke(s: String): String {
            return "$s*$s * sin($s) - 0b:1101"
        }
    }

    private class Expression3 : (String) -> String {
        override fun invoke(s: String): String {
            return "$s*$s * sin($s) - 0b:1101 + √($s) + exp ( $s )"
        }
    }

    companion object {
        @JvmStatic
        @Throws(ParseException::class)
        fun testExpression(line: Array<String>, converter: (String) -> String) {
            val dec = line[0].uppercase()
            val hex = "0x:" + line[1].uppercase()
            val bin = "0b:" + line[2].uppercase()

            val decResult = Expression.valueOf(converter(dec)).numeric().toString()
            val hexResult = Expression.valueOf(converter(hex)).numeric().toString()
            val binResult = Expression.valueOf(converter(bin)).numeric().toString()

            assertEquals(decResult, hexResult)
            assertEquals(decResult, binResult)
        }
    }
}
