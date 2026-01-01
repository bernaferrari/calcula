package org.solovyev.android.calculator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.buttons.CppButton
import org.solovyev.android.calculator.buttons.CppSpecialButton
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.memory.Memory

/**
 * Handle button presses from UI or Widgets.
 */
class Keyboard(
    private val calculator: Calculator,
    private val editor: Editor,
    private val history: History,
    private val memory: Memory,
    private val appPreferences: AppPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun buttonPressed(action: String?): Boolean {
        if (action.isNullOrEmpty()) return false

        val specialButton = CppSpecialButton.getByAction(action)
        if (specialButton != null) {
            processSpecialButton(specialButton)
            return true
        }

        val button = CppButton.getByAction(action)
        if (button != null) {
            editor.insert(button.action)
            return true
        }

        // Default: insert as text
        editor.insert(action)
        return true
    }

    private fun processSpecialButton(button: CppSpecialButton) {
        when (button) {
            CppSpecialButton.equals -> calculator.evaluate(JsclOperation.numeric, editor.state.text, editor.state.sequence)
            CppSpecialButton.clear -> editor.clear()
            CppSpecialButton.erase -> editor.erase()
            CppSpecialButton.cursor_left -> editor.moveCursorLeft()
            CppSpecialButton.cursor_right -> editor.moveCursorRight()
            CppSpecialButton.cursor_to_start -> editor.setCursorOnStart()
            CppSpecialButton.cursor_to_end -> editor.setCursorOnEnd()
            CppSpecialButton.history_undo -> scope.launch { history.undo() }
            CppSpecialButton.copy -> displayCopy() // This needs to be handled via Notifier/Clipboard
            CppSpecialButton.memory -> { /* show memory? */ }
            CppSpecialButton.memory_plus -> {
                // Simplified memory plus logic
                // memory.add(display.state.text)
            }
            else -> {
                // Handle others as text insertion for now if they have valid action that's printable
                if (button.action.length == 1 || button.action.all { it.isLetter() }) {
                    editor.insert(button.action)
                }
            }
        }
    }

    private fun displayCopy() {
        // This is tricky as Display handles its own copy. 
        // We might want to pass Display here or have a central place for copy.
    }
}
