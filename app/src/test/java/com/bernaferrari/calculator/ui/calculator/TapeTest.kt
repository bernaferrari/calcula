package com.bernaferrari.calculator.ui.calculator

import com.bernaferrari.calculator.domain.model.CalculationHistory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class TapeTest {
    private val zone = ZoneId.of("Europe/Berlin")

    @Test fun yesterdayAfterSpringClockChange() {
        assertDayLabels("2026-03-30")
    }

    @Test fun yesterdayAfterAutumnClockChange() {
        assertDayLabels("2026-10-26")
    }

    @Test fun yesterdayAcrossYearBoundary() {
        assertDayLabels("2026-01-01")
    }

    private fun assertDayLabels(date: String) {
        val today = LocalDate.parse(date)
        val history = (0L..2L).map { daysAgo ->
            CalculationHistory(
                id = daysAgo,
                expression = "1+1",
                result = "2",
                timestamp = today.minusDays(daysAgo).atTime(12, 0).atZone(zone).toInstant().toEpochMilli(),
            )
        }
        val now = today.atTime(18, 0).atZone(zone).toInstant()
        val days = buildRows(history, expanded = true, now = now, zone = zone).filterIsInstance<TapeRow.Day>()
        assertEquals(listOf(DayLabel.Today, DayLabel.Yesterday, DayLabel.Date(
            today.minusDays(2).atStartOfDay(zone).toInstant().toEpochMilli(),
        )), days.map { it.label })
        assertEquals((0L..2L).map { today.minusDays(it) }, days.map {
            Instant.ofEpochMilli(it.start).atZone(zone).toLocalDate()
        })
    }
}
