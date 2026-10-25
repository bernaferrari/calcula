package com.bernaferrari.calculator.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferencesTest {
    @Test fun labelsRemainTheDefault() {
        assertEquals(FlickHints.LABELS, UserPreferences().hints)
    }

    @Test fun oldAutomaticCountersCannotHideLabels() {
        val json = Json { ignoreUnknownKeys = true }
        for (flicks in listOf(0, 9, 10, 50, 1000)) {
            val prefs = json.decodeFromString<UserPreferences>("""{"flicksUsed":$flicks}""")
            assertEquals(FlickHints.LABELS, prefs.hints)
        }
    }

    @Test fun aChosenStyleAlwaysWins() {
        for (style in FlickHints.entries) {
            assertEquals(style, UserPreferences(flickHints = style).hints)
        }
        val prefs = Json { ignoreUnknownKeys = true }.decodeFromString<UserPreferences>(
            """{"flickHints":"DOTS","flicksUsed":1000}""",
        )
        assertEquals(FlickHints.DOTS, prefs.hints)
    }
}
