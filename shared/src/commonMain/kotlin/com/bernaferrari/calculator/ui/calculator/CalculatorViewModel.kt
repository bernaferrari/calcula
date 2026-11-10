package com.bernaferrari.calculator.ui.calculator

import com.bernaferrari.calculator.domain.math.UserFunction
import com.bernaferrari.calculator.domain.model.Memory
import com.bernaferrari.calculator.domain.model.Notation
import com.bernaferrari.calculator.domain.model.hints
import com.bernaferrari.calculator.domain.model.FlickHints
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.domain.input.CalcState
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.CalculatorWork
import com.bernaferrari.calculator.domain.input.Editor
import com.bernaferrari.calculator.domain.input.ExpressionEditor
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.input.Span
import com.bernaferrari.calculator.domain.input.Outcome
import com.bernaferrari.calculator.domain.input.NumericDisplay
import com.bernaferrari.calculator.domain.input.ResultDisplay
import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.CalculationHistory
import com.bernaferrari.calculator.domain.model.DecimalSeparator
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.bernaferrari.calculator.AppGraph
import com.bernaferrari.calculator.domain.input.Rpn
import com.bernaferrari.calculator.domain.input.RpnOutcome
import com.bernaferrari.calculator.domain.input.RpnState
import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.Expression
import com.bernaferrari.calculator.domain.math.NumberFormatter
import com.bernaferrari.calculator.domain.model.AUTO_DECIMALS
import com.bernaferrari.calculator.domain.model.UserPreferences

