package com.bernaferrari.calculator.ui.calculator

import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.library.CONSTANTS
import com.bernaferrari.calculator.domain.math.UserFunction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Everything a key or flick can do, by id: the keys themselves and built-in functions
 * ("key:SIN"), constants ("const:c"), the person's functions ("fn:tip"), or nothing ("none").
 * Ids are what settings store, by slot ("D7.up", "PERCENT.tap").
 */
object FlickCatalog {
    const val NONE = "none"

    /** The keys themselves, for a key's tap (or a flick): digits, operators, C, ⌫, =… */
    val basics: List<Pair<String, CustomFlick>> = listOf(
        Key.D7 to "7", Key.D8 to "8", Key.D9 to "9", Key.D4 to "4", Key.D5 to "5", Key.D6 to "6",
        Key.D1 to "1", Key.D2 to "2", Key.D3 to "3", Key.D0 to "0", Key.DOT to ".",
        Key.PLUS to "+", Key.MINUS to "−", Key.TIMES to "×", Key.DIVIDE to "÷", Key.PERCENT to "%",
        Key.PARENS to "( )", Key.CLEAR to "C", Key.DELETE to "⌫", Key.EQUALS to "=", Key.UNDO to "↶", Key.REDO to "↷",
    ).map { (key, label) -> "key:${key.name}" to CustomFlick(label, key = key) }

    val builtIn: List<Pair<String, CustomFlick>> = listOf(
        Key.SIN to "sin", Key.COS to "cos", Key.TAN to "tan",
        Key.ASIN to "sin⁻¹", Key.ACOS to "cos⁻¹", Key.ATAN to "tan⁻¹",
        Key.LN to "ln", Key.EXP to "eˣ", Key.LOG to "log", Key.POW10 to "10ˣ",
        Key.SQUARE to "x²", Key.SQRT to "√", Key.CUBE to "x³", Key.CBRT to "∛",
        Key.POWER to "xʸ", Key.ROOT to "ʸ√x", Key.RECIPROCAL to "1/x",
        Key.FACTORIAL to "x!", Key.ABS to "|x|",
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
        val all = (basics + builtIn + constants() + functions(functions)).toMap()
        return buildMap {
            ids.forEach { (slot, id) ->
                when {
                    id == NONE -> put(slot, null)
                    id in all -> put(slot, all.getValue(id))
                }
            }
        }
    }

    private const val FORMAT = "calcula.keys"

    /** The layout as text to share or keep: `{"format":"calcula.keys","version":1,"keys":{"D7.up":"key:SIN"}}`. */
    fun export(ids: Map<String, String>): String = buildJsonObject {
        put("format", FORMAT)
        put("version", 1)
        put("keys", buildJsonObject { ids.entries.sortedBy { it.key }.forEach { (slot, id) -> put(slot, id) } })
    }.toString()

    /**
     * Reads a layout back, keeping only slots that exist on [slots] and choices this version knows
     * (your functions are kept even when not written yet here: they light up once they are).
     * Null when the text isn't a layout at all.
     */
    fun import(text: String, slots: Set<String>): Map<String, String>? {
        val root = runCatching { Json.parseToJsonElement(text.trim()) }.getOrNull() as? JsonObject ?: return null
        val keys = (root["keys"] as? JsonObject) ?: root.takeIf { "format" !in it } ?: return null
        val known = (basics + builtIn + constants()).map { it.first }.toSet()
        val read = buildMap {
            keys.forEach { (slot, value) ->
                val id = (value as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return@forEach
                if (slot in slots && (id == NONE || id in known || id.startsWith("fn:"))) put(slot, id)
            }
        }
        return read.takeIf { it.isNotEmpty() || keys.isEmpty() }
    }
}
