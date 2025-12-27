package org.solovyev.android.calculator

import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import jscl.MathEngine
import jscl.NumeralBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.text.TextProcessor
import org.solovyev.android.calculator.text.TextProcessorEditorResult
import org.solovyev.android.calculator.view.TextHighlighter
import java.util.Date
import java.util.Random

@RunWith(RobolectricTestRunner::class)
class TextHighlighterTest {

    private lateinit var engine: Engine

    @Before
    fun setUp() {
        engine = Tests.makeEngine()
    }

    @Test
    fun testProcess() {
        var textHighlighter = TextHighlighter(Color.TRANSPARENT, false, engine)

        val random = Random(Date().time)
        for (i in 0 until 100) {
            val sb = StringBuilder()
            for (j in 0 until 1000) {
                sb.append(if (random.nextBoolean()) "(" else ")")
            }
            try {
                textHighlighter.process(sb.toString())
            } catch (e: Exception) {
                println(sb.toString())
                throw e
            }
        }

        //assertEquals("<font color=\"#000000\"></font>)(((())())", textHighlighter.process(")(((())())").toString());
        assertEquals(")", textHighlighter.process(")").toString())
        assertEquals(")()(", textHighlighter.process(")()(").toString())

        textHighlighter = TextHighlighter(0, true, engine)
        assertEquals("1 000 000", textHighlighter.process("1000000").toString())
        assertEquals("1 000 000", textHighlighter.process("1000000").toString())
        assertEquals("0.1E3", textHighlighter.process("0.1E3").toString())
        assertEquals("1E3", textHighlighter.process("1E3").toString())
        //assertEquals("2<b>0x:</b>", textHighlighter.process("20x:").toString());
        assertEquals("20g", textHighlighter.process("20g").toString())
        assertEquals("22g", textHighlighter.process("22g").toString())
        assertEquals("20ю", textHighlighter.process("20ю").toString())
        assertEquals("20ъ", textHighlighter.process("20ъ").toString())
        assertEquals("3!!", textHighlighter.process("3!!").toString())
        assertEquals("2", textHighlighter.process("2").toString())
        assertEquals("21", textHighlighter.process("21").toString())
        assertEquals("214", textHighlighter.process("214").toString())
        assertEquals("2 145", textHighlighter.process("2 145").toString())
        assertEquals("1 000 000E3", textHighlighter.process("1000000E3").toString())
        assertEquals("-1 000 000E3", textHighlighter.process("-1000000E3").toString())
        assertEquals("-1 000 000E-3", textHighlighter.process("-1000000E-3").toString())
        assertEquals("-1 000 000E-30000", textHighlighter.process("-1000000E-30000").toString())
        textHighlighter = TextHighlighter(0, false, engine)

        textHighlighter.process("cannot calculate 3^10^10 !!!\n" +
                "        unable to enter 0. FIXED\n" +
                "        empty display in Xperia Rayo\n" +
                "        check привиденная FIXED\n" +
                "        set display result only if text in editor was not changed FIXED\n" +
                "        shift M text to the left\n" +
                "        do not show SYNTAX ERROR always (may be show send clock?q) FIXED\n" +
                "        ln(8)*log(8) =>  ln(8)*og(8) FIXED\n" +
                "        copy/paste ln(8)*log(8)\n" +
                "        6!^2 ERROR")



       /* assertEquals("<b>0x:</b>E", textHighlighter.process("0x:E").toString());
        assertEquals("<b>0x:</b>6F", textHighlighter.process("0x:6F").toString());
        assertEquals("<b>0x:</b>6F.", textHighlighter.process("0x:6F.").toString());
        assertEquals("<b>0x:</b>6F.2", textHighlighter.process("0x:6F.2").toString());
        assertEquals("<b>0x:</b>6F.B", textHighlighter.process("0x:6F.B").toString());
        assertEquals("<b>0x:</b>006F.B", textHighlighter.process("0x:006F.B").toString());
        assertEquals("<b>0x:</b>0", textHighlighter.process("0x:0").toString());
        assertEquals("<b>0x:</b>FF33233FFE", textHighlighter.process("0x:FF33233FFE").toString());
        assertEquals("<b>0x:</b>FF33 233 FFE", textHighlighter.process("0x:FF33 233 FFE").toString());*/

        val me = engine.mathEngine
        try {
            me.numeralBase = NumeralBase.hex
            assertEquals("E", textHighlighter.process("E").toString())
            assertEquals(".E", textHighlighter.process(".E").toString())
            assertEquals("E+", textHighlighter.process("E+").toString())
            assertEquals("E.", textHighlighter.process("E.").toString())
            assertEquals(".E.", textHighlighter.process(".E.").toString())
            assertEquals("6F", textHighlighter.process("6F").toString())
            assertEquals("6F", textHighlighter.process("6F").toString())
            assertEquals("6F.", textHighlighter.process("6F.").toString())
            assertEquals("6F.2", textHighlighter.process("6F.2").toString())
            assertEquals("6F.B", textHighlighter.process("6F.B").toString())
            assertEquals("006F.B", textHighlighter.process("006F.B").toString())
        } finally {
            me.numeralBase = NumeralBase.dec
        }

       /* assertEquals("<b>0b:</b>110101", textHighlighter.process("0b:110101").toString());
        assertEquals("<b>0b:</b>110101.", textHighlighter.process("0b:110101.").toString());
        assertEquals("<b>0b:</b>110101.101", textHighlighter.process("0b:110101.101").toString());
        assertEquals("<b>0b:</b>11010100.1", textHighlighter.process("0b:11010100.1").toString());
        assertEquals("<b>0b:</b>110101.0", textHighlighter.process("0b:110101.0").toString());
        assertEquals("<b>0b:</b>0", textHighlighter.process("0b:0").toString());
        assertEquals("<b>0b:</b>1010100101111010101001", textHighlighter.process("0b:1010100101111010101001").toString());
        assertEquals("<b>0b:</b>101 010   01 0 111   1 0 10101001", textHighlighter.process("0b:101 010   01 0 111   1 0 10101001").toString());*/

        try {
            me.numeralBase = NumeralBase.bin
            assertEquals("110101", textHighlighter.process("110101").toString())
            assertEquals("110101.", textHighlighter.process("110101.").toString())
            assertEquals("110101.101", textHighlighter.process("110101.101").toString())
            assertEquals("11010100.1", textHighlighter.process("11010100.1").toString())
            assertEquals("110101.0", textHighlighter.process("110101.0").toString())
            assertEquals("0", textHighlighter.process("0").toString())
            assertEquals("1010100101111010101001", textHighlighter.process("1010100101111010101001").toString())
            assertEquals("101 010   01 0 111   1 0 10101001", textHighlighter.process("101 010   01 0 111   1 0 10101001").toString())
        } finally {
            me.numeralBase = NumeralBase.dec
        }
    }

