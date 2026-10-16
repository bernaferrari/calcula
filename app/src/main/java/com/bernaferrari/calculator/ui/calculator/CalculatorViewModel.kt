package com.bernaferrari.calculator.ui.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.domain.input.CalcState
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.Editor
import com.bernaferrari.calculator.domain.input.ExpressionEditor
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.input.Outcome
import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.CalculationHistory
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import javax.inject.Inject

data class CalculatorUiState(
    val text: String = "",
    val cursor: Int = 0,
    val resultShown: Boolean = false,
    val preview: String? = null,
    val error: CalcError? = null,
    val inverse: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    val history: List<CalculationHistory> = emptyList(),
    val haptics: Boolean = true,
    /** Bumps on every `=` and clear, so the display can animate the hand-off. */
    val generation: Int = 0,
) {
    val openParentheses: Int get() = ExpressionEditor.openParentheses(text).coerceAtLeast(0)
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val historyRepository: CalculatorRepository,
    private val preferences: PreferencesRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val calc = MutableStateFlow(
        CalcState(editor = Editor(savedState.get<String>(KEY_TEXT).orEmpty()), resultShown = savedState[KEY_RESULT] ?: false),
    )
    private val generation = MutableStateFlow(0)

    private val _events = MutableSharedFlow<Outcome>(extraBufferCapacity = 8)
    val events: SharedFlow<Outcome> = _events

    private val preview = calc
        .map { it.editor.text to it.angleUnit }
        .distinctUntilChanged()
        .mapLatest { (text, angle) -> Calculator.preview(text, angle) }
        .flowOn(Dispatchers.Default)

    private val prefs = preferences.getUserPreferences()

    val state: StateFlow<CalculatorUiState> = combine(
        calc, preview, historyRepository.getRecentHistory(limit = 200), prefs, generation,
    ) { calc, preview, history, prefs, generation ->
        CalculatorUiState(
            text = calc.editor.text,
            cursor = calc.editor.cursor,
            resultShown = calc.resultShown,
            preview = if (calc.resultShown) null else preview,
            error = calc.error,
            inverse = calc.inverse,
            angleUnit = calc.angleUnit,
            history = history,
            haptics = prefs.hapticFeedback,
            generation = generation,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalculatorUiState(text = calc.value.editor.text))

    init {
        viewModelScope.launch {
            prefs.map { it.angleUnit }.distinctUntilChanged().collect { unit -> calc.update { it.copy(angleUnit = unit) } }
        }
    }

    fun onKey(key: Key) {
        if (key == Key.CLEAR && calc.value.editor.isEmpty) return
        val (next, outcome) = Calculator.press(calc.value, key)
        setState(next)
        when (outcome) {
            is Outcome.Evaluated -> {
                generation.update { it + 1 }
                viewModelScope.launch {
                    historyRepository.insertCalculation(CalculationHistory(expression = outcome.expression, result = outcome.result))
                }
            }
            Outcome.Cleared -> generation.update { it + 1 }
            else -> Unit
        }
        if (outcome != Outcome.None) _events.tryEmit(outcome)
    }

    /** A function picked by a flick or from the ƒ layer. */
    fun onSwipeKey(key: Key) = onKey(key)

    fun onCursorMoved(position: Int) {
        calc.update { it.copy(editor = ExpressionEditor.moveCursor(it.editor, position), resultShown = false) }
    }

    /** Tapping an entry on the tape drops its result where the cursor is. */
    fun onUseResult(entry: CalculationHistory) = setState(Calculator.insertNumber(calc.value, entry.result))

    /** Brings an old calculation back for editing. */
    fun onEditExpression(entry: CalculationHistory) {
        setState(calc.value.copy(editor = Editor(entry.expression), resultShown = false, error = null))
    }

    private val _removed = MutableSharedFlow<List<CalculationHistory>>(extraBufferCapacity = 4)

    /** Entries just removed from the tape, so the screen can offer to undo. */
    val removed: SharedFlow<List<CalculationHistory>> = _removed

    fun onDeleteEntry(entry: CalculationHistory) {
        viewModelScope.launch {
            historyRepository.deleteCalculation(entry)
            _removed.emit(listOf(entry))
        }
    }

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

    fun loadExpression(expression: String) {
        if (expression.isNotEmpty()) setState(CalcState(editor = Editor(expression), angleUnit = calc.value.angleUnit))
    }

    private fun setState(next: CalcState) {
        calc.value = next
        savedState[KEY_TEXT] = next.editor.text
        savedState[KEY_RESULT] = next.resultShown
    }

    private companion object {
        const val KEY_TEXT = "text"
        const val KEY_RESULT = "result_shown"
    }
}
