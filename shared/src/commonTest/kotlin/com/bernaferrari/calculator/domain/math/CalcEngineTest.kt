package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.test.Test

class CalcEngineTest {
    private fun eval(expr: String, angle: AngleUnit = AngleUnit.DEGREES) =
        NumberFormatter.format(CalcEngine.evaluate(expr, angle))

    private fun assertError(expr: String, error: CalcError) {
        try {
            CalcEngine.evaluate(expr)
            fail("Expected $error for $expr")
        } catch (e: CalcException) {
            assertEquals(error, e.error, expr)
        }
    }

    @Test fun moreFunctionsFromTheLibrary() {
        assertEquals("−3", eval("floor(−2.5)"))
        assertEquals("3", eval("ceil(2.1)"))
        assertEquals("3", eval("round(2.5)"))
        assertEquals("−3", eval("round(−2.5)"))
        assertEquals("−1", eval("sgn(−7)"))
        assertEquals("0", eval("sgn(0)"))
        assertEquals("10", eval("log₂(1024)"))
        assertEquals("2", eval("sec(60)"))
        assertEquals("2", eval("csc(30)"))
        assertEquals("0", eval("cot(90)"))
        assertEquals("0", eval("sinh⁻¹(0)"))
        assertEquals("0", eval("cosh⁻¹(1)"))
        assertEquals("0", eval("tanh⁻¹(0)"))
        assertError("cosh⁻¹(0.5)", CalcError.DOMAIN)
        assertError("tanh⁻¹(1)", CalcError.DOMAIN)
        assertError("csc(0)", CalcError.DOMAIN)
        assertError("log₂(0)", CalcError.DOMAIN)
        // The longer names still win over their prefixes, and the old ones keep working.
        assertEquals("2", eval("log(100)"))
        assertEquals("0", eval("sinh(0)"))
    }

    @Test fun cancellationHidesArithmeticRoundingResidue() {
        val expressions = listOf("1÷3×3−1", "3^−1×3−1", "√2×√2−2", "sin(30)−0.5", "tan(45)−1", "log₂(1024)−10")
        for (expression in expressions) {
            assertEquals("0", eval(expression), expression)
        }
    }

    @Test fun exactDifferencesAndSmallValuesSurviveCancellationHandling() {
        assertEquals("1E−39", eval("1.000000000000000000000000000000000000001−1"))
        assertEquals("1E−40", eval("1E−40+0"))
        assertEquals("1", eval("1E35+1−1E35"))
        assertEquals("1E−16", eval("1.0000000000000001−1"))
        assertEquals("1E−15", eval("sin(90)−0.999999999999999"))
    }

    @Test fun powersOfTwoHaveExactBinaryLogarithms() {
        assertEquals(Decimal(10), CalcEngine.evaluate("log₂(1024)"))
        assertEquals(Decimal(-3), CalcEngine.evaluate("log₂(0.125)"))
        assertEquals("1.58496250072116", eval("log₂(3)"))
    }

    @Test fun symbolicIdentitiesUseExactArithmetic() {
        val third = CalcEngine.evaluateValue("1÷3", AngleUnit.DEGREES)
        assertEquals("1", third.symbolic?.fraction?.numerator.toString())
        assertEquals("3", third.symbolic?.fraction?.denominator.toString())
        for (expression in listOf("1÷3×3−1", "√2×√8−4", "√8−2√2", "2π−2π", "3e−e−2e", "sin(45)×√2−1")) {
            val value = CalcEngine.evaluateValue(expression, AngleUnit.DEGREES)
            assertEquals(Decimal.ZERO, value.v, expression)
            assertEquals(Decimal.ZERO, value.error, expression)
        }
        assertEquals("0", eval("sin(π÷6)−0.5", AngleUnit.RADIANS))
        assertEquals("0", eval("tan(π÷4)−1", AngleUnit.RADIANS))
        assertEquals("0", eval("(2÷3)^−2−2.25"))
        assertEquals("1", eval("10^100+1−10^100"))
    }

