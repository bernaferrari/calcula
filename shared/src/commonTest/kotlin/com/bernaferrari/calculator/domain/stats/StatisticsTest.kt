package com.bernaferrari.calculator.domain.stats

import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.NumberFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StatisticsTest {
    private fun of(vararg v: String) = Statistics.summarize(v.map(::Decimal))!!
    private fun Decimal.s() = NumberFormatter.format(this, 12)

    @Test fun theClassicExample() {
        // 2 4 4 4 5 5 7 9: mean 5, population σ 2.
        val s = of("2", "4", "4", "4", "5", "5", "7", "9")
        assertEquals(8, s.count)
        assertEquals("40", s.sum.s())
        assertEquals("5", s.mean.s())
        assertEquals("4.5", s.median.s())
        assertEquals("2", s.populationSd.s())
        assertEquals("2.1380899353", s.sampleSd!!.s().take(12))
        assertEquals("2", s.min.s())
        assertEquals("9", s.max.s())
    }

    @Test fun exactDecimalsAndOddCounts() {
        val s = of("0.1", "0.2", "0.3")
        assertEquals("0.6", s.sum.s())
        assertEquals("0.2", s.mean.s())
        assertEquals("0.2", s.median.s())
    }

    @Test fun oneNumberHasNoSampleDeviation() {
        val s = of("7")
        assertEquals("0", s.populationSd.s())
        assertNull(s.sampleSd)
        assertNull(Statistics.summarize(emptyList()))
    }
}
