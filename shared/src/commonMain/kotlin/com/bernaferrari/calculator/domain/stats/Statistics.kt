package com.bernaferrari.calculator.domain.stats

import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.MathContext

/** The usual summary of a list of numbers, exact where it can be (sums, means, medians). */
data class Summary(
    val count: Int,
    val sum: Decimal,
    val mean: Decimal,
    val median: Decimal,
    /** Standard deviation of the numbers as the whole population (÷ n). */
    val populationSd: Decimal,
    /** Standard deviation as a sample of something larger (÷ n − 1); null for a single number. */
    val sampleSd: Decimal?,
    val min: Decimal,
    val max: Decimal,
)

object Statistics {
    private val MC = MathContext(40)

    fun summarize(values: List<Decimal>): Summary? {
        if (values.isEmpty()) return null
        val n = values.size
        val sum = values.fold(Decimal.ZERO) { a, b -> a.add(b) }
        val mean = sum.divide(Decimal(n), MC)
        val sorted = values.sorted()
        val median = if (n % 2 == 1) sorted[n / 2] else sorted[n / 2 - 1].add(sorted[n / 2]).divide(Decimal(2), MC)
        val squares = values.fold(Decimal.ZERO) { a, x -> x.subtract(mean).let { d -> a.add(d.multiply(d, MC), MC) } }
        return Summary(
            count = n,
            sum = sum,
            mean = mean,
            median = median,
            populationSd = sqrt(squares.divide(Decimal(n), MC)),
            sampleSd = if (n > 1) sqrt(squares.divide(Decimal(n - 1), MC)) else null,
            min = sorted.first(),
            max = sorted.last(),
        )
    }

    private fun sqrt(x: Decimal): Decimal = if (x.signum() == 0) Decimal.ZERO else CalcEngine.evaluate("√(${x.toPlainString()})")
}