    @Test fun decimalRootCancellationUsesThePropagatedRadius() {
        val root = CalcEngine.evaluateValue("√1000003", AngleUnit.DEGREES)
        assertEquals(null, root.symbolic)
        assertEquals(true, root.error?.signum() == 1)
        assertEquals("0", eval("√1000003×√1000003−1000003"))
        assertEquals("−1E−19", eval("√1000003×√1000003−1000003.0000000000000000001"))
    }

    @Test fun boundedArithmeticHandlesExtremeMagnitudes() {
        assertEquals("4.14935960343785E9130", eval("3000!"))
        assertEquals("1E9999", eval("1E9999+1E−9999"))
        assertEquals(eval("9^9999"), eval("9^9999+1"))
    }

    @Test fun repeatedSubexpressionsCancelOnlyAfterDomainValidation() {
        val value = CalcEngine.evaluateValue("e^100+42−exp(100)", AngleUnit.DEGREES)
        assertEquals(Decimal(42), value.v)
        assertEquals(true, value.provenExact)
        assertError("(1÷0)+42−(1÷0)", CalcError.DIVIDE_BY_ZERO)
        assertError("√(−1)+42−√(−1)", CalcError.DOMAIN)
        assertEquals("110", eval("100+10%"))
        assertEquals("100.1", eval("100+(10%)"))
    }

    @Test fun scientificExponentialKeepsTinyNonzeroResults() {
        val formatted = eval("exp(−10000)")
        assertEquals("1.13548386531474E−4343", formatted)
    }

    @Test fun evaluationObservesTheTimeBudget() {
        val failure = kotlin.test.assertFailsWith<CalcException> {
            CalcEngine.evaluateValue("1+1", AngleUnit.DEGREES, timeBudgetMillis = 0)
        }
        assertEquals(CalcError.TIME_LIMIT, failure.error)
    }

    @Test fun arithmetic() {
        assertEquals("3", eval("1+2"))
        assertEquals("0.3", eval("0.1+0.2"))
        assertEquals("7", eval("1+2×3"))
        assertEquals("9", eval("(1+2)×3"))
        assertEquals("2.5", eval("5÷2"))
        assertEquals("1", eval("1÷3×3"))
        assertEquals("0.3333333333333333".take(17), eval("1÷3"))
        assertEquals("−1", eval("2−3"))
        assertEquals("6", eval("2*3"))
        assertEquals("−6", eval("2×−3"))
    }

    @Test fun precedenceOfPowerAndUnaryMinus() {
        assertEquals("−4", eval("−2^2"))
        assertEquals("4", eval("(−2)^2"))
        assertEquals("512", eval("2^3^2"))
        assertEquals("0.5", eval("2^−1"))
        assertEquals("1.4142135623731", eval("2^0.5").take(15))
        assertEquals("−2", eval("(−8)^(1÷3)"))
    }

    @Test fun implicitMultiplication() {
        assertEquals("6", eval("2(3)"))
        assertEquals("6.28318530717959", eval("2π"))
        assertEquals("12", eval("(3)(4)"))
        assertEquals("1", eval("2sin(30)"))
        assertEquals("6", eval("3√4"))
    }

    @Test fun autoClosesParentheses() {
        assertEquals("5", eval("(2+3"))
        assertEquals("0.5", eval("sin(30"))
        assertEquals("20", eval("2×(3+(7"))
    }

    @Test fun smartPercent() {
        assertEquals("90", eval("120−25%"))
        assertEquals("110", eval("100+10%"))
        assertEquals("5", eval("50×10%"))
        assertEquals("500", eval("50÷10%"))
        assertEquals("0.25", eval("25%"))
    }

    @Test fun postfix() {
        assertEquals("120", eval("5!"))
        assertEquals("25", eval("5²"))
        assertEquals("27", eval("3³"))
        assertEquals("0.25", eval("4⁻¹"))
        assertEquals("3", eval("√9"))
        assertEquals("−3", eval("∛(−27)"))
        assertEquals("1.4142135623731", eval("√2").take(15))
    }

