package org.solovyev.android.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.history.RoomHistory
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.latex.LatexMapper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.memory.Memory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

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
    private val memory: Memory,
    private val notifier: Notifier,
    private val appPreferences: AppPreferences
) : ViewModel() {
    private var liveEvalJob: Job? = null
    private var lastLiveKind: LiveKind = LiveKind.Empty
    private var lastLiveExpression: String = ""

    private enum class LiveKind {
        Empty,
        Number,
        Incomplete,
        Expression
    }

    // Expose editor state
    val editorState: StateFlow<EditorState> = editor.stateFlow

    // Expose display state
    val displayState: StateFlow<DisplayState> = display.stateFlow

    // Expose notification events for UI to observe
    val notificationEvents = notifier.events

    // LaTeX mode state
    private val _latexMode = MutableStateFlow(false)
    val latexMode: StateFlow<Boolean> = _latexMode.asStateFlow()

    // Live result preview (Google Calculator style)
    private val _previewResult = MutableStateFlow<String?>(null)
    val previewResult: StateFlow<String?> = _previewResult.asStateFlow()

    // Live Unit Hint
    private val _unitHint = MutableStateFlow<String?>(null)
    val unitHint: StateFlow<String?> = _unitHint.asStateFlow()

    // Recent history for chips
    val recentHistory: StateFlow<List<HistoryState>> = roomHistory.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize display to listen to calculator events
        display.init()
        
        viewModelScope.launch {
            editor.stateFlow.collect { state ->
                // Check for unit pattern "Number Unit"
                val text = state.text
                val match = Regex("""(\d+(?:\.\d+)?)\s*([a-zA-Z]+)$""").find(text)
                if (match != null) {
                    val (valueStr, unitStr) = match.destructured
                    val value = valueStr.toDoubleOrNull()
                    if (value != null) {
                        val hint = when (unitStr.lowercase()) {
                            "km" -> "≈ ${(value * 0.621371).format(2)} mi"
                            "mi" -> "≈ ${(value * 1.60934).format(2)} km"
                            "kg" -> "≈ ${(value * 2.20462).format(2)} lb"
                            "lb" -> "≈ ${(value * 0.453592).format(2)} kg"
                            "c" -> "≈ ${(value * 9/5 + 32).format(1)} F"
                            "f" -> "≈ ${((value - 32) * 5/9).format(1)} C"
                            "m" -> "≈ ${(value * 3.28084).format(2)} ft"
                            "ft" -> "≈ ${(value * 0.3048).format(2)} m"
                            "inch", "in" -> "≈ ${(value * 2.54).format(2)} cm"
                            "cm" -> "≈ ${(value * 0.393701).format(2)} in"
                            else -> null
                        }
                        _unitHint.value = hint
                    } else {
                        _unitHint.value = null
                    }
                } else {
                    _unitHint.value = null
                }
            }
        }

        // Observe LaTeX mode preference
        viewModelScope.launch {
            appPreferences.gui.latexMode.collect { enabled ->
                _latexMode.value = enabled
            }
        }

        // Observe editor changes to trigger calculations and live preview
        viewModelScope.launch {
            editor.changedEvents.collect { event ->
                if (!_latexMode.value && calculator.isCalculateOnFly()) {
                    val classification = LiveInputClassifier.classify(event.newState.text.toString())
                    liveEvalJob?.cancel()
                    when (classification) {
                        is LiveInputClassifier.Result.Empty -> {
                            display.setState(DisplayState.empty())
                            _previewResult.value = null
                            lastLiveKind = LiveKind.Empty
                            lastLiveExpression = ""
                        }
                        is LiveInputClassifier.Result.NumberOnly -> {
                            display.setState(DisplayState(text = classification.normalized, valid = true, sequence = event.newState.sequence))
                            _previewResult.value = null
                            lastLiveKind = LiveKind.Number
                            lastLiveExpression = ""
                        }
                        is LiveInputClassifier.Result.Incomplete -> {
                            if (lastLiveKind != LiveKind.Incomplete) {
                                display.setState(DisplayState.empty())
                            }
                            _previewResult.value = null
                            lastLiveKind = LiveKind.Incomplete
                            lastLiveExpression = ""
                        }
                        is LiveInputClassifier.Result.Expression -> {
                            if (classification.normalized != lastLiveExpression) {
                                if (lastLiveKind != LiveKind.Expression) {
                                    display.setState(DisplayState.empty())
                                }
                                lastLiveKind = LiveKind.Expression
                                lastLiveExpression = classification.normalized
                                liveEvalJob = viewModelScope.launch {
                                    delay(400)
                                    val current = LiveInputClassifier.classify(editor.state.text.toString())
                                    if (current is LiveInputClassifier.Result.Expression &&
                                        current.normalized == classification.normalized
                                    ) {
                                        calculator.evaluate(
                                            JsclOperation.numeric,
                                            classification.normalized,
                                            editor.state.sequence
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Update live preview (even if calculate-on-fly is off)
                    if (!_latexMode.value && event.newState.text.isNotEmpty()) {
                        updatePreview(event.newState.text)
                    } else {
                        _previewResult.value = null
                    }
                }
            }
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    /**
     * Attempts to evaluate the current expression for a live preview.
     * This runs synchronously and catches all errors gracefully.
     */
    private fun updatePreview(expression: String) {
        viewModelScope.launch {
            try {
                val result = calculator.evaluateForPreview(expression)
                // Only show preview if it's different from the input
                _previewResult.value = if (result != expression && result.isNotEmpty()) result else null
            } catch (e: Exception) {
                _previewResult.value = null
            }
        }
    }

    // --- Input Operations ---

    /**
     * Transforms input to LaTeX if LaTeX mode is enabled.
     */
    private fun transformForLatex(input: String): String {
        return if (_latexMode.value) LatexMapper.toLatex(input) else input
    }

    fun onDigitPressed(digit: String) {
        editor.insert(transformForLatex(digit))
    }

    fun onOperatorPressed(operator: String) {
        editor.insert(transformForLatex(operator))
    }

    fun onClear() {
        editor.clear()
        display.setState(DisplayState.empty())
        _previewResult.value = null
        _unitHint.value = null
    }

    fun onBackspace(): Boolean {
        return editor.erase()
    }

    fun onEquals() {
        // In LaTeX mode, equals does nothing (no calculation)
        if (_latexMode.value) return
        calculator.evaluate(JsclOperation.numeric, editor.state.text, editor.state.sequence)
    }

    /**
     * Simplifies the current expression algebraically.
     * E.g., "2x + x" -> "3*x"
     */
    fun onSimplify() {
        if (_latexMode.value) return
        calculator.evaluate(JsclOperation.simplify, editor.state.text, editor.state.sequence)
    }

    fun onParenthesis(paren: String) {
        editor.insert(transformForLatex(paren))
    }

    // --- Cursor Operations ---

    fun moveCursorLeft() {
        editor.moveCursorLeft()
    }

    fun moveCursorRight() {
        editor.moveCursorRight()
    }

    fun moveCursorToStart() {
        editor.setCursorOnStart()
    }

    fun moveCursorToEnd() {
        editor.setCursorOnEnd()
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

    fun memorySubtract() {
        val result = display.getState().text
        if (result.isNotEmpty()) {
            viewModelScope.launch {
                memory.subtract(result)
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
        val normalized = normalizeMultiplication(text)
        editor.setText(normalized)
    }

    fun insert(text: String) {
        editor.insert(transformForLatex(text))
    }

    // --- Special ---

    fun onSpecialClick(action: String) {
        editor.insert(transformForLatex(action))
    }

    fun setLatexMode(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.gui.setLatexMode(enabled)
        }
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

    private fun normalizeMultiplication(text: String): String {
        if (text.indexOf('x', ignoreCase = true) == -1) return text
        var normalized = text
        normalized = normalized.replace(Regex("(\\d)\\s*[xX]\\s*(\\d)"), "$1×$2")
        normalized = normalized.replace(Regex("(\\d)\\s*[xX]\\s*\\("), "$1×(")
        normalized = normalized.replace(Regex("\\)\\s*[xX]\\s*(\\d)"), ")×$1")
        normalized = normalized.replace(Regex("\\)\\s*[xX]\\s*\\("), ")×(")
        return normalized
    }
}
