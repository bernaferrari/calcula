package com.bernaferrari.calculator

import androidx.lifecycle.SavedStateHandle
import com.bernaferrari.calculator.domain.input.*
import com.bernaferrari.calculator.domain.math.*
import com.bernaferrari.calculator.domain.programmer.*
import com.bernaferrari.calculator.ui.converter.ConverterViewModel
import com.bernaferrari.calculator.ui.graph.GraphViewModel
import com.bernaferrari.calculator.ui.graph.Parameter
import kotlin.test.*

class FullReviewRegressionTest {
    @Test fun graphCopiesReuseCompilationAndEditsInvalidateIt() {
        val graph = com.bernaferrari.calculator.ui.graph.GraphState()
        val curves = graph.curves
        assertSame(curves, graph.copy(viewport = graph.viewport.copy(centerX = 10.0)).curves)
        val edited = graph.copy(functions = listOf(com.bernaferrari.calculator.ui.graph.GraphFunction(Editor("x²"))))
        assertNotSame(curves, edited.curves)
        assertEquals(4.0, (edited.curves.single() as Curve.Explicit).plot.at(2.0))
    }
    @Test fun nearMissIsNotAnIntersection() {
        assertTrue(KeyPoints.intersections(Plot.compile("(x−0.1)²+0.000001")!!, Plot.compile("0")!!, -2.0, 2.0).isEmpty())
    }
    @Test fun expressionBudgetsCoverUnaryPowersAndExpansion() {
        for (text in listOf("−".repeat(1000) + "1", "1^".repeat(1000) + "1")) {
            assertNull(CalcEngine.evaluateOrNull(text))
            assertNull(Plot.compile(text))
        }
        assertFailsWith<CalcException> {
            UserFunctions.expand("double(".repeat(8) + "1" + ")".repeat(8), listOf(UserFunction("double", "x+x+x+x+x+x+x+x")))
        }
    }
    @Test fun squareSelectedNegative() {
        val original = CalcState(editor = Editor("−2"), selection = Span(0, 2))
        val edited = Calculator.press(original, Key.SQUARE).first
        assertEquals(Decimal(4), CalcEngine.evaluate(edited.editor.text), edited.editor.text)
    }
    @Test fun wideningBytesPreservesSignedAndUnsignedValues() {
        for (signed in listOf(true, false)) for (bits in 0..255) {
            val state = Programmer.withValue(ProgState(word = WordSize.BYTE, signed = signed), bits.toLong())
            val expected = if (signed) Programmer.wrap(bits.toLong(), WordSize.BYTE) else bits.toLong()
            assertEquals(expected, Programmer.value(Programmer.withWord(state, WordSize.WORD)), "$bits signed=$signed")
        }
    }
    @Test fun wideningMinimumSignedBytePreservesValue() {
        val original = Programmer.withValue(ProgState(word = WordSize.BYTE), -128)
        assertEquals(-128L, Programmer.value(Programmer.withWord(original, WordSize.WORD)))
    }
    @Test fun programmerRejectsDeepExpressionsWithoutCrashing() {
        assertNull(Programmer.value(ProgState(tokens = List(5000) { "NOT" } + "1")))
        assertNull(Programmer.value(ProgState(tokens = List(100) { "(" } + "1" + List(100) { ")" })))
    }
    @Test fun largeShiftCountMustNotBecomeZero() {
        val original = ProgState(tokens = listOf("1", "<<", "4294967296"))
        assertNotEquals(1L, Programmer.value(original))
    }
    @Test fun converterDecimalMustKeepSeedValid() {
        val vm = ConverterViewModel(SavedStateHandle())
        vm.onUnit(false, com.bernaferrari.calculator.domain.convert.Units.byId("mm")!!)
        vm.seed("10000000000")
        vm.onActivate(false)
        vm.onDecimal()
        vm.onDigit('5')
        assertTrue(vm.state.value.output.isNotEmpty(), vm.state.value.input)
    }
    @Test fun sliderSnapStaysInsideRange() {
        val p = Parameter(value = 0.001, min = 0.001, max = 100.0)
        assertTrue(GraphViewModel.snap(p.value, p) >= p.min)
    }
    @Test fun decimalScaleMustNotWrap() {
        assertFailsWith<IllegalArgumentException> { Decimal("1.0E-2147483648") }
    }
    @Test fun tangentialIntersectionsAreFound() {
        val f = Plot.compile("(x−0.1)²")!!
        val g = Plot.compile("0")!!
        assertTrue(KeyPoints.intersections(f, g, -2.0, 2.0).any { kotlin.math.abs(it.x - 0.1) < 1e-6 })
    }
    @Test fun deepExpressionIsRejectedWithoutStackOverflow() {
        assertFailsWith<CalcException> { CalcEngine.evaluate("(".repeat(5000) + "1" + ")".repeat(5000)) }
    }
    @Test fun insertingNumberReplacesSelectedText() {
        val original = CalcState(editor = Editor("123"), selection = Span(0, 3))
        val edited = Calculator.insertNumber(original, "5")
        assertEquals("5", edited.editor.text)
        assertNull(edited.selection)
    }
}
