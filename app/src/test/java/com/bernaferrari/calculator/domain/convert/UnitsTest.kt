package com.bernaferrari.calculator.domain.convert

import com.bernaferrari.calculator.domain.math.NumberFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class UnitsTest {
    private fun conv(value: String, from: String, to: String) =
        NumberFormatter.format(Units.convert(BigDecimal(value.replace("−", "-")), Units.byId(from)!!, Units.byId(to)!!))

    @Test fun exactDefinitions() {
        assertEquals("1.609344", conv("1", "mi", "km"))
        assertEquals("2.54", conv("1", "in", "cm"))
        assertEquals("0.45359237", conv("1", "lb", "kg"))
        assertEquals("3.785411784", conv("1", "gal", "l"))
        assertEquals("1024", conv("1", "MiB", "KiB"))
        assertEquals("8", conv("1", "B", "b"))
    }

    @Test fun temperatures() {
        assertEquals("212", conv("100", "c", "f"))
        assertEquals("−40", conv("−40", "f", "c"))
        assertEquals("0", conv("−273.15", "c", "k"))
        assertEquals("37", conv("98.6", "f", "c"))
    }

    @Test fun roundTripsAreStable() {
        for (unit in Units.all) {
            val (a, _) = Units.defaults(unit.category)
            val there = Units.convert(BigDecimal("123.456"), a, unit)
            assertEquals(unit.id, "123.456", NumberFormatter.format(Units.convert(there, unit, a)))
        }
    }

    @Test fun everyCategoryHasDefaultsAndUnits() {
        Category.entries.forEach { assertTrue(Units.of(it).size >= 2); Units.defaults(it) }
    }
}
