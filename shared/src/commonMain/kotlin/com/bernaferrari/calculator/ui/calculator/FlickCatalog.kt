package com.bernaferrari.calculator.ui.calculator

import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.library.CONSTANTS
import com.bernaferrari.calculator.domain.math.UserFunction

/**
 * Everything a flick can do, by id: built-in functions ("key:SIN"), constants ("const:c"),
 * the person's functions ("fn:tip"), or nothing ("none"). Ids are what settings store.
 */
object FlickCatalog {
    const val NONE = "none"

    val builtIn: List<Pair<String, CustomFlick>> = listOf(
        Key.SIN to "sin", Key.COS to "cos", Key.TAN to "tan",
        Key.ASIN to "sin⁻¹", Key.ACOS to "cos⁻¹", Key.ATAN to "tan⁻¹",
        Key.LN to "ln", Key.EXP to "eˣ", Key.LOG to "log", Key.POW10 to "10ˣ",
        Key.SQUARE to "x²", Key.SQRT to "√", Key.CUBE to "x³", Key.CBRT to "∛",
        Key.POWER to "xʸ", Key.ROOT to "ʸ√x", Key.RECIPROCAL to "1/x",
        Key.FACTORIAL to "x!", Key.ABS to "|x|", Key.PERCENT to "%",
        Key.PI to "π", Key.E to "e",
        Key.LPAREN to "(", Key.RPAREN to ")", Key.DOUBLE_ZERO to "00", Key.TRIPLE_ZERO to "000",
    ).map { (key, label) -> "key:${key.name}" to CustomFlick(label, key = key) }

    fun constants(): List<Pair<String, CustomFlick>> =
        CONSTANTS.map { "const:${it.id}" to CustomFlick(it.plainSymbol, insert = it.value) }

    fun functions(functions: List<UserFunction>): List<Pair<String, CustomFlick>> =
        functions.map { "fn:${it.name}" to CustomFlick(it.name, insert = "${it.name}(") }

    /** The flick an id stands for; null for "none". Unknown ids (a deleted function) are dropped. */
    fun resolve(ids: Map<String, String>, functions: List<UserFunction>): Map<String, CustomFlick?> {
        if (ids.isEmpty()) return emptyMap()
        val all = (builtIn + constants() + functions(functions)).toMap()
        return buildMap {
            ids.forEach { (slot, id) ->
                when {
                    id == NONE -> put(slot, null)
                    id in all -> put(slot, all.getValue(id))
                }
            }
        }
    }
}
