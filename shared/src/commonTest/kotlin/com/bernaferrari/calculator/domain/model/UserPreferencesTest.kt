package com.bernaferrari.calculator.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferencesTest {
    @Test fun theOldDotsSwitchCarriesOverToFlickHints() {
        assertEquals(FlickHints.DOTS, UserPreferences().hints)
        assertEquals(FlickHints.NONE, UserPreferences(showFlickDots = false).hints)
        // Once chosen, the new setting wins over the old switch.
        assertEquals(FlickHints.LABELS, UserPreferences(showFlickDots = false, flickHints = FlickHints.LABELS).hints)
    }
}