    @Test fun trigInDegrees() {
        assertEquals("0.5", eval("sin(30)"))
        assertEquals("0.5", eval("(sin(30)^2+cos(30)^2)÷2"))
        assertEquals("0", eval("cos(90)"))
        assertEquals("0", eval("sin(180)"))
        assertEquals("1", eval("tan(45)"))
        assertEquals("30", eval("sin⁻¹(0.5)"))
        assertError("tan(90)", CalcError.DOMAIN)
    }

    @Test fun trigInRadians() {
        assertEquals("0", eval("sin(π)", AngleUnit.RADIANS))
        assertEquals("−1", eval("cos(π)", AngleUnit.RADIANS))
        assertEquals("1.5707963267949", eval("sin⁻¹(1)", AngleUnit.RADIANS))
    }

    @Test fun logs() {
        assertEquals("3", eval("log(1000)"))
        assertEquals("−2", eval("log(0.01)"))
        assertEquals("1", eval("ln(e)"))
        assertError("ln(0)", CalcError.DOMAIN)
        assertError("log(−1)", CalcError.DOMAIN)
    }

    @Test fun logarithmRetainsTinyDifferencesOnEitherSideOfOne() {
        assertEquals("1E−20", eval("ln(1+1E−20)"))
        assertEquals("−1E−20", eval("ln(1−1E−20)"))
        assertEquals("4.34294481903252E−21", eval("log(1+1E−20)"))
        assertEquals("1.44269504088896E−20", eval("log₂(1+1E−20)"))
    }

    @Test fun logarithmRetriesWhenItsArgumentStraddlesZero() {
        assertEquals("3.73766961828337", eval("ln(exp(100)+42−exp(50)²)"))
        assertEquals("0.0238095238095238", eval("1÷(exp(100)+42−exp(50)²)"))
        assertError("ln(exp(100)−42−exp(50)²)", CalcError.DOMAIN)
    }

    @Test fun fractionalPowersUseTheUnroundedInput() {
        assertEquals("5E−21", eval("(1+1E−20)^0.5−1"))
        assertEquals("4", eval("(−8)^(2÷3)"))
        assertError("(−8)^0.5", CalcError.DOMAIN)
    }

    @Test fun rationalRootPowersKeepExactForms() {
        val expressions = mapOf(
            "4^0.5" to "2", "0^0.5" to "0", "8^(1÷3)" to "2", "27^(2÷3)" to "9", "0.25^0.5" to "0.5",
            "2^0.5×2^0.5−2" to "0", "2^(1÷2)×2^(1÷2)" to "2", "(−8)^(2÷3)" to "4",
        )
        for ((expression, expected) in expressions) {
            val result = CalcEngine.evaluateValue(expression, AngleUnit.DEGREES)
            assertEquals(Decimal(expected), result.v, expression)
            assertEquals(true, result.provenExact, expression)
        }
    }

    @Test fun piSquaredRetainsARefinableRadius() {
        val result = CalcEngine.evaluateValue("π^2", AngleUnit.DEGREES, displaySettled = { false })
        val radius = kotlin.test.assertNotNull(result.error)
        assertEquals(true, result.retryable)
        assertEquals(true, radius < Decimal("1E-150"))
    }

    @Test fun logarithmAndInverseTrigHaveExactIdentities() {
        for ((expression, expected) in mapOf(
            "ln(1)" to "0", "ln(e)" to "1", "ln(e^3)" to "3",
            "sin⁻¹(0.5)" to "30", "cos⁻¹(0)" to "90", "tan⁻¹(1)" to "45",
        )) {
            val result = CalcEngine.evaluateValue(expression, AngleUnit.DEGREES)
            assertEquals(Decimal(expected), result.v, expression)
            assertEquals(true, result.provenExact, expression)
        }
    }

