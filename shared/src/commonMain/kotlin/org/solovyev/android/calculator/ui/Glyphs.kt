package org.solovyev.android.calculator.ui

/**
 * Normalizes glyph strings (removes quotes if present).
 * Historically some glyphs were stored as quoted strings in XML.
 */
fun normalizeGlyphString(value: String): String {
    return if (value.length >= 2 && value.first() == '"' && value.last() == '"') {
        value.substring(1, value.length - 1)
    } else {
        value
    }
}
