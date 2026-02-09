@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Keyboard mode - unified for all modes now
 */
enum class KeyboardMode {
    SIMPLE,
    ENGINEER,
    MODERN,
    MINIMAL
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
    fun onMemoryRecall()
    fun onMemoryPlus()
    fun onMemoryMinus()
    fun onMemoryClear()
    fun onCursorLeft()
    fun onCursorRight()
    fun onCursorToStart()
    fun onCursorToEnd()
    fun onCopy()
    fun onPaste()
    fun onOpenVars()
    fun onOpenFunctions()
    fun onOpenHistory()
}

/**
 * Unified calculator keyboard - all modes now use the same clean implementation.
 * The mode parameter is kept for backward compatibility but ignored.
 */
@Composable
fun CalculatorKeyboard(
    mode: KeyboardMode = KeyboardMode.ENGINEER,
    actions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    // All modes now use the unified, clean keyboard
    UnifiedCalculatorKeyboard(
        actions = actions,
        modifier = modifier
    )
}
