package com.bernaferrari.calculator.domain.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext as JavaContext
import java.math.RoundingMode as JavaRounding
import kotlin.random.Random
import kotlin.test.*

class DecimalDifferentialTest {
    @Test fun arithmeticAndRoundingMatchBigDecimal() {
        val random = Random(73129)
        repeat(600) {
            val a = BigDecimal(BigInteger.valueOf(random.nextLong(-999999999999999999L, 999999999999999999L)), random.nextInt(-100, 101))
            val b = BigDecimal(BigInteger.valueOf(random.nextLong(1, 999999999999999999L)), random.nextInt(-100, 101))
            val mode = RoundingMode.entries[random.nextInt(3)]
            val mc = MathContext(random.nextInt(1, 41), mode)
            val jmc = JavaContext(mc.precision, JavaRounding.valueOf(mode.name))
            val x = Decimal(a.toString()); val y = Decimal(b.toString())
            fun same(expected: BigDecimal, actual: Decimal) = assertEquals(0, expected.compareTo(BigDecimal(actual.toPlainString())), "$a, $b, $mc: expected $expected, got $actual")
            same(a.add(b, jmc), x.add(y, mc))
            same(a.subtract(b, jmc), x.subtract(y, mc))
            same(a.multiply(b, jmc), x.multiply(y, mc))
            same(a.divide(b, jmc), x.divide(y, mc))
            same(a.round(jmc), x.round(mc))
            val scale = random.nextInt(-100, 101)
            same(a.setScale(scale, jmc.roundingMode), x.setScale(scale, mode))
        }
    }

    @Test fun tiesAndExtremeScalesMatchBigDecimal() {
        for (text in listOf("2.5", "3.5", "-2.5", "-3.5", "9.9995", "-9.9995", "1E10000", "1E-10000")) {
            for (mode in RoundingMode.entries) {
                val expected = BigDecimal(text).round(JavaContext(1, JavaRounding.valueOf(mode.name)))
                val actual = Decimal(text).round(MathContext(1, mode))
                assertEquals(0, expected.compareTo(BigDecimal(actual.toPlainString())))
            }
        }
        assertEquals(Decimal("2"), Decimal("2.000"))
        assertEquals(Decimal("2").hashCode(), Decimal("2.000").hashCode())
    }
}
