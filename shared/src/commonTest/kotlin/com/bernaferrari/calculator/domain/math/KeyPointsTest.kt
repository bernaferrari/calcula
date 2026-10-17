package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.math.KeyPoint.Kind
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeyPointsTest {
    private fun of(text: String, from: Double, to: Double) = KeyPoints.of(Plot.compile(text)!!, from, to)

    private fun List<KeyPoint>.has(kind: Kind, x: Double, y: Double) =
        assertTrue(any { it.kind == kind && abs(it.x - x) < 1e-6 && abs(it.y - y) < 1e-6 }, "no $kind at ($x, $y) in $this")

    @Test fun parabola() {
        val points = of("x²−4", -5.0, 5.0)
        points.has(Kind.ROOT, -2.0, 0.0)
        points.has(Kind.ROOT, 2.0, 0.0)
        points.has(Kind.MINIMUM, 0.0, -4.0)
        // The minimum and the y-intercept are the same point: shown once.
        assertEquals(3, points.size)
    }

    @Test fun sine() {
        val points = of("sin(x)", -1.0, 7.0)
        points.has(Kind.ROOT, PI, 0.0)
        points.has(Kind.ROOT, 2 * PI, 0.0)
        points.has(Kind.MAXIMUM, PI / 2, 1.0)
        points.has(Kind.MINIMUM, 3 * PI / 2, -1.0)
        points.has(Kind.ROOT, 0.0, 0.0) // also the y-intercept, shown once
    }

    @Test fun asymptotesAreNotRoots() {
        val points = of("1÷x", -5.0, 5.0)
        assertTrue(points.none { it.kind == Kind.ROOT || it.kind == Kind.MAXIMUM || it.kind == Kind.MINIMUM }, "$points")
        assertTrue(of("tan(x)", -5.0, 5.0).filter { it.kind == Kind.ROOT }.all { abs(it.x / PI - kotlin.math.round(it.x / PI)) < 1e-6 })
    }

    @Test fun intersections() {
        val meets = KeyPoints.intersections(Plot.compile("x²")!!, Plot.compile("x+2")!!, -5.0, 5.0)
        meets.has(Kind.INTERSECTION, -1.0, 1.0)
        meets.has(Kind.INTERSECTION, 2.0, 4.0)
        assertEquals(2, meets.size)
    }
}

class ExactTest {
    @Test fun recognisesPiAndFractions() {
        assertEquals("π/2", Exact.recognize(PI / 2))
        assertEquals("π/2", Exact.recognize(1.5707963396996787)) // a golden-section maximum
        assertEquals("−3π/4", Exact.recognize(-3 * PI / 4))
        assertEquals("π", Exact.recognize(PI))
        assertEquals("1/3", Exact.recognize(1.0 / 3))
        assertEquals("−5/2", Exact.recognize(-2.5))
        assertEquals(null, Exact.recognize(2.0))
        assertEquals(null, Exact.recognize(kotlin.math.sqrt(2.0)))
    }
}
