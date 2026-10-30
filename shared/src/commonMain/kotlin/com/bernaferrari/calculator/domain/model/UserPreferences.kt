package com.bernaferrari.calculator.domain.model

import com.bernaferrari.calculator.domain.programmer.ProgEntry
import com.bernaferrari.calculator.domain.math.UserFunction
import com.bernaferrari.calculator.domain.programmer.Base
import com.bernaferrari.calculator.domain.programmer.WordSize
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** The accent family; null means the platform default (wallpaper where available). */
    val palette: AppPalette? = null,
    /** How the family is spread across the scheme: calm, muted, vivid or playful. */
    val colorStyle: ColorStyle = ColorStyle.TONAL_SPOT,
    val showHistory: Boolean = true,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    /** Fixed number of decimals for results, or [AUTO_DECIMALS] for up to 15 significant digits. */
    val decimalPlaces: Int = AUTO_DECIMALS,
    val decimalSeparator: DecimalSeparator = DecimalSeparator.AUTO,
    val hapticFeedback: Boolean = true,
    val soundEnabled: Boolean = false,
    /** How keys show the functions a flick reaches, once chosen in Settings; see [hints]. */
    val flickHints: FlickHints? = null,
    val reduceMotion: Boolean = false,
    /** Whether the person has already flicked a key, so the swipe tip can go away for good. */
    val swipeHintSeen: Boolean = false,
    /** The keypad's height relative to the default, set by dragging the bar above it. */
    val keypadScale: Float = 1f,
    /** The graph keypad's height relative to its default; separate, since the graph wants its plot. */
    val graphKeypadScale: Float = 1f,
    /** A row of memory keys (MC M+ M− MR) above the keypad, for people who work that way. */
    val memoryKeys: Boolean = false,
    /** Stored values, newest last; the memory keys work on the first. */
    val memories: List<Memory> = emptyList(),
    /** Functions of x the person defined, like `tip(x) = x×0.18`. */
    val functions: List<UserFunction> = emptyList(),
    /** Flicks the person reassigned: "D7.up" to an action id like "const:c" or "fn:tip". */
    val flicks: Map<String, String> = emptyMap(),
    /** Reverse Polish entry: ↵ pushes onto a stack and operators act at once. */
    val rpn: Boolean = false,
    val programmerBase: Base = Base.DEC,
    val programmerWord: WordSize = WordSize.QWORD,
    /** Decimal in the programmer reads the top bit as a sign. */
    val programmerSigned: Boolean = true,
    /** The optional interactive bit grid; keep the number and history clear until requested. */
    val programmerShowBits: Boolean = false,
    /** The programmer keypad's size, as a share of its default height: set by dragging the row above it. */
    val programmerKeypadScale: Float = 1f,
    /** The programmer's recent calculations, kept between visits. */
    val programmerTape: List<ProgEntry> = emptyList(),
    /** How results are written: as is, or scientific (`1.23E6`) or engineering (`12.3E3`). */
    val notation: Notation = Notation.NORMAL,
    /** The numbers on the statistics screen, kept between visits. */
    val statsData: List<String> = emptyList(),
    /** How the calculator gets used, for the "Your Calcula" card. Never leaves the device. */
    val usage: Usage = Usage(),
)

/** Labels stay visible by default; only an explicit Settings choice changes the hint style. */
val UserPreferences.hints: FlickHints get() = flickHints ?: FlickHints.LABELS

/** How a key shows its flick functions: their names above and below, two dots, or nothing. */
enum class FlickHints { LABELS, DOTS, NONE }

/**
 * Material Kolor's ways of turning one seed into a whole scheme. People choose by look, not by
 * name: tonal spot keeps everything in the seed's family, neutral quiets it, vibrant turns it up,
 * expressive brings in neighbouring hues.
 */
enum class ColorStyle { TONAL_SPOT, NEUTRAL, VIBRANT, EXPRESSIVE }

const val AUTO_DECIMALS = -1

enum class Notation { NORMAL, SCIENTIFIC, ENGINEERING }

/** Key presses by key (taps and flicks alike), flicks, finished calculations and days used. */
@Serializable
data class Usage(
    val keys: Map<String, Int> = emptyMap(),
    val flicks: Int = 0,
    val calculations: Int = 0,
    val days: Int = 0,
    /** The last day it was used, as days since 1970, to count each day once. */
    val lastDay: Long = 0,
    /** When counting started, in epoch millis. */
    val since: Long = 0,
) {
    /** These counts added in, on [day] at [now]. */
    fun plus(keys: Map<String, Int>, flicks: Int, calculations: Int, day: Long, now: Long) = copy(
        keys = (this.keys.keys + keys.keys).associateWith { (this.keys[it] ?: 0) + (keys[it] ?: 0) },
        flicks = this.flicks + flicks,
        calculations = this.calculations + calculations,
        days = if (day != lastDay) days + 1 else days,
        lastDay = day,
        since = if (since == 0L) now else since,
    )
}

/** One stored value, as a number in the expression language, with an optional name. */
@Serializable
data class Memory(val value: String, val label: String? = null)

/** AUTO follows the locale; DOT and COMMA force `1,234.5` or `1.234,5`. */
enum class DecimalSeparator { AUTO, DOT, COMMA }

/**
 * The color families on offer, each a Material Kolor seed. [WALLPAPER] follows the system's
 * wallpaper colors and only exists where the platform has them (Android 12+).
 */
enum class AppPalette(val seed: Long) {
    WALLPAPER(0xFF6366F1),
    INDIGO(0xFF6366F1),
    BLUE(0xFF1A73E8),
    TEAL(0xFF00897B),
    GREEN(0xFF34A853),
    AMBER(0xFFF9AB00),
    ORANGE(0xFFF4511E),
    PINK(0xFFD81B60),
    PURPLE(0xFF8E24AA),
    /** Retained to read preferences saved before Graphite was removed from the picker. */
    GRAPHITE(0xFF5F6368),
}
