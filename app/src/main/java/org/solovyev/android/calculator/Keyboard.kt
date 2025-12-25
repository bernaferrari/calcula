/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator

import android.content.SharedPreferences
import android.text.Spannable
import android.util.Log
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
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
import org.solovyev.android.Check
import org.solovyev.android.calculator.Engine.Preferences.numeralBase
import org.solovyev.android.calculator.buttons.CppSpecialButton
import org.solovyev.android.calculator.ga.Ga
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.memory.Memory
import org.solovyev.android.calculator.text.NumberSpan
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Keyboard @Inject constructor(
    private val preferences: SharedPreferences,
    private val bus: Bus,
    private val editor: Editor,
    private val display: Display,
    private val history: History,
    private val memory: Lazy<Memory>,
    private val calculator: Calculator,
    private val engine: Engine,
    private val ga: Lazy<Ga>,
    private val clipboard: Lazy<Clipboard>,
    private val launcher: ActivityLauncher
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val mathType = MathType.Result()

    private val _vibrateOnKeypress = MutableStateFlow(
        Preferences.Gui.vibrateOnKeypress.getPreference(preferences) ?: false
    )
    val vibrateOnKeypress: StateFlow<Boolean> = _vibrateOnKeypress.asStateFlow()

    private val _numberMode = MutableStateFlow(numeralBase.getPreference(preferences) ?: NumeralBase.dec)
    val numberMode: StateFlow<NumeralBase> = _numberMode.asStateFlow()

    private val _highContrast = MutableStateFlow(
        Preferences.Gui.highContrast.getPreference(preferences) ?: false
    )
    val highContrast: StateFlow<Boolean> = _highContrast.asStateFlow()

    init {
        bus.register(this)
        preferences.registerOnSharedPreferenceChangeListener(this)
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
            CppSpecialButton.copy -> bus.post(Display.CopyOperation())
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
            else -> Check.shouldNotHappen()
        }
    }

    @Subscribe
    fun onCursorMoved(e: Editor.CursorMovedEvent) {
        updateNumberMode(e.state)
    }

    @Subscribe
    fun onEditorChanged(e: Editor.ChangedEvent) {
        updateNumberMode(e.newState)
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
        val text = state.text as Spannable
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
        bus.post(NumberModeChangedEvent(newNumberMode))
    }

    private fun equalsButtonPressed() {
        if (!calculator.isCalculateOnFly()) {
            calculator.evaluate()
            return
        }

        val state = display.getState()
        if (!state.valid) {
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

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        when {
            Preferences.Gui.vibrateOnKeypress.isSameKey(key ?: "") -> {
                _vibrateOnKeypress.value = Preferences.Gui.vibrateOnKeypress.getPreference(preferences) ?: false
            }
            numeralBase.isSameKey(key ?: "") -> {
                setNumberMode(numeralBase.getPreference(preferences) ?: NumeralBase.dec)
            }
            Preferences.Gui.highContrast.isSameKey(key ?: "") -> {
                _highContrast.value = Preferences.Gui.highContrast.getPreference(preferences) ?: false
            }
        }
    }

    data class NumberModeChangedEvent(val mode: NumeralBase)
}