data class CalculatorUiState(
    val text: String = "",
    val cursor: Int = 0,
    val resultShown: Boolean = false,
    val preview: String? = null,
    val previewApproximate: Boolean = false,
    val resultDisplay: ResultDisplay? = null,
    val extendedDisplay: NumericDisplay? = null,
    val error: CalcError? = null,
    val inverse: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    val history: List<CalculationHistory> = emptyList(),
    val haptics: Boolean = true,
    val sounds: Boolean = false,
    val reduceMotion: Boolean = false,
    val showHistory: Boolean = true,
    val flickHints: FlickHints = FlickHints.DOTS,
    val swipeHintSeen: Boolean = true,
    val keypadScale: Float = 1f,
    val decimalSeparator: DecimalSeparator = DecimalSeparator.AUTO,
    /** Bumps on every `=` and clear, so the display can animate the hand-off. */
    val generation: Int = 0,
    /** Keys that would do nothing to the expression right now; the keypad dims its functions. */
    val inert: Set<Key> = emptySet(),
    /** Text picked in the display, in raw offsets; the next key acts on it. */
    val selection: Span? = null,
    /** The result as a simple fraction, when it is one. */
    val fraction: String? = null,
    val memoryKeys: Boolean = false,
    /** What the first memory holds, or null; the memory keys work on it. */
    val memory: String? = null,
    val memories: List<Memory> = emptyList(),
    /** There's a number on screen worth storing (not nothing, not zero): M+, M− and Keep light up. */
    val canStore: Boolean = false,
    val functions: List<UserFunction> = emptyList(),
    /** Flicks the person reassigned, resolved to what they do. */
    val flicks: Map<String, CustomFlick?> = emptyMap(),
    /** Reverse Polish entry; [stack] then holds X, Y, Z… as numbers, X as typed while typing. */
    val rpn: Boolean = false,
    val stack: List<String> = emptyList(),
    val entering: Boolean = false,
) {
    val openParentheses: Int get() = ExpressionEditor.openParentheses(text).coerceAtLeast(0)
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModel(
    private val historyRepository: CalculatorRepository = AppGraph.history,
    private val preferences: PreferencesRepository = AppGraph.preferences,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    private val evaluationDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val calc = MutableStateFlow(
        CalcState(
            editor = Editor(savedState.get<String>(KEY_TEXT).orEmpty()).let {
                it.copy(cursor = (savedState.get<Int>("cursor") ?: it.cursor).coerceIn(0, it.text.length))
            }.restoreAnswer(savedState[KEY_ANSWER]).copy(savedExpression = savedState[KEY_REPLAY]),
            resultShown = savedState[KEY_RESULT] ?: false,
            repeatTail = savedState["repeat_tail"],
            inverse = savedState["inverse"] ?: false,
            fraction = savedState["fraction"],
        ),
    )
    private val generation = MutableStateFlow(0)
    private data class ExtendedDisplay(val number: NumericDisplay? = null, val canRequestMore: Boolean = true)
    private val extended = MutableStateFlow(ExtendedDisplay())
    private val rpn = MutableStateFlow(
        RpnState(
            stack = savedState.get<Array<String>>("rpn_stack")?.map { Decimal(it) }.orEmpty(),
            entry = savedState["rpn_entry"],
            lift = savedState["rpn_lift"] ?: true,
        ),
    )
    // Every edit can be stepped back and forward: ⌫ flicks up to undo, down to redo.
    private val undoStack = ArrayDeque<CalcState>()
    private val redoStack = ArrayDeque<CalcState>()
    private val steps = MutableStateFlow(0 to 0)

    private val _events = MutableSharedFlow<Outcome>(extraBufferCapacity = 8)
    val events: SharedFlow<Outcome> = _events

    private data class PreviewInput(val editor: Editor, val angle: AngleUnit, val decimals: Int, val notation: Notation)

    private fun CalcState.previewInput() = PreviewInput(editor.copy(cursor = 0), angleUnit, decimalPlaces, notation)

    private val preview = calc
        .map { it.previewInput() }
        .distinctUntilChanged()
        .mapLatest { input ->
            if (Calculator.isPlainNumber(input.editor.text)) return@mapLatest input to null
            delay(60)
            input to CalculatorWork.preview(input.editor, input.angle, input.decimals, input.notation)
        }
        .flowOn(evaluationDispatcher)

    private val prefs = preferences.getUserPreferences()
    private val settings = prefs.stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val state: StateFlow<CalculatorUiState> = combine(
        combine(calc, rpn, steps, extended) { a, b, _, digits -> Triple(a, b, digits) }, preview, historyRepository.getRecentHistory(limit = 200), prefs, generation,
    ) { (calc, rpn, extended), (previewInput, previewResult), history, prefs, generation ->
        val preview = previewResult.takeIf { !calc.resultShown && previewInput == calc.previewInput() }
        CalculatorUiState(
            text = calc.editor.text,
            cursor = calc.editor.cursor,
            resultShown = calc.resultShown,
            preview = preview?.text,
            previewApproximate = preview?.approximate == true,
            resultDisplay = if (calc.resultShown) Calculator.resultDisplay(calc.editor)?.let {
                it.copy(canRefine = it.canRefine && extended.canRequestMore)
            } else null,
            extendedDisplay = extended.number,
            error = calc.error,
            inverse = calc.inverse,
            angleUnit = calc.angleUnit,
            history = history,
            haptics = prefs.hapticFeedback,
            sounds = prefs.soundEnabled,
            reduceMotion = prefs.reduceMotion,
            showHistory = prefs.showHistory,
            flickHints = prefs.hints,
            swipeHintSeen = prefs.swipeHintSeen,
            keypadScale = prefs.keypadScale,
            decimalSeparator = prefs.decimalSeparator,
            generation = generation,
            inert = if (prefs.rpn) setOf(Key.UNDO, Key.REDO) else Calculator.inertKeys(calc) - setOf(Key.UNDO, Key.REDO) +
                listOfNotNull(Key.UNDO.takeIf { undoStack.isEmpty() }, Key.REDO.takeIf { redoStack.isEmpty() }),
            selection = calc.selection,
            fraction = calc.fraction,
            memoryKeys = prefs.memoryKeys,
            memory = prefs.memories.firstOrNull()?.value,
            memories = prefs.memories,
            canStore = storable(calc, rpn, preview?.text, prefs.rpn),
            functions = prefs.functions,
            flicks = FlickCatalog.resolve(prefs.flicks, prefs.functions),
            rpn = prefs.rpn,
            stack = rpn.stack.mapIndexed { i, v -> if (i == 0 && rpn.entry != null) rpn.entry else Calculator.formatResult(v, prefs.decimalPlaces, prefs.notation) },
            entering = rpn.entry != null,
        ).let { if (prefs.rpn) it.copy(error = rpn.error) else it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalculatorUiState(text = calc.value.editor.text))

    init {
        viewModelScope.launch {
            prefs.map { it.angleUnit }.distinctUntilChanged().collect { unit -> calc.update { it.copy(angleUnit = unit) } }
        }
        viewModelScope.launch {
            prefs.map { it.decimalPlaces }.distinctUntilChanged().collect { places -> calc.update { it.copy(decimalPlaces = places) } }
        }
        viewModelScope.launch {
            prefs.map { it.notation }.distinctUntilChanged().collect { n -> calc.update { it.copy(notation = n) } }
        }
    }


    private fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(calc.value)
        setState(previous, record = false)
    }

    private fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(calc.value)
        setState(next, record = false)
    }

    // Usage is counted here and saved after each calculation (and when the screen goes), not on
    // every key press.
    private val pendingKeys = mutableMapOf<String, Int>()
    private var pendingFlicks = 0
    private var pendingCalculations = 0

    private fun count(key: Key) { pendingKeys[key.name] = (pendingKeys[key.name] ?: 0) + 1 }

    private fun saveUsage() {
        if (pendingKeys.isEmpty() && pendingFlicks == 0 && pendingCalculations == 0) return
        val keys = pendingKeys.toMap(); val flicks = pendingFlicks; val calculations = pendingCalculations
        pendingKeys.clear(); pendingFlicks = 0; pendingCalculations = 0
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val day = now / 86_400_000L
        // ViewModel clearing cancels viewModelScope before onCleared runs. This final write
        // needs its own lifetime, including if a repository implementation suspends.
        kotlinx.coroutines.CoroutineScope(Dispatchers.Main.immediate).launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
            preferences.update { it.copy(usage = it.usage.plus(keys, flicks, calculations, day, now)) }
        }
    }

    override fun onCleared() {
        saveUsage()
        super.onCleared()
    }

    private var evaluation: Job? = null
    private val queuedKeys = ArrayDeque<Key>()

    fun onKey(key: Key) {
        count(key)
        if (key == Key.CLEAR) cancelEvaluation()
        if (evaluation != null) {
            queuedKeys.addLast(key)
            return
        }
        pressKey(key)
    }

    private fun pressKey(key: Key) {
        if (settings.value.rpn) return onRpnKey(key)
        if (key == Key.UNDO) return undo()
        if (key == Key.REDO) return redo()
        if (key == Key.CLEAR && calc.value.editor.isEmpty) return
        if (key == Key.EQUALS) {
            startEvaluation()
            return
        }
        val (next, outcome) = Calculator.press(calc.value, key)
        acceptKeyOutcome(next, outcome)
    }

    private fun startEvaluation() {
        val snapshot = calc.value
        startWork(compute = { CalculatorWork.equals(snapshot) }) { (next, outcome) ->
            if (calc.value.copy(selection = snapshot.selection) == snapshot && !settings.value.rpn) {
                acceptKeyOutcome(next, outcome)
            }
        }
    }

    private fun <T> startWork(compute: suspend () -> T, apply: (T) -> Unit) {
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            val result = withContext(evaluationDispatcher) { compute() }
            evaluation = null
            apply(result)
            while (evaluation == null && queuedKeys.isNotEmpty()) pressKey(queuedKeys.removeFirst())
        }
        evaluation = job
        job.start()
    }

    private var detailJob: Job? = null

    fun onMoreDigits() {
        val snapshot = calc.value
        if (!snapshot.resultShown || Calculator.resultDisplay(snapshot.editor)?.canRefine != true) return
        if (detailJob?.isActive == true || !extended.value.canRequestMore) return
        val previous = extended.value.number
        val requested = minOf((previous?.significantDigits ?: 0) + 30, CalcEngine.MAX_REFINEMENT_DIGITS)
        detailJob = viewModelScope.launch {
            val result = withContext(evaluationDispatcher) {
                CalculatorWork.moreDigits(snapshot.editor, requested)
            }
            if (calc.value.editor == snapshot.editor && calc.value.resultShown) {
                val settledDigits = result?.significantDigits ?: 0
                val improved = settledDigits > (previous?.significantDigits ?: 15)
                extended.value = ExtendedDisplay(
                    if (improved) result else previous,
                    improved && result?.approximate == true && settledDigits < CalcEngine.MAX_REFINEMENT_DIGITS,
                )
            }
        }
    }

    private fun cancelEvaluation() {
        evaluation?.cancel()
        evaluation = null
        queuedKeys.clear()
    }

    private fun acceptKeyOutcome(next: CalcState, outcome: Outcome) {
        setState(next)
        when (outcome) {
            is Outcome.Evaluated -> {
                pendingCalculations++
                saveUsage()
                generation.update { it + 1 }
                viewModelScope.launch {
                    historyRepository.insertCalculation(
                        CalculationHistory(expression = outcome.expression, result = outcome.result, answerOperand = outcome.answerOperand,
                            approximate = outcome.approximate, resultOperand = next.editor.saveAnswer()),
                    )
                }
            }
            Outcome.Cleared -> generation.update { it + 1 }
            else -> Unit
        }
        if (outcome != Outcome.None) _events.tryEmit(outcome)
    }

    private fun onRpnKey(key: Key) {
        if (key == Key.CLEAR && rpn.value.stack.isEmpty()) return
        if (Rpn.requiresEvaluation(key)) {
            startRpnWork(CalculatorWork.RpnOperation.Press(key))
        } else {
            val (next, outcome) = Rpn.press(rpnSnapshot(), key)
            onRpnOutcome(next, outcome)
        }
    }

    private fun rpnSnapshot() = rpn.value.copy(
        angleUnit = calc.value.angleUnit, decimalPlaces = calc.value.decimalPlaces, notation = calc.value.notation,
    )

    private fun startRpnWork(operation: CalculatorWork.RpnOperation) {
        if (evaluation != null) cancelEvaluation()
        val snapshot = rpnSnapshot()
        startWork(compute = { CalculatorWork.rpn(snapshot, operation) }) { (next, outcome) ->
            if (settings.value.rpn && rpnSnapshot() == snapshot) onRpnOutcome(next, outcome)
        }
    }

    private fun onRpnOutcome(next: RpnState, outcome: RpnOutcome) {
        setRpnState(next)
        when (outcome) {
            is RpnOutcome.Evaluated -> viewModelScope.launch {
                pendingCalculations++
                saveUsage()
                historyRepository.insertCalculation(CalculationHistory(expression = outcome.expression, result = outcome.result, approximate = outcome.approximate))
            }
            is RpnOutcome.Failed -> _events.tryEmit(Outcome.Failed(outcome.error))
            RpnOutcome.Cleared -> _events.tryEmit(Outcome.Cleared)
            RpnOutcome.None -> Unit
        }
    }

    /**
     * The memory keys. M+ and M− finish the calculation first, like pressing `=`, then add or
     * take away the answer; MR drops what's stored at the cursor (or onto the stack).
     */
    fun onMemory(op: MemoryOp) {
        val memories = settings.value.memories
        val first = memories.firstOrNull()
        when (op) {
            MemoryOp.CLEAR -> saveMemories(memories.drop(1))
            MemoryOp.RECALL -> if (first != null) insertNumber(first.value)
            MemoryOp.ADD, MemoryOp.SUBTRACT -> viewModelScope.launch {
                val value = currentValue() ?: return@launch
                preferences.update { prefs ->
                    val memory = prefs.memories.firstOrNull()
                    val base = memory?.value?.let { CalcEngine.evaluateOrNull(it) } ?: Decimal.ZERO
                    val sum = Calculator.formatResult(if (op == MemoryOp.ADD) base.add(value) else base.subtract(value), AUTO_DECIMALS)
                    prefs.copy(memories = listOf(Memory(sum, memory?.label)) + prefs.memories.drop(1))
                }
            }
        }
    }

    private fun storable(calc: CalcState, rpn: RpnState, preview: String?, rpnMode: Boolean): Boolean {
        val value = if (rpnMode) Rpn.x(rpn) else {
            val text = calc.editor.text
            val shown = when {
                calc.resultShown -> text
                preview != null -> preview
                text.isNotEmpty() && Calculator.isPlainNumber(text) -> text
                else -> null
            }
            shown?.let { CalcEngine.evaluateOrNull(it) }
        }
        return value != null && value.signum() != 0
    }

    /** Keeps what's on screen as a memory of its own, at the end of the list. */
    fun onStoreMemory() {
        viewModelScope.launch {
            val value = currentValue() ?: return@launch
            val memory = Memory(Calculator.formatResult(value, AUTO_DECIMALS))
            preferences.update { it.copy(memories = it.memories + memory) }
        }
    }

    fun onRecallMemory(index: Int) { settings.value.memories.getOrNull(index)?.let { insertNumber(it.value) } }

    fun onDeleteMemory(index: Int) = saveMemories(settings.value.memories.filterIndexed { i, _ -> i != index })

    fun onRenameMemory(index: Int, label: String) = saveMemories(
        settings.value.memories.mapIndexed { i, m -> if (i == index) m.copy(label = label.trim().ifEmpty { null }) else m },
    )

    /** Adds a function of the person's own, or replaces the one with that name. */
    fun onSaveFunction(function: UserFunction) {
        viewModelScope.launch { preferences.update { p -> p.copy(functions = p.functions.filter { it.name != function.name } + function) } }
    }

    fun onDeleteFunction(name: String) {
        viewModelScope.launch { preferences.update { p -> p.copy(functions = p.functions.filter { it.name != name }) } }
    }

    /** Drops a constant's value or a function call at the cursor. */
    fun onInsertText(text: String) {
        if (settings.value.rpn) {
            val operation = if (text.endsWith("(")) CalculatorWork.RpnOperation.Function(text.dropLast(1))
                else CalculatorWork.RpnOperation.Insert(text)
            startRpnWork(operation)
            return
        }
        setState(Calculator.insertText(calc.value, text))
    }

    private suspend fun currentValue(): Decimal? {
        evaluation?.join()
        if (settings.value.rpn) return Rpn.x(rpn.value)
        val c = calc.value
        if (!c.resultShown && !Calculator.isPlainNumber(c.editor.text)) {
            onKey(Key.EQUALS)
            evaluation?.join()
            if (!calc.value.resultShown) return null
        }
        val editor = calc.value.editor
        return editor.answer?.value?.v ?: CalcEngine.evaluateOrNull(editor.text.ifEmpty { return null })
    }

    private fun saveMemories(memories: List<Memory>) {
        viewModelScope.launch { preferences.update { it.copy(memories = memories) } }
    }

    /** A function picked by a flick or from the ƒ layer. */
    fun onSwipeKey(key: Key) {
        pendingFlicks++
        onKey(key)
    }

    fun onCursorMoved(position: Int) {
        setState(calc.value.copy(editor = ExpressionEditor.moveCursor(calc.value.editor, position), resultShown = false, selection = null), record = false)
    }

    /** ‹ ›: one step left (−1) or right (+1), over `sin(` as a whole. True if the cursor moved. */
    fun onCursorStep(direction: Int): Boolean {
        val editor = calc.value.editor
        val next = ExpressionEditor.nudge(editor, direction)
        if (next.cursor == editor.cursor) return false
        setState(calc.value.copy(editor = next, resultShown = false, selection = null), record = false)
        return true
    }

    /** Held ‹ ›: to the very start or end. */
    fun onCursorJump(direction: Int): Boolean {
        val editor = calc.value.editor
        val target = if (direction < 0) 0 else editor.text.length
        if (target == editor.cursor) return false
        setState(calc.value.copy(editor = editor.copy(cursor = target), resultShown = false, selection = null), record = false)
        return true
    }

    fun onSelect(selection: Span?) = calc.update { it.copy(selection = selection) }

    /** Cut and paste: [start, end) of the expression becomes [text]. */
    fun onReplace(start: Int, end: Int, text: String) {
        setState(calc.value.copy(editor = ExpressionEditor.replaceRange(calc.value.editor, start, end, text), resultShown = false, error = null, selection = null))
    }

    /** Tapping an entry on the tape drops its result where the cursor is. */
    fun onUseResult(entry: CalculationHistory) = insertNumber(entry.result)

    /** Brings an old calculation back for editing. */
    fun onEditExpression(entry: CalculationHistory) {
        val originalTree = Editor(entry.result).restoreAnswer(entry.resultOperand).answer?.value?.tree
        val editor = Editor(entry.expression).restoreAnswer(entry.answerOperand).copy(savedExpression = originalTree?.let(Expression::save))
        setState(calc.value.copy(editor = editor, selection = null, resultShown = false, error = null))
    }

    private val _removed = MutableSharedFlow<List<CalculationHistory>>(extraBufferCapacity = 4)

    /** Entries just removed from the tape, so the screen can offer to undo. */
    val removed: SharedFlow<List<CalculationHistory>> = _removed

    fun onSwipeHintSeen() { viewModelScope.launch { preferences.markSwipeHintSeen() } }

    fun onKeypadScale(scale: Float) {
        viewModelScope.launch { preferences.updateKeypadScale(scale) }
    }

    fun onNote(entry: CalculationHistory, note: String) {
        viewModelScope.launch { historyRepository.insertCalculation(entry.copy(note = note.trim().ifEmpty { null })) }
    }

    fun onDeleteEntry(entry: CalculationHistory) {
        viewModelScope.launch {
            historyRepository.deleteCalculation(entry)
            _removed.emit(listOf(entry))
        }
    }

    suspend fun historyForExport(): List<CalculationHistory> = historyRepository.getAllHistory().first()

    fun onClearHistory() {
        viewModelScope.launch {
            val everything = historyRepository.getAllHistory().first()
            if (everything.isEmpty()) return@launch
            historyRepository.clearAllHistory()
            _removed.emit(everything)
        }
    }

    fun onRestore(entries: List<CalculationHistory>) {
        viewModelScope.launch { entries.forEach { historyRepository.insertCalculation(it) } }
    }

    fun onToggleAngleUnit() {
        val next = if (calc.value.angleUnit == AngleUnit.DEGREES) AngleUnit.RADIANS else AngleUnit.DEGREES
        calc.update { it.copy(angleUnit = next) }
        viewModelScope.launch { preferences.updateAngleUnit(next) }
    }

    /** A number coming back from the converter, dropped in at the cursor. */
    fun insertNumber(number: String) {
        if (settings.value.rpn) startRpnWork(CalculatorWork.RpnOperation.Insert(number))
        else setState(Calculator.insertNumber(calc.value, number))
    }

    fun loadExpression(expression: String) {
        if (expression.isNotEmpty()) setState(CalcState(editor = Editor(expression), angleUnit = calc.value.angleUnit))
    }

    private fun setState(next: CalcState, record: Boolean = true) {
        if (evaluation != null) cancelEvaluation()
        val current = calc.value
        if (current.editor != next.editor || current.resultShown != next.resultShown) {
            detailJob?.cancel()
            extended.value = ExtendedDisplay()
        }
        // Cursor movement alone isn't an undo step.
        if (record && (next.editor.text != current.editor.text || next.resultShown != current.resultShown ||
                next.editor.answer != current.editor.answer)
        ) {
            undoStack.addLast(current)
            if (undoStack.size > 100) undoStack.removeFirst()
            redoStack.clear()
        }
        steps.value = undoStack.size to redoStack.size
        // Undo restores editing state, not old global settings.
        calc.value = next.copy(angleUnit = settings.value.angleUnit, decimalPlaces = settings.value.decimalPlaces, notation = settings.value.notation)
        savedState["cursor"] = next.editor.cursor
        savedState["repeat_tail"] = next.repeatTail
        savedState["inverse"] = next.inverse
        savedState["fraction"] = next.fraction
        savedState[KEY_ANSWER] = next.editor.saveAnswer()
        savedState[KEY_REPLAY] = next.editor.savedExpression
        savedState[KEY_TEXT] = next.editor.text
        savedState[KEY_RESULT] = next.resultShown
    }

    private fun setRpnState(next: RpnState) {
        rpn.value = next
        savedState["rpn_stack"] = next.stack.map { NumberFormatter.exact(it).replace('−', '-') }.toTypedArray()
        savedState["rpn_entry"] = next.entry
        savedState["rpn_lift"] = next.lift
    }

    private companion object {
        const val KEY_TEXT = "text"
        const val KEY_ANSWER = "answer"
        const val KEY_REPLAY = "answer_replay"
        const val KEY_RESULT = "result_shown"
    }
}
