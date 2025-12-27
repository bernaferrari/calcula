package org.solovyev.android.calculator

import android.text.Spannable
import android.util.Log
import dagger.Lazy
import jscl.NumeralBase
import jscl.math.Expression
import jscl.math.Generic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.solovyev.android.Check
import org.solovyev.android.calculator.buttons.CppSpecialButton
import org.solovyev.android.calculator.ga.Ga
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.memory.Memory
import org.solovyev.android.calculator.text.NumberSpan
import org.solovyev.android.calculator.di.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Keyboard @Inject constructor(
    private val appPreferences: AppPreferences,
    private val editor: Editor,
    private val display: Display,
    private val history: History,
    private val memory: Lazy<Memory>,
    private val calculator: Calculator,
    private val engine: Engine,
    private val ga: Lazy<Ga>,
    private val clipboard: Lazy<Clipboard>,
    private val launcher: ActivityLauncher
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val mathType = MathType.Result()

    private val _vibrateOnKeypress = MutableStateFlow(
        appPreferences.settings.vibrateOnKeypressBlocking()
    )
    val vibrateOnKeypress: StateFlow<Boolean> = _vibrateOnKeypress.asStateFlow()

    private val _numberMode = MutableStateFlow(appPreferences.settings.getNumeralBaseBlocking())
    val numberMode: StateFlow<NumeralBase> = _numberMode.asStateFlow()

    private val _highContrast = MutableStateFlow(appPreferences.settings.getHighContrastBlocking())
    val highContrast: StateFlow<Boolean> = _highContrast.asStateFlow()

    init {
        observePreferences()
        scope.launch {
            editor.changedEvents.collect { event ->
                updateNumberMode(event.newState)
            }
        }
        scope.launch {
            editor.cursorMovedEvents.collect { event ->
                updateNumberMode(event.state)
            }
        }
    }

    fun buttonPressed(text: String?): Boolean {
        if (text.isNullOrEmpty()) {
            return false
        }

        if (text.length == 1) {
            val glyph = text[0]
            val button = CppSpecialButton.getByGlyph(glyph)
            if (button != null) {
                ga.get().onButtonPressed(button.action)
                handleSpecialAction(button)
                return true
            }
        }

        ga.get().onButtonPressed(text)
        if (!processSpecialAction(text)) {
            processText(prepareText(text))
        }
        return true
    }

    private fun processText(text: String) {
        var cursorPositionOffset = 0
        val textToBeInserted = StringBuilder(text)

        MathType.getType(text, 0, false, mathType, engine)
        when (mathType.type) {
            MathType.function -> {
                textToBeInserted.append("()")
                cursorPositionOffset = -1
            }
            MathType.operator -> {
                textToBeInserted.append("()")
                cursorPositionOffset = -1
            }
            MathType.comma -> {
                textToBeInserted.append(" ")
            }
            else -> {}
        }

        if (cursorPositionOffset == 0) {
            if (MathType.groupSymbols.contains(text)) {
                cursorPositionOffset = -1
            }
        }

        editor.insert(textToBeInserted.toString(), cursorPositionOffset)
    }

    private fun prepareText(text: String): String {
        return when (text) {
            "(  )", "( )" -> "()"
            else -> text
        }
    }

    private fun processSpecialAction(action: String): Boolean {
        val button = CppSpecialButton.getByAction(action) ?: return false
        handleSpecialAction(button)
        return true
    }

    private fun handleSpecialAction(button: CppSpecialButton) {
        when (button) {
            CppSpecialButton.history -> launcher.showHistory()
            CppSpecialButton.history_undo -> history.undo()
            CppSpecialButton.history_redo -> history.redo()
            CppSpecialButton.cursor_right -> editor.moveCursorRight()
            CppSpecialButton.cursor_to_end -> editor.setCursorOnEnd()
            CppSpecialButton.cursor_left -> editor.moveCursorLeft()
            CppSpecialButton.cursor_to_start -> editor.setCursorOnStart()
            CppSpecialButton.settings -> launcher.showSettings()
            CppSpecialButton.settings_widget -> launcher.showWidgetSettings()
            CppSpecialButton.like -> launcher.openFacebook()
            CppSpecialButton.memory -> memory.get().requestValue()
            CppSpecialButton.memory_plus -> handleMemoryButton(true)
            CppSpecialButton.memory_minus -> handleMemoryButton(false)
            CppSpecialButton.memory_clear -> memory.get().clear()
            CppSpecialButton.erase -> editor.erase()
            CppSpecialButton.paste -> {
                val text = clipboard.get().getText()
                if (text.isNotEmpty()) {
                    editor.insert(text)
                }
            }
            CppSpecialButton.copy -> display.copy()
            CppSpecialButton.brackets_wrap -> handleBracketsWrap()
            CppSpecialButton.equals -> equalsButtonPressed()
            CppSpecialButton.clear -> editor.clear()
            CppSpecialButton.functions -> launcher.showFunctions()
            CppSpecialButton.function_add -> launcher.showFunctionEditor()
            CppSpecialButton.var_add -> launcher.showConstantEditor()
            CppSpecialButton.plot_add -> launcher.plotDisplayedExpression()
            CppSpecialButton.open_app -> launcher.openApp()
            CppSpecialButton.vars -> launcher.showVariables()
            CppSpecialButton.operators -> launcher.showOperators()
            CppSpecialButton.simplify -> calculator.simplify()
        }
    }

    private fun updateNumberMode(state: EditorState) {
        if (state.text !is Spannable) {
            setNumberMode(NumeralBase.dec)
            return
        }
        if (state.selection < 0) {
            setNumberMode(NumeralBase.dec)
            return
        }
        val text = state.text
        val spans = text.getSpans(state.selection, state.selection, NumberSpan::class.java)
        if (spans != null && spans.isNotEmpty()) {
            setNumberMode(spans[0].numeralBase)
            return
        }
        setNumberMode(NumeralBase.dec)
    }

    private fun setNumberMode(newNumberMode: NumeralBase) {
        if (_numberMode.value == newNumberMode) {
            return
        }
        _numberMode.value = newNumberMode
    }

    private fun equalsButtonPressed() {
        if (!calculator.isCalculateOnFly()) {
            calculator.evaluate()
            return
        }

        val state = display.getState()
        val editorState = editor.getState()
        if (!state.valid || state.sequence != editorState.sequence || state.text.isEmpty()) {
            calculator.evaluate()
            return
        }
        editor.setText(state.text)
    }

    fun handleBracketsWrap() {
        val state = editor.getState()
        val cursorPosition = state.selection
        val oldText = state.text
        editor.setText(
            "(" + oldText.subSequence(0, cursorPosition) + ")" + oldText.subSequence(cursorPosition, oldText.length),
            cursorPosition + 2
        )
    }

    private fun handleMemoryButton(plus: Boolean): Boolean {
        val state = display.getState()
        if (!state.valid) {
            return false
        }
        var value: Generic? = state.result
        if (value == null) {
            try {
                value = Expression.valueOf(state.text)
            } catch (e: jscl.text.ParseException) {
                Log.w(App.TAG, e.message, e)
            }
        }
        if (value == null) {
            memory.get().requestShow()
            return false
        }
        if (plus) {
            memory.get().add(value)
        } else {
            memory.get().subtract(value)
        }
        return true
    }

    private fun observePreferences() {
        scope.launch {
            appPreferences.settings.vibrateOnKeypress.collect { enabled ->
                _vibrateOnKeypress.value = enabled
            }
        }
        scope.launch {
            appPreferences.settings.numeralBase.collect { base ->
                setNumberMode(base)
            }
        }
        scope.launch {
            appPreferences.settings.highContrast.collect { enabled ->
                _highContrast.value = enabled
            }
        }
    }

}