    @Test fun provesSmallSymbolicSumsAndPowers() {
        val cases = mapOf(
            "π×π÷π" to "π",
            "π^−2×π²" to "1",
            "e×e÷e^2" to "1",
            "exp(100)+42−exp(50)²" to "42",
            "(√2+√3)²−5−2√6" to "0",
            "∛2×∛2×∛2−2" to "0",
            "∛(−4)×∛(−2)" to "2",
            "e^(ln(5))−5" to "0",
            "ln(8)÷ln(2)−3" to "0",
            "ln(2)+ln(3)−ln(6)" to "0",
            "ln(3÷4)+2ln(2)−ln(3)" to "0",
            "sin(10)²+cos(10)^2−1" to "0",
        )
        for ((expression, exactText) in cases) {
            val result = CalcEngine.evaluateValue(expression, AngleUnit.DEGREES)
            assertEquals(true, result.provenExact, expression)
            assertEquals(exactText, result.symbolic?.exactText(), expression)
        }
    }

    @Test fun exactRewritesPreserveDomainErrorsAndDistinctArguments() {
        assertError("e^(ln(0))", CalcError.DOMAIN)
        assertError("e^(ln(−5))", CalcError.DOMAIN)
        assertError("sin(1÷0)²+cos(1÷0)²", CalcError.DIVIDE_BY_ZERO)
        assertError("1÷(ln(2)+ln(3)−ln(6))", CalcError.DIVIDE_BY_ZERO)
        val distinct = CalcEngine.evaluateValue("sin(10)²+cos(11)²−1", AngleUnit.DEGREES)
        assertEquals(false, distinct.provenExact)
        assertEquals(-1, distinct.v.signum())
        val sumAngle = CalcEngine.evaluateValue("sin(√2+√3)", AngleUnit.DEGREES)
        assertEquals(false, sumAngle.provenExact)
        assertEquals(1, sumAngle.v.signum())
    }

    @Test fun symbolicFormsRoundTripAndReadOldAnswerTags() {
        val value = CalcEngine.evaluateValue("2√3−π²+∛2+ln(5)", AngleUnit.DEGREES)
        val symbolic = requireNotNull(value.symbolic)
        assertEquals(symbolic, SymbolicNumber.restore(symbolic.save()))
        assertEquals("2√3⁄5", SymbolicNumber.restore("2,5,3")?.exactText())
        assertEquals("2π", SymbolicNumber.restore("2,1,pi")?.exactText())
    }

    @Test fun largeAnglesAndFactorialsKeepTheirUsefulPrecision() {
        assertEquals(eval("sin(280)"), eval("sin(1E9999)"))
        assertEquals(eval("sin(π÷7)", AngleUnit.RADIANS), eval("sin((10^200+1÷7)×π)", AngleUnit.RADIANS))
        val exact = CalcEngine.evaluateValue("100!÷98!", AngleUnit.DEGREES)
        assertEquals(Decimal(9900), exact.v)
        assertEquals(true, exact.provenExact)
        assertEquals("1.42857142857143E9998", eval("floor(1E9999÷7)"))
    }

    @Test fun integerPowerUnderflowRoundsToZero() {
        assertEquals("0", eval("0.5^100000"))
        assertEquals("0", eval("2^−100000"))
        assertError("2^100000", CalcError.OVERFLOW)
    }

    @Test fun spacedDigitsAndCommasRequireInputNormalization() {
        assertError("1,5", CalcError.SYNTAX)
        assertError("1 000", CalcError.SYNTAX)
        assertEquals("3", eval("1 + 2"))
    }

    @Test fun errors() {
        assertError("1÷0", CalcError.DIVIDE_BY_ZERO)
        assertError("5+", CalcError.SYNTAX)
        assertError("×5", CalcError.SYNTAX)
        assertError("1)", CalcError.SYNTAX)
        assertError("√(−1)", CalcError.DOMAIN)
        assertError("(−1)!", CalcError.DOMAIN)
        assertError("2.5!", CalcError.DOMAIN)
        assertError("10^99999^9", CalcError.OVERFLOW)
    }

    @Test fun formatting() {
        assertEquals("1E20", eval("10^20"))
        assertEquals("1.5E−12", eval("1.5÷10^12"))
        assertEquals("123456789012345", eval("123456789012345"))
        assertEquals("2.43290200817664E18", eval("20!"))
        assertEquals("1E21", eval("1E20×10"))
        assertEquals("0.000001", eval("10^−6"))
    }
}
