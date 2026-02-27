package org.solovyev.android.calculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.MissingResourceException

val LocalCalculatorHighContrast = staticCompositionLocalOf { false }
val LocalCalculatorHapticsEnabled = staticCompositionLocalOf { true }

/**
 * Interface for all string labels used in the keyboard.
 */
interface KeyboardStrings {
    val history: String @Composable get
    val glyphBackspace: String @Composable get
    val glyphCopy: String @Composable get
    val glyphFastBack: String @Composable get
    val glyphFastForward: String @Composable get
    val glyphHistory: String @Composable get
    val glyphLeft: String @Composable get
    val glyphPaste: String @Composable get
    val glyphRedo: String @Composable get
    val glyphRight: String @Composable get
    val glyphUndo: String @Composable get
    val kbClear: String @Composable get
    val kbFunctions: String @Composable get
    val kbMemoryClear: String @Composable get
    val kbMemoryMinus: String @Composable get
    val kbMemoryPlus: String @Composable get
    val kbMemoryRecall: String @Composable get
    val kbOperators: String @Composable get
    val kbVariables: String @Composable get
    val settings: String @Composable get
    val glyphGraph: String @Composable get
}

/**
 * Interface for all icon painters used in the keyboard.
 */
interface KeyboardIcons {
    val backspace: Painter @Composable get
    val copy: Painter @Composable get
    val paste: Painter @Composable get
    val history: Painter @Composable get
    val arrowLeft: Painter @Composable get
    val arrowRight: Painter @Composable get
    val launch: Painter @Composable get
    val settings: Painter @Composable get
}

/**
 * Shared implementation using Compose Multiplatform Resources
 */
class KmpKeyboardStrings : KeyboardStrings {
    override val history: String @Composable get() = stringResource(Res.string.c_history)
    override val glyphBackspace: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_backspace))
    override val glyphCopy: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_copy))
    override val glyphFastBack: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_fast_back))
    override val glyphFastForward: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_fast_forward))
    override val glyphHistory: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_history))
    override val glyphLeft: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_left))
    override val glyphPaste: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_paste))
    override val glyphRedo: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_redo))
    override val glyphRight: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_right))
    override val glyphUndo: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_undo))
    override val kbClear: String @Composable get() = stringResource(Res.string.cpp_kb_clear)
    override val kbFunctions: String @Composable get() = stringResource(Res.string.cpp_kb_functions)
    override val kbMemoryClear: String @Composable get() = stringResource(Res.string.cpp_kb_memory_clear)
    override val kbMemoryMinus: String @Composable get() = stringResource(Res.string.cpp_kb_memory_minus)
    override val kbMemoryPlus: String @Composable get() = stringResource(Res.string.cpp_kb_memory_plus)
    override val kbMemoryRecall: String @Composable get() = stringResource(Res.string.cpp_kb_memory_recall)
    override val kbOperators: String @Composable get() = stringResource(Res.string.cpp_kb_operators)
    override val kbVariables: String @Composable get() = stringResource(Res.string.cpp_kb_variables)
    override val settings: String @Composable get() = stringResource(Res.string.cpp_settings)
    override val glyphGraph: String @Composable get() = normalizeGlyphString(stringResource(Res.string.cpp_glyph_graph))
}

private val hasBackspaceComposeResource: Boolean by lazy {
    try {
        runBlocking {
            Res.readBytes("drawable/ic_backspace_white_48dp.png")
        }
        true
    } catch (_: MissingResourceException) {
        false
    }
}

class KmpKeyboardIcons : KeyboardIcons {
    override val backspace: Painter @Composable get() = if (hasBackspaceComposeResource) {
        painterResource(Res.drawable.ic_backspace_white_48dp)
    } else {
        rememberVectorPainter(Icons.AutoMirrored.Filled.Backspace)
    }

    override val copy: Painter @Composable get() = painterResource(Res.drawable.ic_content_copy_white_48dp)
    override val paste: Painter @Composable get() = painterResource(Res.drawable.ic_content_paste_white_48dp)
    override val history: Painter @Composable get() = painterResource(Res.drawable.ic_history_white_48dp)
    override val arrowLeft: Painter @Composable get() = painterResource(Res.drawable.ic_keyboard_arrow_left_white_48dp)
    override val arrowRight: Painter @Composable get() = painterResource(Res.drawable.ic_keyboard_arrow_right_white_48dp)
    override val launch: Painter @Composable get() = painterResource(Res.drawable.ic_launch_white_48dp)
    override val settings: Painter @Composable get() = painterResource(Res.drawable.ic_settings_white_48dp)
}

val LocalKeyboardStrings = staticCompositionLocalOf<KeyboardStrings> {
    KmpKeyboardStrings()
}

val LocalKeyboardIcons = staticCompositionLocalOf<KeyboardIcons> {
    KmpKeyboardIcons()
}
