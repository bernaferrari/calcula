package com.bernaferrari.calculator.domain.math

import com.ionspin.kotlin.bignum.integer.BigInteger

/** A fraction in lowest terms; [numerator] carries the sign. */
data class Fraction(val numerator: BigInteger, val denominator: BigInteger)

/**
 * Recognises results that are simple fractions (0.375 is ³⁄₈, 0.333… is ⅓) so they can be shown
 * as such. Results carry 40 digits, so a repeating decimal is only "equal" to its fraction to
 * within that noise; a terminating one has to match exactly, which keeps 0.33 from passing as ⅓.
 */
object Fractions {
    /** The fraction with the smallest denominator, at most [maxDenominator], equal to [x]. */
    fun of(x: Decimal, maxDenominator: Int = 1000): Fraction? {
        val negative = x.signum() < 0
        val a = x.abs()
        // x = n / d exactly, as written.
        var n: BigInteger
        var d: BigInteger
        if (a.scale() <= 0) return Fraction(x.toBigInteger(), BigInteger.ONE)
        n = a.unscaled
        d = BigInteger.TEN.pow(a.scale())
        val n0 = n
        val d0 = d
        val limit = BigInteger.fromInt(maxDenominator)
        // Continued fraction convergents h/k.
        var h0 = BigInteger.ZERO; var h1 = BigInteger.ONE
        var k0 = BigInteger.ONE; var k1 = BigInteger.ZERO
        while (!d.isZero()) {
            val (q, r) = n.divideAndRemainder(d)
            val h2 = q.multiply(h1).add(h0)
            val k2 = q.multiply(k1).add(k0)
            if (k2 > limit) break
            h0 = h1; h1 = h2; k0 = k1; k1 = k2
            n = d; d = r
            // |x − h/k| = |n0·k − h·d0| / (d0·k); close enough when below 10⁻³⁰ of it.
            val error = n0.multiply(k1).subtract(h1.multiply(d0)).abs()
            if (error.isZero() || error.multiply(TOLERANCE) < d0.multiply(k1)) {
                return Fraction(if (negative) h1.negate() else h1, k1)
            }
        }
        return null
    }

    /**
     * The fraction worth showing next to a result, or null: not for whole numbers, and only small
     * denominators for decimals that end anyway (59.97 is clearer than 5997⁄100).
     */
    fun worthShowing(x: Decimal): Fraction? {
        if (x.isInteger()) return null
        val f = of(x) ?: return null
        if (f.denominator == BigInteger.ONE) return null
        val terminates = f.denominator.let { var q = it; for (p in listOf(2, 5)) { val b = BigInteger.fromInt(p); while (q.rem(b).isZero()) q = q.divide(b) }; q == BigInteger.ONE }
        return if (!terminates || f.denominator <= BigInteger.fromInt(64)) f else null
    }

    /** `2 3/8`, `−5/6`: the whole part, then what's left; the display sets it as a fraction. */
    fun format(f: Fraction): String {
        val negative = f.numerator.signum() < 0
        val (whole, rest) = f.numerator.abs().divideAndRemainder(f.denominator)
        val sign = if (negative) "−" else ""
        val fraction = "$rest/${f.denominator}"
        return when {
            rest.isZero() -> sign + whole
            whole.isZero() -> sign + fraction
            else -> "$sign$whole $fraction"
        }
    }

    private val TOLERANCE = BigInteger.TEN.pow(30)
}
