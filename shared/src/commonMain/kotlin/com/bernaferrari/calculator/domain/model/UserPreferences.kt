package com.bernaferrari.calculator.domain.model

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
    /** Functions picked by flick so far, counted until [FLICKS_LEARNED]; see [hints]. */
    val flicksUsed: Int = 0,
    val reduceMotion: Boolean = false,
    /** Whether the person has already flicked a key, so the swipe tip can go away for good. */
    val swipeHintSeen: Boolean = false,
    /** The keypad's height relative to the default, set by dragging the bar above it. */
    val keypadScale: Float = 1f,
    /** The graph keypad's height relative to its default; separate, since the graph wants its plot. */
    val graphKeypadScale: Float = 1f,
    /** A row of memory keys (MC M+ M− MR) above the keypad, for people who work that way. */
    val memoryKeys: Boolean = false,
    /** What memory holds, as a number in the expression language; null when it's empty. */
    val memory: String? = null,
    /** Reverse Polish entry: ↵ pushes onto a stack and operators act at once. */
    val rpn: Boolean = false,
    val programmerBase: Base = Base.DEC,
    val programmerWord: WordSize = WordSize.QWORD,
)

/**
 * The hint style in effect: the one chosen in Settings, if any. Otherwise new people see every
 * function named on its key, and once they've flicked [FLICKS_LEARNED] times they know the
 * gesture, so the keypad quiets down to dots.
 */
val UserPreferences.hints: FlickHints get() = flickHints ?: if (flicksUsed < FLICKS_LEARNED) FlickHints.LABELS else FlickHints.DOTS

/** Flicks after which the names on the keys give way to dots, unless a style was chosen. */
const val FLICKS_LEARNED = 10

/** How a key shows its flick functions: their names above and below, two dots, or nothing. */
enum class FlickHints { LABELS, DOTS, NONE }

/**
 * Material Kolor's ways of turning one seed into a whole scheme. People choose by look, not by
 * name: tonal spot keeps everything in the seed's family, neutral quiets it, vibrant turns it up,
 * expressive brings in neighbouring hues.
 */
enum class ColorStyle { TONAL_SPOT, NEUTRAL, VIBRANT, EXPRESSIVE }

const val AUTO_DECIMALS = -1

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
    GRAPHITE(0xFF5F6368),
}
