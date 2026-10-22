package com.bernaferrari.calculator.domain.model

import com.bernaferrari.calculator.data.decodePreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class StoredPreferencesTest {
    @Test fun aRetiredStyleFallsBackWithoutLosingTheRest() {
        val prefs = decodePreferences("""{"themeMode":"DARK","palette":"AMBER","colorStyle":"RAINBOW"}""")
        assertEquals(ColorStyle.TONAL_SPOT, prefs.colorStyle)
        assertEquals(ThemeMode.DARK, prefs.themeMode)
        assertEquals(AppPalette.AMBER, prefs.palette)
    }
}
