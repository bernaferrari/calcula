package org.solovyev.android.calculator.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.buttons.CppSpecialButton
import org.solovyev.android.calculator.ui.compose.components.KeyboardActions
import javax.inject.Inject

@HiltViewModel
class CalculatorComposeViewModel @Inject constructor(
    private val editor: Editor,
    private val display: Display,
    private val keyboard: Keyboard,
    private val calculator: Calculator
) : ViewModel(), KeyboardActions {

    val displayState: StateFlow<DisplayState> = display.stateFlow
    val editorState: StateFlow<EditorState> = editor.stateFlow
    private var lastEditorText: String? = null

    init {
        display.init()
        viewModelScope.launch {
            calculator.initAsync()
        }
        viewModelScope.launch {
            editor.stateFlow.collect { state ->
                val text = state.getTextString()
                val previous = lastEditorText
                lastEditorText = text
                if (text != previous && calculator.isCalculateOnFly()) {
                    calculator.evaluate()
                }
            }
        }
    }

    fun onEditorTextChange(text: String, selection: Int) {
        editor.setText(text, selection)
        if (calculator.isCalculateOnFly()) {
            calculator.evaluate()
        }
    }

    fun onEditorSelectionChange(selection: Int) {
        editor.setSelection(selection)
    }

    fun onCopyResult() {
        display.copy()
    }

    override fun onNumberClick(number: String) {
        keyboard.buttonPressed(number)
    }

    override fun onOperatorClick(operator: String) {
        keyboard.buttonPressed(operator)
    }

    override fun onFunctionClick(function: String) {
        keyboard.buttonPressed(function)
    }

    override fun onSpecialClick(action: String) {
        keyboard.buttonPressed(action)
    }

    override fun onClear() {
        keyboard.buttonPressed(CppSpecialButton.clear.action)
    }

    override fun onDelete() {
        keyboard.buttonPressed(CppSpecialButton.erase.action)
    }

    override fun onEquals() {
        val currentDisplay = display.stateFlow.value
        val currentEditor = editor.stateFlow.value
        if (!currentDisplay.valid ||
            currentDisplay.sequence != currentEditor.sequence ||
            currentDisplay.text.isEmpty()
        ) {
            calculator.evaluate()
            return
        }
        keyboard.buttonPressed(CppSpecialButton.equals.action)
    }

    override fun onMemoryRecall() {
        keyboard.buttonPressed(CppSpecialButton.memory.action)
    }

    override fun onMemoryPlus() {
        keyboard.buttonPressed(CppSpecialButton.memory_plus.action)
    }

    override fun onMemoryMinus() {
        keyboard.buttonPressed(CppSpecialButton.memory_minus.action)
    }

    override fun onMemoryClear() {
        keyboard.buttonPressed(CppSpecialButton.memory_clear.action)
    }

    override fun onCursorLeft() {
        keyboard.buttonPressed(CppSpecialButton.cursor_left.action)
    }

    override fun onCursorRight() {
        keyboard.buttonPressed(CppSpecialButton.cursor_right.action)
    }

    override fun onCursorToStart() {
        keyboard.buttonPressed(CppSpecialButton.cursor_to_start.action)
    }

    override fun onCursorToEnd() {
        keyboard.buttonPressed(CppSpecialButton.cursor_to_end.action)
    }

    override fun onCopy() {
        keyboard.buttonPressed(CppSpecialButton.copy.action)
    }

    override fun onPaste() {
        keyboard.buttonPressed(CppSpecialButton.paste.action)
    }

    override fun onOpenVars() {
        keyboard.buttonPressed(CppSpecialButton.vars.action)
    }

    override fun onOpenFunctions() {
        keyboard.buttonPressed(CppSpecialButton.functions.action)
    }

    override fun onOpenHistory() {
        keyboard.buttonPressed(CppSpecialButton.history.action)
    }
}
