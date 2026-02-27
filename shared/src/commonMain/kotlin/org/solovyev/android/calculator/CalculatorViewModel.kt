package org.solovyev.android.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.history.RoomHistory
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.latex.LatexMapper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.memory.Memory
import org.solovyev.android.calculator.memory.MemoryRegisterState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import jscl.NumeralBase
import kotlin.text.uppercaseChar

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

    private data class PendingHistoryCapture(
        val sequence: Long,
        val expression: String,
        val selection: Int
    )

    companion object {
        private const val LIVE_EVAL_DELAY_SIMPLE_MULTIPLY_MS = 0L
        private const val LIVE_EVAL_DELAY_SIMPLE_ARITHMETIC_MS = 0L
        private const val LIVE_EVAL_DELAY_COMPLEX_MS = 55L
        private const val RPN_STACK_LIMIT = 64
        private const val TAPE_MAX_ENTRIES = 240
        private val BINARY_FUNCTION_NAMES = setOf("and", "or", "xor", "shl", "shr")
        private val BITWISE_BINARY_OPERATORS = setOf("&", "|", "^", "<<", ">>")
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

    private val _rpnMode = MutableStateFlow(false)
    val rpnMode: StateFlow<Boolean> = _rpnMode.asStateFlow()

    private val _rpnStack = MutableStateFlow<List<String>>(emptyList())
    private val _rpnEntry = MutableStateFlow("")
    val rpnStack: StateFlow<List<String>> = combine(_rpnStack, _rpnEntry) { stack, entry ->
        if (entry.isEmpty()) stack else stack + entry
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val rpnOperationMutex = Mutex()
    private val rpnMachine = RpnMachine(
        evaluator = { expression -> calculator.evaluateForPreview(expression) },
        stackLimit = RPN_STACK_LIMIT
    )
    private var infixEditorSnapshot: EditorState? = null

    private val _calculateOnFly = MutableStateFlow(true)
    private val _tapeMode = MutableStateFlow(false)
    val tapeMode: StateFlow<Boolean> = _tapeMode.asStateFlow()
    private var pendingHistoryCapture: PendingHistoryCapture? = null
    private val _tapeEntries = MutableStateFlow<List<TapeEntry>>(emptyList())
    val tapeEntries: StateFlow<List<TapeEntry>> = _tapeEntries.asStateFlow()

    private val _liveTapeEntry = MutableStateFlow<TapeEntry?>(null)
    val liveTapeEntry: StateFlow<TapeEntry?> = _liveTapeEntry.asStateFlow()

    private var tapeIdCounter = 0L
    private var lastCommittedTapeSignature: String? = null

    // Live result preview (Google Calculator style)
    private val _previewResult = MutableStateFlow<String?>(null)
    val previewResult: StateFlow<String?> = _previewResult.asStateFlow()

    // Live Unit Hint
    private val _unitHint = MutableStateFlow<String?>(null)
    val unitHint: StateFlow<String?> = _unitHint.asStateFlow()

    // Optional runtime latency diagnostics controlled by settings.
    val calculationLatencyMs: StateFlow<Long?> = combine(
        appPreferences.gui.showCalculationLatency,
        calculator.lastEvaluationLatencyMs
    ) { showLatency, latencyMs ->
        if (showLatency) latencyMs else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Recent history for chips
    val recentHistory: StateFlow<List<HistoryState>> = roomHistory.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoryActiveRegister: StateFlow<String> = memory.activeRegister
    val memoryRegisters: StateFlow<List<MemoryRegisterState>> = memory.registers
    val numeralBase: StateFlow<NumeralBase> = appPreferences.settings.numeralBase
        .map { baseId -> NumeralBase.entries.getOrElse(baseId) { NumeralBase.dec } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NumeralBase.dec)
    val bitwiseWordSize: StateFlow<Int> = appPreferences.settings.bitwiseWordSize
        .map { it.coerceIn(1, 64) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 64)
    val bitwiseSigned: StateFlow<Boolean> = appPreferences.settings.bitwiseSigned
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val bitwiseOverflow: StateFlow<Boolean> = combine(
        displayState,
        bitwiseWordSize,
        bitwiseSigned,
        numeralBase
    ) { state, wordSize, signed, base ->
        if (!state.valid || state.text.isBlank()) {
            false
        } else {
            BitwiseRange.isOverflow(
                displayText = state.text,
                wordSize = wordSize,
                signed = signed,
                base = base
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // Initialize display to listen to calculator events
        display.init()

        viewModelScope.launch {
            display.changedEvents.collect { event ->
                val pending = pendingHistoryCapture
                if (pending != null && event.newState.sequence == pending.sequence) {
                    pendingHistoryCapture = null
                    if (event.newState.valid && event.newState.text.isNotBlank()) {
                        persistHistoryEntry(
                            expression = pending.expression,
                            result = event.newState.text,
                            selection = pending.selection
                        )
                    }
                }
                refreshLiveTape()
            }
        }
        
        viewModelScope.launch {
            editor.stateFlow.collect { state ->
                if (_rpnMode.value) {
                    _unitHint.value = null
                    return@collect
                }
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
                refreshLiveTape()
            }
        }

        viewModelScope.launch {
            appPreferences.settings.rpnMode.collect { enabled ->
                val wasEnabled = _rpnMode.value
                if (wasEnabled == enabled) return@collect

                _rpnMode.value = enabled
                if (enabled) {
                    infixEditorSnapshot = editor.state
                    clearRpnState(resetEditorAndDisplay = true)
                } else {
                    rpnMachine.clear()
                    val snapshot = infixEditorSnapshot
                    if (snapshot != null) {
                        editor.setText(snapshot.text, snapshot.selection)
                    } else {
                        editor.clear()
                    }
                    display.setState(DisplayState.empty())
                    _previewResult.value = null
                    _unitHint.value = null
                }
                refreshLiveTape()
            }
        }

        // Keep calculator behavior aligned with current preferences.
        viewModelScope.launch {
            appPreferences.settings.calculateOnFly.collect { enabled ->
                _calculateOnFly.value = enabled
                calculator.setCalculateOnFly(enabled)
                refreshLiveTape()
            }
        }

        viewModelScope.launch {
            appPreferences.settings.tapeMode.collect { enabled ->
                _tapeMode.value = enabled
                if (!enabled) {
                    _liveTapeEntry.value = null
                }
                refreshLiveTape()
            }
        }

        // Observe editor changes to trigger calculations and live preview
        viewModelScope.launch {
            editor.changedEvents.collect { event ->
                pendingHistoryCapture?.let { pending ->
                    if (pending.sequence != event.newState.sequence) {
                        pendingHistoryCapture = null
                    }
                }
                if (_rpnMode.value) {
                    _previewResult.value = null
                    refreshLiveTape()
                    return@collect
                }
                val forcedRecalculation = event.force
                if (!_latexMode.value && _calculateOnFly.value) {
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
                            if (forcedRecalculation) {
                                calculator.evaluate(
                                    JsclOperation.numeric,
                                    classification.normalized,
                                    event.newState.sequence
                                )
                            } else {
                                display.setState(
                                    DisplayState(
                                        text = classification.normalized,
                                        valid = true,
                                        sequence = event.newState.sequence
                                    )
                                )
                                _previewResult.value = null
                                lastLiveKind = LiveKind.Number
                                lastLiveExpression = ""
                            }
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
                            if (forcedRecalculation || classification.normalized != lastLiveExpression) {
                                if (lastLiveKind != LiveKind.Expression) {
                                    display.setState(DisplayState.empty())
                                }
                                lastLiveKind = LiveKind.Expression
                                lastLiveExpression = classification.normalized
                                liveEvalJob = viewModelScope.launch {
                                    val delayMs = if (forcedRecalculation) {
                                        0L
                                    } else {
                                        liveEvalDelayFor(classification.normalized)
                                    }
                                    if (delayMs > 0L) delay(delayMs)
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
                    // Preference-driven recalculations should still update display even
                    // when calculate-on-fly is disabled.
                    if (forcedRecalculation && !_latexMode.value && event.newState.text.isNotEmpty()) {
                        calculator.evaluate(
                            JsclOperation.numeric,
                            event.newState.text,
                            event.newState.sequence
                        )
                    } else if (!_latexMode.value && event.newState.text.isNotEmpty()) {
                        updatePreview(event.newState.text)
                    } else {
                        _previewResult.value = null
                    }
                }
                refreshLiveTape()
            }
        }
    }

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
            refreshLiveTape()
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
        val normalizedDigit = if (digit == ",") "." else digit
        if (normalizedDigit == ".") {
            if (numeralBase.value != NumeralBase.dec) {
                viewModelScope.launch {
                    appPreferences.settings.setNumeralBase(NumeralBase.dec.ordinal)
                }
            }
            if (_rpnMode.value) {
                rpnAppendDigit(".")
                return
            }
            editor.insert(transformForLatex("."))
            return
        }
        if (!isDigitAllowed(digit, numeralBase.value)) {
            return
        }
        if (_rpnMode.value) {
            rpnAppendDigit(digit)
            return
        }
        editor.insert(transformForLatex(digit))
    }

    fun onOperatorPressed(operator: String) {
        if (_rpnMode.value) {
            when (operator) {
                "%" -> viewModelScope.launch { rpnApplyUnaryExpression("(%s) / 100") }
                "~" -> viewModelScope.launch { rpnApplyUnaryFunction("not") }
                "&" -> viewModelScope.launch { rpnApplyBinaryFunction("and") }
                "|" -> viewModelScope.launch { rpnApplyBinaryFunction("or") }
                "^" -> viewModelScope.launch { rpnApplyBinaryFunction("xor") }
                "<<" -> viewModelScope.launch { rpnApplyBinaryFunction("shl") }
                ">>" -> viewModelScope.launch { rpnApplyBinaryFunction("shr") }
                else -> viewModelScope.launch { rpnApplyBinaryOperator(operator) }
            }
            return
        }
        if (operator == "~") {
            insert(operator)
            return
        }
        if (operator in BITWISE_BINARY_OPERATORS) {
            insert(" $operator ")
            return
        }
        editor.insert(transformForLatex(operator))
    }

    fun onFunctionPressed(function: String) {
        if (_rpnMode.value) {
            if (function in BINARY_FUNCTION_NAMES) {
                viewModelScope.launch { rpnApplyBinaryFunction(function) }
            } else {
                viewModelScope.launch { rpnApplyUnaryFunction(function) }
            }
            return
        }
        val insertion = functionInsertion(function)
        insert(insertion.text, insertion.selectionOffset)
    }

    fun onClear() {
        pendingHistoryCapture = null
        if (_rpnMode.value) {
            clearRpnState(resetEditorAndDisplay = true)
            return
        }
        editor.clear()
        display.setState(DisplayState.empty())
        _previewResult.value = null
        _unitHint.value = null
        refreshLiveTape()
    }

    fun onBackspace(): Boolean {
        if (_rpnMode.value) {
            return rpnBackspace()
        }
        return editor.erase()
    }

    fun onEquals() {
        // In LaTeX mode, equals does nothing (no calculation)
        if (_latexMode.value) return
        if (_rpnMode.value) {
            viewModelScope.launch { rpnEnter() }
            return
        }
        val editorState = editor.state
        if (!_calculateOnFly.value) {
            // Legacy behavior: explicit equals triggers evaluation only when calculate-on-fly is off.
            pendingHistoryCapture = PendingHistoryCapture(
                sequence = editorState.sequence,
                expression = editorState.text,
                selection = editorState.selection
            )
            calculator.evaluate(JsclOperation.numeric, editorState.text, editorState.sequence)
            return
        }

        // Legacy behavior parity: in calculate-on-fly mode, equals reuses the current display result.
        // Guard against stale in-flight results by matching the editor sequence.
        val displayState = display.getState()
        if (displayState.valid &&
            displayState.text.isNotEmpty() &&
            displayState.sequence == editorState.sequence
        ) {
            editor.setText(displayState.text)
            viewModelScope.launch {
                persistHistoryEntry(
                    expression = editorState.text,
                    result = displayState.text,
                    selection = editorState.selection
                )
            }
        } else {
            pendingHistoryCapture = PendingHistoryCapture(
                sequence = editorState.sequence,
                expression = editorState.text,
                selection = editorState.selection
            )
            calculator.evaluate(JsclOperation.numeric, editorState.text, editorState.sequence)
        }
    }

    /**
     * Simplifies the current expression algebraically.
     * E.g., "2x + x" -> "3*x"
     */
    fun onSimplify() {
        if (_latexMode.value) return
        if (_rpnMode.value) {
            viewModelScope.launch { rpnApplyUnaryExpression("simplify(%s)") }
            return
        }
        calculator.evaluate(JsclOperation.simplify, editor.state.text, editor.state.sequence)
    }

    fun onParenthesis(paren: String) {
        if (_rpnMode.value) {
            rpnAppendRaw(paren)
            return
        }
        editor.insert(transformForLatex(paren))
    }

    // --- Cursor Operations ---

    fun moveCursorLeft() {
        if (_rpnMode.value) return
        editor.moveCursorLeft()
    }

    fun moveCursorRight() {
        if (_rpnMode.value) return
        editor.moveCursorRight()
    }

    fun moveCursorToStart() {
        if (_rpnMode.value) return
        editor.setCursorOnStart()
    }

    fun moveCursorToEnd() {
        if (_rpnMode.value) return
        editor.setCursorOnEnd()
    }

    fun onEditorSelectionChange(selection: Int) {
        if (_rpnMode.value) return
        editor.setSelection(selection)
    }

    fun undo(): Boolean {
        pendingHistoryCapture = null
        val changed = if (_rpnMode.value) {
            rpnBackspace()
        } else {
            editor.undo()
        }
        if (changed) refreshLiveTape()
        return changed
    }

    fun redo(): Boolean {
        pendingHistoryCapture = null
        if (_rpnMode.value) return false
        val changed = editor.redo()
        if (changed) refreshLiveTape()
        return changed
    }

    // --- Copy ---

    /**
     * Returns the text to copy, or null if nothing to copy.
     * The caller should use LocalClipboard to perform the actual copy.
     */
    fun getTextToCopy(): String? {
        if (_rpnMode.value) {
            val x = _rpnStack.value.lastOrNull()
            if (!x.isNullOrEmpty()) return x
            return _rpnEntry.value.ifEmpty { null }
        }
        return display.getTextToCopy()
    }

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

    fun selectMemoryRegister(name: String) {
        viewModelScope.launch {
            memory.setActiveRegister(name)
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
                persistHistoryEntry(expression, result, editor.state.selection)
            }
        }
    }

    fun clearHistory() {
        pendingHistoryCapture = null
        viewModelScope.launch {
            roomHistory.clearRecent()
        }
    }

    fun clearTape() {
        _tapeEntries.value = emptyList()
        _liveTapeEntry.value = null
        lastCommittedTapeSignature = null
    }

    private suspend fun persistHistoryEntry(expression: String, result: String, selection: Int) {
        val normalizedExpression = expression.trim()
        val normalizedResult = result.trim()
        if (normalizedExpression.isEmpty() || normalizedResult.isEmpty()) return

        appendCommittedTapeEntry(normalizedExpression, normalizedResult)

        val last = roomHistory.getLastEntry()
        if (last != null &&
            last.expression == normalizedExpression &&
            last.result == normalizedResult
        ) {
            return
        }

        roomHistory.addEntry(
            expression = normalizedExpression,
            result = normalizedResult,
            selection = selection.coerceAtLeast(0)
        )
    }

    // --- Utility ---

    fun onEditorTextChange(text: String, selection: Int = text.length) {
        if (_rpnMode.value) {
            rpnMachine.setEntry(text)
            applyRpnSnapshot(rpnMachine.snapshot())
            syncRpnEditorAndDisplay()
            return
        }
        val normalized = normalizeMultiplication(text)
        val boundedSelection = selection.coerceIn(0, normalized.length)
        val current = editor.state
        if (current.getTextString() == normalized && current.selection == boundedSelection) {
            return
        }
        editor.setText(normalized, boundedSelection)
    }

    fun insert(text: String, selectionOffset: Int = 0) {
        if (_rpnMode.value) {
            rpnAppendRaw(text)
            return
        }
        editor.insert(transformForLatex(text), selectionOffset)
    }

    fun onPasteText(text: String) {
        if (_rpnMode.value) {
            rpnMachine.setEntry(text)
            applyRpnSnapshot(rpnMachine.snapshot())
            syncRpnEditorAndDisplay()
            return
        }
        insert(text)
    }

    // --- Special ---

    fun onSpecialClick(action: String) {
        if (_rpnMode.value) {
            when (action) {
                "()" -> viewModelScope.launch { rpnEnter() }
                "^" -> viewModelScope.launch { rpnApplyBinaryOperator("^") }
                "^2" -> viewModelScope.launch { rpnApplyUnaryExpression("(%s)^2") }
                "^3" -> viewModelScope.launch { rpnApplyUnaryExpression("(%s)^3") }
                "!" -> viewModelScope.launch { rpnApplyUnaryExpression("(%s)!") }
                "±" -> rpnToggleSign()
                else -> rpnAppendRaw(action)
            }
            return
        }
        val selectionOffset = when (action) {
            "()" -> -1
            else -> 0
        }
        insert(action, selectionOffset)
    }

    private fun clearRpnState(resetEditorAndDisplay: Boolean) {
        rpnMachine.clear()
        applyRpnSnapshot(rpnMachine.snapshot())
        if (resetEditorAndDisplay) {
            editor.clear()
            display.setState(DisplayState.empty())
        }
        _previewResult.value = null
        _unitHint.value = null
        refreshLiveTape()
    }

    private fun applyRpnSnapshot(snapshot: RpnSnapshot) {
        _rpnStack.value = snapshot.stack
        _rpnEntry.value = snapshot.entry
    }

    private fun syncRpnEditorAndDisplay() {
        val entry = _rpnEntry.value
        val current = editor.state
        if (current.getTextString() != entry || current.selection != entry.length) {
            editor.setText(entry, entry.length)
        }

        val x = if (entry.isNotEmpty()) entry else _rpnStack.value.lastOrNull().orEmpty()
        if (x.isEmpty()) {
            display.setState(DisplayState.empty())
        } else {
            display.setState(
                DisplayState(
                    text = x,
                    valid = true,
                    sequence = editor.state.sequence
                )
            )
        }
        _previewResult.value = null
        _unitHint.value = null
    }

    private fun rpnAppendDigit(digit: String) {
        val before = rpnMachine.snapshot()
        rpnMachine.appendDigit(digit)
        val after = rpnMachine.snapshot()
        if (after != before) {
            applyRpnSnapshot(after)
            syncRpnEditorAndDisplay()
        }
    }

    private fun rpnAppendRaw(token: String) {
        if (token.isEmpty()) return
        rpnMachine.appendRaw(token)
        applyRpnSnapshot(rpnMachine.snapshot())
        syncRpnEditorAndDisplay()
    }

    private fun rpnBackspace(): Boolean {
        val changed = rpnMachine.backspace()
        if (changed) {
            applyRpnSnapshot(rpnMachine.snapshot())
            syncRpnEditorAndDisplay()
        }
        return changed
    }

    private fun rpnToggleSign() {
        val before = rpnMachine.snapshot()
        rpnMachine.toggleSign()
        val after = rpnMachine.snapshot()
        if (after != before) {
            applyRpnSnapshot(after)
            syncRpnEditorAndDisplay()
        }
    }

    private suspend fun rpnEnter() = rpnOperationMutex.withLock {
        rpnMachine.enter()
        applyRpnSnapshot(rpnMachine.snapshot())
        syncRpnEditorAndDisplay()
    }

    private suspend fun rpnApplyBinaryOperator(operator: String) = rpnOperationMutex.withLock {
        when (val result = rpnMachine.applyBinaryOperator(operator)) {
            RpnActionResult.Success -> Unit
            is RpnActionResult.Error -> notifier.showMessage(result.message)
        }
        applyRpnSnapshot(rpnMachine.snapshot())
        syncRpnEditorAndDisplay()
    }

    private suspend fun rpnApplyUnaryFunction(function: String) = rpnOperationMutex.withLock {
        when (val result = rpnMachine.applyUnaryFunction(function)) {
            RpnActionResult.Success -> Unit
            is RpnActionResult.Error -> notifier.showMessage(result.message)
        }
        applyRpnSnapshot(rpnMachine.snapshot())
        syncRpnEditorAndDisplay()
    }

    private suspend fun rpnApplyBinaryFunction(function: String) = rpnOperationMutex.withLock {
        when (val result = rpnMachine.applyBinaryExpression("$function(%s,%s)")) {
            RpnActionResult.Success -> Unit
            is RpnActionResult.Error -> notifier.showMessage(result.message)
        }
        applyRpnSnapshot(rpnMachine.snapshot())
        syncRpnEditorAndDisplay()
    }

    private suspend fun rpnApplyUnaryExpression(expressionTemplate: String) = rpnOperationMutex.withLock {
        when (val result = rpnMachine.applyUnaryExpression(expressionTemplate)) {
            RpnActionResult.Success -> Unit
            is RpnActionResult.Error -> notifier.showMessage(result.message)
        }
        applyRpnSnapshot(rpnMachine.snapshot())
        syncRpnEditorAndDisplay()
    }

    private fun refreshLiveTape() {
        if (!_tapeMode.value || _rpnMode.value || _latexMode.value) {
            _liveTapeEntry.value = null
            return
        }

        val expression = editor.state.getTextString().trim()
        if (expression.isEmpty()) {
            _liveTapeEntry.value = null
            return
        }

        when (LiveInputClassifier.classify(expression)) {
            is LiveInputClassifier.Result.Empty,
            is LiveInputClassifier.Result.NumberOnly,
            is LiveInputClassifier.Result.Incomplete -> {
                _liveTapeEntry.value = null
                return
            }
            is LiveInputClassifier.Result.Expression -> Unit
        }

        val result = resolveTapeResult(editor.state.sequence)?.trim().orEmpty()
        if (result.isEmpty()) {
            _liveTapeEntry.value = null
            return
        }

        val signature = tapeSignature(expression, result)
        if (signature == lastCommittedTapeSignature) {
            _liveTapeEntry.value = null
            return
        }

        val previous = _liveTapeEntry.value
        if (previous != null && previous.expression == expression && previous.result == result) {
            return
        }

        _liveTapeEntry.value = TapeEntry(
            id = nextTapeId(),
            expression = expression,
            result = result,
            timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            committed = false
        )
    }

    private fun resolveTapeResult(sequence: Long): String? {
        val currentDisplay = display.getState()
        if (currentDisplay.valid &&
            currentDisplay.sequence == sequence &&
            currentDisplay.text.isNotBlank()
        ) {
            return currentDisplay.text
        }
        if (!_calculateOnFly.value) {
            return _previewResult.value?.takeIf { it.isNotBlank() }
        }
        return null
    }

    private fun appendCommittedTapeEntry(expression: String, result: String) {
        if (!_tapeMode.value) return
        val signature = tapeSignature(expression, result)
        if (signature == lastCommittedTapeSignature) return

        val newEntry = TapeEntry(
            id = nextTapeId(),
            expression = expression,
            result = result,
            timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            committed = true
        )

        _tapeEntries.update { entries ->
            val next = entries + newEntry
            if (next.size > TAPE_MAX_ENTRIES) next.takeLast(TAPE_MAX_ENTRIES) else next
        }
        lastCommittedTapeSignature = signature

        val live = _liveTapeEntry.value
        if (live != null && live.expression == expression && live.result == result) {
            _liveTapeEntry.value = null
        }
    }

    private fun tapeSignature(expression: String, result: String): String {
        return expression.trim() + '\u0000' + result.trim()
    }

    private fun nextTapeId(): Long {
        tapeIdCounter += 1L
        return tapeIdCounter
    }

    private fun liveEvalDelayFor(normalizedExpression: String): Long {
        if (normalizedExpression.length <= 18) {
            return 0L
        }
        return when {
            simpleMultiplyRegex.matches(normalizedExpression) -> LIVE_EVAL_DELAY_SIMPLE_MULTIPLY_MS
            simpleArithmeticRegex.matches(normalizedExpression) -> LIVE_EVAL_DELAY_SIMPLE_ARITHMETIC_MS
            else -> LIVE_EVAL_DELAY_COMPLEX_MS
        }
    }

    private val simpleMultiplyRegex = Regex(
        """^\s*[+\-−]?\d+(?:\.\d+)?\s*[×*]\s*[+\-−]?\d+(?:\.\d+)?\s*$"""
    )

    private val simpleArithmeticRegex = Regex(
        """^\s*[+\-−]?\d+(?:\.\d+)?(?:\s*[+\-−×÷*/]\s*[+\-−]?\d+(?:\.\d+)?)+\s*$"""
    )

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

    fun setBitwiseWordSize(value: Int) {
        viewModelScope.launch {
            appPreferences.settings.setBitwiseWordSize(value.coerceIn(1, 64))
        }
    }

    fun setBitwiseSigned(value: Boolean) {
        viewModelScope.launch {
            appPreferences.settings.setBitwiseSigned(value)
        }
    }

    private data class FunctionInsertion(val text: String, val selectionOffset: Int)

    private fun functionInsertion(function: String): FunctionInsertion {
        return if (function in BINARY_FUNCTION_NAMES) {
            FunctionInsertion(text = "$function(,)", selectionOffset = -2)
        } else {
            FunctionInsertion(text = "$function()", selectionOffset = -1)
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

    private fun isDigitAllowed(input: String, base: NumeralBase): Boolean {
        if (input.isEmpty()) return false
        if (input == ".") return base == NumeralBase.dec
        if (input == ",") return base == NumeralBase.dec
        val acceptable = base.getAcceptableCharacters()
        return input.all { ch ->
            if (ch == '.') {
                base == NumeralBase.dec
            } else {
                acceptable.contains(ch.uppercaseChar())
            }
        }
    }
}

// Top-level extension function to avoid recursive type checking issues in init block
private fun Double.format(digits: Int): String {
    var multiplier = 1.0
    repeat(digits) { multiplier *= 10.0 }
    val rounded = kotlin.math.round(this * multiplier) / multiplier
    return rounded.toString()
}
