package org.solovyev.android.calculator.model

import au.com.bytecode.opencsv.CSVReader
import com.google.common.base.Function
import jscl.JsclMathEngine
import jscl.MathEngine
import jscl.math.Expression
import jscl.util.ExpressionGeneratorWithInput
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.BaseCalculatorTest
import org.solovyev.android.calculator.ParseException
import java.io.InputStreamReader

@RunWith(value = RobolectricTestRunner::class)
class NumeralBaseTest : BaseCalculatorTest() {

    @Before
    override fun setUp() {
        super.setUp()
        engine.mathEngine.precision = 3
    }

    @Throws(jscl.text.ParseException::class, ParseException::class)
    fun testExpression(line: Array<String>, converter: Function<String, String>) {
        val dec = line[0].uppercase()
        val hex = "0x:" + line[1].uppercase()
        val bin = "0b:" + line[2].uppercase()

        val decExpression = converter.apply(dec)
        val decResult = engine.mathEngine.evaluate(decExpression)
        val hexExpression = converter.apply(hex)
        val hexResult = engine.mathEngine.evaluate(hexExpression)
        val binExpression = converter.apply(bin)
        val binResult = engine.mathEngine.evaluate(binExpression)

        Assert.assertEquals("dec-hex: $decExpression : $hexExpression", decResult, hexResult)
        Assert.assertEquals("dec-bin: $decExpression : $binExpression", decResult, binResult)
    }

    @Test
    @Throws(Exception::class)
    fun testConversion() {
        var reader: CSVReader? = null
        try {
            val me: MathEngine = JsclMathEngine.getInstance()

            reader = CSVReader(
                InputStreamReader(NumeralBaseTest::class.java.getResourceAsStream("/org/solovyev/android/calculator/model/nb_table.csv")),
                '\t'
            )

            // skip first line
            reader.readNext()

            var line = reader.readNext()
            while (line != null) {
                testExpression(line, DummyExpression())
                testExpression(line, Expression1())
                testExpression(line, Expression2())
                testExpression(line, Expression3())

                val dec = line[0].uppercase()
                val hex = "0x:" + line[1].uppercase()
                val bin = "0b:" + line[2].uppercase()

                val input = ArrayList<String>()
                input.add(dec)
                input.add(hex)
                input.add(bin)

                //System.out.println("Dec: " + dec);
                //System.out.println("Hex: " + hex);
                //System.out.println("Bin: " + bin);

                val eg = ExpressionGeneratorWithInput(input, 20)
                val expressions = eg.generate()

                val decExpression = expressions[0]
                val hexExpression = expressions[1]
                val binExpression = expressions[2]

                //System.out.println("Dec expression: " + decExpression);
                //System.out.println("Hex expression: " + hexExpression);
                //System.out.println("Bin expression: " + binExpression);

                val decResult = Expression.valueOf(decExpression).numeric().toString()
                //System.out.println("Dec result: " + decResult);

                val hexResult = Expression.valueOf(hexExpression).numeric().toString()
                //System.out.println("Hex result: " + hexResult);

                val binResult = Expression.valueOf(binExpression).numeric().toString()
                //System.out.println("Bin result: " + binResult);

                Assert.assertEquals("dec-hex: $decExpression : $hexExpression", decResult, hexResult)
                Assert.assertEquals("dec-bin: $decExpression : $binExpression", decResult, binResult)

                line = reader.readNext()
            }
        } finally {
            reader?.close()
        }
    }

    private class DummyExpression : Function<String, String> {
        override fun apply(s: String): String {
            return s
        }
    }

    private class Expression1 : Function<String, String> {
        override fun apply(s: String): String {
            return "$s*$s"
        }
    }

    private class Expression2 : Function<String, String> {
        override fun apply(s: String): String {
            return "$s*$s * sin($s) - 0b:1101"
        }
    }

    private class Expression3 : Function<String, String> {
        override fun apply(s: String): String {
            return "$s*$s * sin($s) - 0b:1101 + √($s) + exp ( $s )"
        }
    }
}
