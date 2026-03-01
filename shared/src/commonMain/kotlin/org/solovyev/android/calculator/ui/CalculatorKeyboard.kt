package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import jscl.NumeralBase
import org.solovyev.android.calculator.ui.nb.NotBoringKeyboard

/**
 * Keyboard mode - different visual styles
 */
enum class KeyboardMode {
    ENGINEER,
    MODERN,
    NOT_BORING  // Andy-inspired, result-focused design
}

/**
 * Callback interface for keyboard actions
 */
interface KeyboardActions {
    fun onNumberClick(number: String)
    fun onOperatorClick(operator: String)
    fun onFunctionClick(function: String)
    fun onSpecialClick(action: String)
    fun onSimplify()
    fun onOpenSettings()
    fun onClear()
    fun onDelete()
    fun onEquals()
    fun onMemoryStore()
    fun onMemoryRecall()
    fun onMemoryPlus()
    fun onMemoryMinus()
    fun onMemoryClear()
    fun onMemoryRegisterSelected(register: String)
    fun onSetNumeralBase(base: NumeralBase)
    fun onSetBitwiseWordSize(size: Int)
    fun onSetBitwiseSigned(signed: Boolean)
    fun onSetBitwiseOverflow(overflow: Boolean)
    fun onCursorLeft()
    fun onCursorRight()
    fun onCursorToStart()
    fun onCursorToEnd()
    fun onCopy()
    fun onPaste()
    fun onOpenVars()
    fun onOpenFunctions()
    fun onOpenHistory()
    fun onOpenGraph()

    // Gesture tracking for tutorial system
    fun onSwipeUp(buttonId: String) {}
    fun onSwipeDown(buttonId: String) {}
    fun onSwipeLeft(buttonId: String) {}
    fun onSwipeRight(buttonId: String) {}
    fun onLongPress(buttonId: String) {}
    fun onDoubleTap(buttonId: String) {}
}

/**
 * Calculator keyboard - routes to appropriate implementation based on mode.
 */
@Composable
fun CalculatorKeyboard(
    mode: KeyboardMode = KeyboardMode.ENGINEER,
    actions: KeyboardActions,
    numeralBase: NumeralBase = NumeralBase.dec,
    bitwiseWordSize: Int = 64,
    bitwiseSigned: Boolean = true,
    gestureAutoActivation: Boolean = false,
    showBottomRightEqualsKey: Boolean = false,
    modifier: Modifier = Modifier,
    onSwipeUpForScientific: () -> Unit = {}
) {
    when (mode) {
        KeyboardMode.MODERN -> {
            ModernCalculatorKeyboard(
                actions = actions,
                numeralBase = numeralBase,
                bitwiseWordSize = bitwiseWordSize,
                bitwiseSigned = bitwiseSigned,
                gestureAutoActivation = gestureAutoActivation,
                showBottomRightEqualsKey = showBottomRightEqualsKey,
                modifier = modifier
            )
        }
        KeyboardMode.NOT_BORING -> {
            NotBoringKeyboard(
                actions = actions,
                onSwipeUp = onSwipeUpForScientific,
                showBottomRightEqualsKey = showBottomRightEqualsKey,
                gestureAutoActivation = gestureAutoActivation,
                modifier = modifier
            )
        }
        KeyboardMode.ENGINEER -> {
            UnifiedCalculatorKeyboard(
                actions = actions,
                numeralBase = numeralBase,
                bitwiseWordSize = bitwiseWordSize,
                bitwiseSigned = bitwiseSigned,
                gestureAutoActivation = gestureAutoActivation,
                showBottomRightEqualsKey = showBottomRightEqualsKey,
                modifier = modifier
            )
        }
    }
}
