package com.bernaferrari.calculator.ui.calculator

import com.bernaferrari.calculator.domain.input.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeyLayoutTest {
    private val rows = keypadRows('.')
    private val slots = rows.flatten().flatMap { listOf(it.tapSlot, it.slot(true), it.slot(false)) }.toSet()

    @Test fun anyKeyCanBeRemappedOrCleared() {
        val mapped = rows.withFlicks(FlickCatalog.resolve(mapOf("PERCENT.tap" to "key:SQRT", "CLEAR.tap" to "none", "EQUALS.up" to "const:c"), emptyList()))
        val percent = mapped[0][1]
        assertEquals(Key.SQRT, percent.key)
        assertEquals("PERCENT", percent.id)
        assertTrue(mapped[0][0].blank)
        assertEquals("299792458", mapped[4][3].up?.insert)
        // The rest stays as shipped.
        assertEquals(Key.DELETE, mapped[0][2].key)
    }

    @Test fun layoutsSurviveExportAndImport() {
        val layout = mapOf("D7.up" to "key:LOG", "PARENS.tap" to "key:DELETE", "DELETE.down" to "none", "D2.up" to "fn:tip")
        assertEquals(layout, FlickCatalog.import(FlickCatalog.export(layout), slots))
    }

    @Test fun importKeepsOnlyWhatItKnows() {
        val text = """{"format":"calcula.keys","version":1,"keys":{"D7.up":"key:SIN","NOPE.tap":"key:SIN","D8.up":"key:WHAT","D9.up":3}}"""
        assertEquals(mapOf("D7.up" to "key:SIN"), FlickCatalog.import(text, slots))
        assertNull(FlickCatalog.import("hello", slots))
        assertNull(FlickCatalog.import("""{"keys":{"NOPE.tap":"key:SIN"}}""", slots))
    }
}
