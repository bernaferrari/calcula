package org.solovyev.android.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.history.RoomHistory
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.memory.DataStoreMemory

/**
 * Shared CalculatorViewModel that orchestrates:
 * - Editor state
 * - Display state
 * - Calculation logic
 * - History management
 * - Memory operations
 * - Notification events
 *
 * This ViewModel is intended to be used from both Android and iOS Compose UI.
 * Uses official JetBrains KMP ViewModel from org.jetbrains.androidx.lifecycle.
 */
class CalculatorViewModel(
    private val calculator: Calculator,
    private val editor: Editor,
    private val display: Display,
    private val roomHistory: RoomHistory,
    private val memory: DataStoreMemory,
    private val notifier: Notifier,
    private val appPreferences: AppPreferences
) : ViewModel() {

    // Expose editor state
    val editorState: StateFlow<EditorState> = editor.stateFlow

    // Expose display state
    val displayState: StateFlow<DisplayState> = display.stateFlow

    // Expose notification events for UI to observe
    val notificationEvents = notifier.events

    init {
        // Initialize display to listen to calculator events
        display.init()

        // Observe editor changes to trigger calculations
        viewModelScope.launch {
            editor.changedEvents.collect { event ->
                if (event.shouldEvaluate() && calculator.isCalculateOnFly()) {
                    calculator.evaluate()
                }
            }
        }
    }

    // --- Input Operations ---

    fun onDigitPressed(digit: String) {
        editor.insert(digit)
    }

    fun onOperatorPressed(operator: String) {
        editor.insert(operator)
    }

    fun onClear() {
        editor.clear()
    }

    fun onBackspace(): Boolean {
        return editor.erase()
    }

    fun onEquals() {
        calculator.evaluate(JsclOperation.numeric, editor.state.text, editor.state.sequence)
    }

    fun onParenthesis(paren: String) {
        editor.insert(paren)
    }

    // --- Cursor Operations ---

    fun moveCursorLeft() {
        editor.moveCursorLeft()
    }

    fun moveCursorRight() {
        editor.moveCursorRight()
    }

    fun onEditorSelectionChange(selection: Int) {
        editor.setSelection(selection)
    }

    // --- Copy ---

    /**
     * Returns the text to copy, or null if nothing to copy.
     * The caller should use LocalClipboard to perform the actual copy.
     */
    fun getTextToCopy(): String? = display.getTextToCopy()

    fun onCopied() {
        display.showCopiedMessage()
    }

    // --- Memory Operations ---
    // --- Memory Operations ---

    fun memoryStore() {
        val result = display.getState().text
        if (result.isNotEmpty()) {
            viewModelScope.launch {
                memory.store(result)
            }
        }
    }

    fun memoryRecall() {
        memory.recall()
    }

    fun memoryClear() {
        viewModelScope.launch {
            memory.clear()
        }
    }

    fun memoryAdd() {
        val result = display.getState().text
        if (result.isNotEmpty()) {
            viewModelScope.launch {
                memory.add(result)
            }
        }
    }

    // --- History ---

    fun addToHistory() {
        viewModelScope.launch {
            val expression = editor.state.text
            val result = display.getState().text
            if (expression.isNotEmpty() && result.isNotEmpty()) {
                roomHistory.addEntry(expression, result, editor.state.selection)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            roomHistory.clearRecent()
        }
    }

    // --- Utility ---

    fun onEditorTextChange(text: String) {
        editor.setText(text)
    }

    fun insert(text: String) {
        editor.insert(text)
    }

    // --- Special ---

    fun onSpecialClick(action: String) {
        editor.insert(action)
    }

    fun setTheme(theme: GuiTheme) {
        viewModelScope.launch {
            appPreferences.gui.setTheme(theme.id)
        }
    }

    fun setMode(mode: GuiMode) {
        viewModelScope.launch {
            appPreferences.gui.setMode(mode.id)
        }
    }

    fun setAngleUnit(unit: jscl.AngleUnit) {
        viewModelScope.launch {
            appPreferences.settings.setAngleUnit(unit.ordinal)
        }
    }

    fun setNumeralBase(base: jscl.NumeralBase) {
        viewModelScope.launch {
            appPreferences.settings.setNumeralBase(base.ordinal)
        }
    }
}