    @Test
    fun testTime() {
        val textHighlighter: TextProcessor<*, String> = TextHighlighter(Color.WHITE, false, engine)

        val count = 100
        val subExpression = "cos(acos(t8ln(t5t85tln(8ln(5t55tln(5))))))+tln(88cos(tln(t)))+t√(ln(t))"
        val expression = StringBuilder(subExpression.length * count)
        for (i in 0 until count) {
            expression.append(subExpression)
            expression.append("+")
        }
        expression.append(subExpression)

        val startTime = System.currentTimeMillis()
        textHighlighter.process(expression.toString())
        val endTime = System.currentTimeMillis()
        println("Total time, ms: " + (endTime - startTime))
    }

    @Test
    fun testDarkColor() {
        val textHighlighter: TextProcessor<TextProcessorEditorResult, String> = TextHighlighter(Color.BLACK, false, engine)
        val res = textHighlighter.process("sin(2cos(3))").charSequence
        assertEquals("sin(2cos(3))", res.toString())
        val spannable = res as Spannable
        val spans = spannable.getSpans(0, res.length, ForegroundColorSpan::class.java)
        assertEquals(2, spans.size)
        assertEquals(4, spannable.getSpanStart(spans[0]))
        assertEquals(res.length - 1, spannable.getSpanEnd(spans[0]))
    }

    @Test
    fun testIsDark() {
        assertFalse(TextHighlighter.isDark(Color.WHITE))
        assertFalse(TextHighlighter.isDark(Color.LTGRAY))
        assertTrue(TextHighlighter.isDark(Color.DKGRAY))
        assertTrue(TextHighlighter.isDark(Color.BLACK))

    }
}
