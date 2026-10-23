package com.bernaferrari.calculator.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferencesTest {
    @Test fun labelsUntilTheGestureIsKnownThenDots() {
        assertEquals(FlickHints.LABELS, UserPreferences().hints)
        assertEquals(FlickHints.LABELS, UserPreferences(flicksUsed = FLICKS_LEARNED - 1).hints)
        assertEquals(FlickHints.DOTS, UserPreferences(flicksUsed = FLICKS_LEARNED).hints)
    }

    @Test fun aChosenStyleAlwaysWins() {
        assertEquals(FlickHints.LABELS, UserPreferences(flickHints = FlickHints.LABELS, flicksUsed = 50).hints)
        assertEquals(FlickHints.NONE, UserPreferences(flickHints = FlickHints.NONE).hints)
    }
}
