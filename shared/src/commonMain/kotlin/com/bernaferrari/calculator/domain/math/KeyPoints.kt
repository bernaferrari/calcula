package com.bernaferrari.calculator.domain.math

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/** A point worth knowing on a graph: where it crosses zero, turns, meets the y axis or another curve. */
data class KeyPoint(val x: Double, val y: Double, val kind: Kind) {
    enum class Kind { ROOT, MAXIMUM, MINIMUM, Y_INTERCEPT, INTERSECTION }
}

/**
 * Finds the key points of functions between [xMin] and [xMax], the way a student would mark
 * them by hand: sample, spot where the value changes sign (a root) or the slope does (a turn),
 * then home in. Asymptotes also change sign, so a "root" whose value never gets small is dropped.
 */
object KeyPoints {
    private const val SAMPLES = 480

    fun of(f: Plot, xMin: Double, xMax: Double): List<KeyPoint> {
        val step = (xMax - xMin) / SAMPLES
        val xs = DoubleArray(SAMPLES + 1) { xMin + it * step }
        val ys = DoubleArray(SAMPLES + 1) { f.at(xs[it]) }
        val scale = scale(ys)
        val points = mutableListOf<KeyPoint>()
        roots(f, xs, ys, scale).mapTo(points) { KeyPoint(it, 0.0, KeyPoint.Kind.ROOT) }
        for (i in 1 until SAMPLES) {
            val a = ys[i - 1]
            val b = ys[i]
            val c = ys[i + 1]
            if (!a.isFinite() || !b.isFinite() || !c.isFinite()) continue
            val rising = b - a
            val falling = c - b
            if (rising > 0 && falling < 0 || rising < 0 && falling > 0) {
                val max = rising > 0
                val x = turn(f, xs[i - 1], xs[i + 1], max)
                val y = f.at(x)
                // A spike towards an asymptote turns too; real turns stay within reach of the view.
                if (y.isFinite() && abs(y) < scale * 1e3) {
                    points += KeyPoint(x, clean(y, scale), if (max) KeyPoint.Kind.MAXIMUM else KeyPoint.Kind.MINIMUM)
                }
            }
        }
        if (0.0 in xMin..xMax) {
            val y = f.at(0.0)
            if (y.isFinite()) points += KeyPoint(0.0, clean(y, scale), KeyPoint.Kind.Y_INTERCEPT)
        }
        return dedupe(points, step)
    }

    /** Where [f] and [g] meet: the roots of their difference. */
    fun intersections(f: Plot, g: Plot, xMin: Double, xMax: Double): List<KeyPoint> {
        val d = Plot { x -> f.at(x) - g.at(x) }
        val step = (xMax - xMin) / SAMPLES
        val xs = DoubleArray(SAMPLES + 1) { xMin + it * step }
        val ys = DoubleArray(SAMPLES + 1) { d.at(xs[it]) }
        val fs = DoubleArray(SAMPLES + 1) { f.at(xs[it]) }
        val scale = scale(fs)
        return dedupe(roots(d, xs, ys, scale(ys)).map { x -> KeyPoint(x, clean(f.at(x), scale), KeyPoint.Kind.INTERSECTION) }, step)
    }

    private fun roots(f: Plot, xs: DoubleArray, ys: DoubleArray, scale: Double): List<Double> {
        val roots = mutableListOf<Double>()
        for (i in 0 until xs.lastIndex) {
            val a = ys[i]
            val b = ys[i + 1]
            if (!a.isFinite() || !b.isFinite()) continue
            if (a == 0.0) { roots += xs[i]; continue }
            if ((a > 0) == (b > 0) || b == 0.0) continue
            var lo = xs[i]
            var hi = xs[i + 1]
            var flo = a
            repeat(60) {
                val mid = (lo + hi) / 2
                val fm = f.at(mid)
                if ((fm > 0) == (flo > 0)) { lo = mid; flo = fm } else hi = mid
            }
            val x = (lo + hi) / 2
            val y = f.at(x)
            if (y.isFinite() && abs(y) < scale * 1e-6) roots += x
        }
        if (ys.last() == 0.0) roots += xs.last()
        return roots
    }

    /** Golden-section search for the top (or bottom) of a turn between [a] and [b]. */
    private fun turn(f: Plot, a0: Double, b0: Double, max: Boolean): Double {
        val ratio = (sqrt(5.0) - 1) / 2
        var a = a0
        var b = b0
        repeat(60) {
            val c = b - (b - a) * ratio
            val d = a + (b - a) * ratio
            val fc = f.at(c)
            val fd = f.at(d)
            if ((fc > fd) == max) b = d else a = c
        }
        return (a + b) / 2
    }

    /** How big this function's values are, so "small" means small for it. */
    private fun scale(ys: DoubleArray): Double {
        var m = 0.0
        for (y in ys) if (y.isFinite()) m = max(m, abs(y))
        return max(m, 1e-12)
    }

    /** Rounding noise around zero reads as zero: sin(π) is 0, not 1.2E−16. */
    private fun clean(y: Double, scale: Double) = if (abs(y) < scale * 1e-9) 0.0 else y

    private fun dedupe(points: List<KeyPoint>, step: Double): List<KeyPoint> {
        val kept = mutableListOf<KeyPoint>()
        for (p in points.sortedBy { it.kind.ordinal }) {
            if (kept.none { abs(it.x - p.x) < step && abs(it.y - p.y) < step }) kept += p
        }
        return kept.sortedBy { it.x }
    }
}

/**
 * The exact value behind a decimal, when it's one a student would recognise: a fraction with a
 * small denominator, or one of π (1/3, 3π/4, −π). Null for whole numbers, which need no help,
 * and for anything else.
 */
object Exact {
    fun recognize(value: Double, tolerance: Double = 1e-7): String? {
        if (!value.isFinite() || abs(value - kotlin.math.round(value)) < 1e-12) return null
        val sign = if (value < 0) "−" else ""
        val v = abs(value)
        for (q in 1..12) {
            val p = kotlin.math.round(v / kotlin.math.PI * q).toLong()
            if (p > 0 && gcd(p, q.toLong()) == 1L && abs(v - p * kotlin.math.PI / q) < tolerance * max(1.0, v)) {
                val top = if (p == 1L) "π" else "${p}π"
                return sign + if (q == 1) top else "$top/$q"
            }
        }
        for (q in 2..12) {
            val p = kotlin.math.round(v * q).toLong()
            if (p > 0 && gcd(p, q.toLong()) == 1L && abs(v - p.toDouble() / q) < tolerance * max(1.0, v)) return "$sign$p/$q"
        }
        return null
    }

    private tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
}
