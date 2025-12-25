package jscl.math

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.MathEngine
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.pow

/**
 * User: serso
 * Date: 11/22/11
 * Time: 12:49 PM
 */
class TrigonometricTest {

    // todo serso: due to conversion errors values on the borders are calculated not precisely
    /*
 0;0;1;0;Infinity
 90;1;0;1.633123935319537E16;0
 180;0;-1;0;-8.165619676597685E15
 270;-1;0;5.443746451065123E15;0
 360;0;1;0;-4.0828098382988425E15
      */

    @Test
    fun testValues() {
        val stream = TrigonometricTest::class.java.getResourceAsStream("./trig_table.csv")
            ?: throw IllegalStateException("Cannot find trig_table.csv")

        val csvParser = CSVParserBuilder().withSeparator('\t').build()
        CSVReaderBuilder(InputStreamReader(stream)).withCSVParser(csvParser).build().use { reader ->
            val me: MathEngine = JsclMathEngine.getInstance()

            // skip first line
            reader.readNext()

            var line: Array<String>? = reader.readNext()
            while (line != null) {
                val degrees = line[0].toInt()

                val sinValue = line[1].toDouble()
                val cosValue = line[2].toDouble()
                val tgValue = line[3].toDouble()
                val ctgValue = line[4].toDouble()

                val radians = line[5].toDouble()

                val sinhValue = line[6].toDouble()
                val coshValue = line[7].toDouble()
                val tghValue = line[8].toDouble()
                val cthgValue = line[9].toDouble()

                val asinValue = line[10].toDouble()
                val acosValue = line[11].toDouble()
                val atanValue = line[12].toDouble()

                testValue(sinValue, me.evaluate("sin(${degrees}°)").toDouble(), degrees)
                testValue(cosValue, me.evaluate("cos(${degrees}°)").toDouble(), degrees)
                testValue(tgValue, me.evaluate("tan(${degrees}°)").toDouble(), degrees)
                testValue(ctgValue, me.evaluate("cot(${degrees}°)").toDouble(), degrees)

                testValue(sinhValue, me.evaluate("sinh(${degrees}°)").toDouble(), degrees)
                testValue(coshValue, me.evaluate("cosh(${degrees}°)").toDouble(), degrees)
                testValue(tghValue, me.evaluate("tanh(${degrees}°)").toDouble(), degrees)
                testValue(cthgValue, me.evaluate("coth(${degrees}°)").toDouble(), degrees)

                val angleUnits = me.getAngleUnits()
                try {
                    me.setAngleUnits(AngleUnit.rad)

                    testValue(sinValue, me.evaluate("sin($radians)").toDouble(), degrees)
                    testValue(cosValue, me.evaluate("cos($radians)").toDouble(), degrees)
                    testValue(tgValue, me.evaluate("tan($radians)").toDouble(), degrees)
                    testValue(ctgValue, me.evaluate("cot($radians)").toDouble(), degrees)

                    testValue(sinhValue, me.evaluate("sinh($radians)").toDouble(), degrees)
                    testValue(coshValue, me.evaluate("cosh($radians)").toDouble(), degrees)
                    testValue(tghValue, me.evaluate("tanh($radians)").toDouble(), degrees)
                    testValue(cthgValue, me.evaluate("coth($radians)").toDouble(), degrees)
                } finally {
                    me.setAngleUnits(angleUnits)
                }

                testValue(asinValue, me.evaluate("rad(asin($sinValue))").toDouble(), degrees)
                testValue(acosValue, me.evaluate("rad(acos($cosValue))").toDouble(), degrees)
                testValue(atanValue, me.evaluate("rad(atan($tgValue))").toDouble(), degrees)

                // todo serso: check this
                // testValue(degrees.toDouble(), me.evaluate("asin(sin(${degrees}°))").toDouble(), degrees)
                // testValue(degrees.toDouble(), me.evaluate("acos(cos(${degrees}°))").toDouble(), degrees)
                // testValue(degrees.toDouble(), me.evaluate("atan(tan(${degrees}°))").toDouble(), degrees)
                // testValue(degrees.toDouble(), me.evaluate("acot(cot(${degrees}°))").toDouble(), degrees)

                testValue(sinValue, me.evaluate("sin(asin(sin(${degrees}°)))").toDouble(), degrees)
                testValue(cosValue, me.evaluate("cos(acos(cos(${degrees}°)))").toDouble(), degrees)
                testValue(tgValue, me.evaluate("tan(atan(tan(${degrees}°)))").toDouble(), degrees)
                testValue(ctgValue, me.evaluate("cot(acot(cot(${degrees}°)))").toDouble(), degrees)

                line = reader.readNext()
            }
        }
    }

    private fun testValue(expected: Double, actual: Double, degrees: Int) {
        when {
            expected.isInfinite() && actual.isInfinite() -> {
                // ok
            }
            expected.isNaN() && actual.isNaN() -> {
                // ok
            }
            else -> {
                assertTrue("Actual: $actual, expected: $expected for $degrees°", abs(expected - actual) < 10.0.pow(-10.0))
            }
        }
    }
}
