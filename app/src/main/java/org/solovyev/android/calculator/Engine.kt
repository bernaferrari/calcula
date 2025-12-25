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

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import dagger.hilt.android.qualifiers.ApplicationContext
import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.MathEngine
import jscl.NumeralBase
import jscl.math.operator.Operator
import jscl.text.Identifier
import jscl.text.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import midpcalc.Real
import org.solovyev.android.Check
import org.solovyev.android.calculator.Preferences.Gui
import org.solovyev.android.calculator.functions.FunctionsRegistry
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.operators.OperatorsRegistry
import org.solovyev.android.calculator.operators.PostfixFunctionsRegistry
import org.solovyev.android.calculator.preferences.PreferenceEntry
import org.solovyev.android.prefs.IntegerPreference
import org.solovyev.android.prefs.Preference
import org.solovyev.android.prefs.StringPreference
import org.solovyev.common.NumberFormatter
import org.solovyev.common.text.CharacterMapper
import org.solovyev.common.text.EnumMapper
import org.solovyev.common.text.NumberMapper
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Engine @Inject constructor(
    private val mathEngine: JsclMathEngine,
    @ApplicationContext private val context: Context,
    private val preferences: SharedPreferences,
    private val errorReporter: ErrorReporter,
    val functionsRegistry: FunctionsRegistry,
    val variablesRegistry: VariablesRegistry,
    val operatorsRegistry: OperatorsRegistry,
    val postfixFunctionsRegistry: PostfixFunctionsRegistry
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _multiplicationSign = MutableStateFlow<String?>(Preferences.multiplicationSign.defaultValue)
    val multiplicationSign: StateFlow<String?> = _multiplicationSign.asStateFlow()

    private val _changedEvents = MutableSharedFlow<ChangedEvent>(extraBufferCapacity = 1)
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

    init {
        mathEngine.setPrecision(5)
        mathEngine.setGroupingSeparator(JsclMathEngine.GROUPING_SEPARATOR_DEFAULT)
    }

    fun init(initThread: Executor) {
        Check.isMainThread()
        checkPreferences()
        preferences.registerOnSharedPreferenceChangeListener(this)
        applyPreferences()
        initThread.execute {
            initAsync()
        }
    }

    private fun checkPreferences() {
        val oldVersion = if (Preferences.version.isSet(preferences)) {
            Preferences.version.getPreference(preferences)
        } else {
            0
        }
        val newVersion = Preferences.version.defaultValue
        if (oldVersion == newVersion) {
            return
        }
        val editor = preferences.edit()
        when (oldVersion) {
            0 -> {
                migratePreference(Preferences.Output.separator, "org.solovyev.android.calculator.CalculatorActivity_calc_grouping_separator", editor)
                migratePreference(Preferences.multiplicationSign, "org.solovyev.android.calculator.CalculatorActivity_calc_multiplication_sign", editor)
                migratePreference(Preferences.numeralBase, "org.solovyev.android.calculator.CalculatorActivity_numeral_bases", editor)
                migratePreference(Preferences.angleUnit, "org.solovyev.android.calculator.CalculatorActivity_angle_units", editor)
                migratePreference(Preferences.Output.precision, "org.solovyev.android.calculator.CalculatorModel_result_precision", editor)
                if (preferences.contains("engine.output.science_notation")) {
                    val scientific = preferences.getBoolean("engine.output.science_notation", false)
                    Preferences.Output.notation.putPreference(editor, if (scientific) Notation.sci else Notation.dec)
                }
                if (preferences.contains("org.solovyev.android.calculator.CalculatorModel_round_result")) {
                    val round = preferences.getBoolean("org.solovyev.android.calculator.CalculatorModel_round_result", true)
                    if (!round) {
                        Preferences.Output.precision.putPreference(editor, NumberFormatter.ENG_PRECISION as Integer?)
                    }
                }
                editor.apply()
                initPreferences(editor)
            }
            1 -> {
                migratePreference(Preferences.Output.separator, "engine.groupingSeparator", editor)
                if (preferences.contains("engine.output.scientificNotation")) {
                    val scientific = preferences.getBoolean("engine.output.scientificNotation", false)
                    Preferences.Output.notation.putPreference(editor, if (scientific) Notation.sci else Notation.dec)
                }
                if (preferences.contains("engine.output.round")) {
                    val round = preferences.getBoolean("engine.output.round", true)
                    if (!round) {
                        Preferences.Output.precision.putPreference(editor, NumberFormatter.ENG_PRECISION as Integer?)
                    }
                }
                editor.apply()
                initPreferences(editor)
            }
            2 -> {
                val precision = Preferences.Output.precision.getPreference(preferences)
                val mode = Gui.mode.getPreference(preferences)
                if (precision == NumberFormatter.MAX_PRECISION && mode == Gui.Mode.engineer) {
                    Preferences.Output.precision.putPreference(editor, NumberFormatter.ENG_PRECISION as Integer?)
                }
            }
        }
        Preferences.version.putDefault(editor)
        editor.apply()
    }

    private fun initPreferences(editor: SharedPreferences.Editor) {
        if (!Preferences.Output.separator.isSet(preferences)) {
            val locale = Locale.getDefault()
            if (locale != null) {
                val decimalFormatSymbols = DecimalFormatSymbols(locale)
                val index = MathType.grouping_separator.tokens.indexOf(decimalFormatSymbols.groupingSeparator.toString())
                val separator = if (index >= 0) {
                    MathType.grouping_separator.tokens[index][0]
                } else {
                    JsclMathEngine.GROUPING_SEPARATOR_DEFAULT
                }
                Preferences.Output.separator.putPreference(editor, separator)
            }
        }

        Preferences.angleUnit.tryPutDefault(preferences, editor)
        Preferences.numeralBase.tryPutDefault(preferences, editor)
        Preferences.Output.notation.tryPutDefault(preferences, editor)
        Preferences.Output.separator.tryPutDefault(preferences, editor)
        Preferences.Output.precision.tryPutDefault(preferences, editor)
    }

    private fun <T> migratePreference(
        preference: StringPreference<T>,
        oldKey: String,
        editor: SharedPreferences.Editor
    ) {
        if (!preferences.contains(oldKey)) {
            return
        }
        editor.putString(preference.key, preferences.getString(oldKey, null))
    }

    @VisibleForTesting
    fun initAsync() {
        init(variablesRegistry)
        init(functionsRegistry)
        init(operatorsRegistry)
        init(postfixFunctionsRegistry)
    }

    private fun init(registry: EntitiesRegistry<*>) {
        try {
            registry.init()
        } catch (e: Exception) {
            errorReporter.onException(e)
        }
    }

    fun getMathEngine(): JsclMathEngine {
        return mathEngine
    }

    private fun applyPreferences() {
        Check.isMainThread()
        mathEngine.setAngleUnits(Preferences.angleUnit.getPreference(preferences) ?: AngleUnit.deg)
        mathEngine.setNumeralBase(Preferences.numeralBase.getPreference(preferences) ?: NumeralBase.dec)
        _multiplicationSign.value = Preferences.multiplicationSign.getPreference(preferences)

        mathEngine.setPrecision(Preferences.Output.precision.getPreference(preferences)?.toInt() ?: 5)
        mathEngine.setNotation(Preferences.Output.notation.getPreference(preferences)?.notationId ?: Real.NumberFormat.FSE_NONE)
        mathEngine.setGroupingSeparator(Preferences.Output.separator.getPreference(preferences) ?: JsclMathEngine.GROUPING_SEPARATOR_DEFAULT)

        scope.launch {
            _changedEvents.emit(ChangedEvent)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key != null && Preferences.preferenceKeys.contains(key)) {
            applyPreferences()
        }
    }

    enum class Notation(
        val notationId: Int,
        @StringRes val nameRes: Int
    ) : PreferenceEntry {
        dec(Real.NumberFormat.FSE_NONE, R.string.cpp_number_format_dec),
        eng(Real.NumberFormat.FSE_ENG, R.string.cpp_number_format_eng),
        sci(Real.NumberFormat.FSE_SCI, R.string.cpp_number_format_sci);

        override val id: CharSequence
            get() = name

        override fun getName(context: Context): CharSequence {
            return context.getString(nameRes)
        }
    }

    object ChangedEvent

    object Preferences {
        val multiplicationSign = StringPreference.of("engine.multiplicationSign", "×")
        val numeralBase = StringPreference.ofTypedValue(
            "engine.numeralBase",
            "dec",
            EnumMapper.of(NumeralBase::class.java)
        )
        val angleUnit = StringPreference.ofTypedValue(
            "engine.angleUnit",
            "deg",
            EnumMapper.of(AngleUnit::class.java)
        )
        val version: Preference<Int> = IntegerPreference.of("engine.version", 3)

        val preferenceKeys: List<String> = listOf(
            multiplicationSign.key,
            numeralBase.key,
            angleUnit.key,
            Output.precision.key,
            Output.notation.key,
            Output.separator.key
        )

        @StringRes
        fun angleUnitName(angleUnit: AngleUnit): Int = when (angleUnit) {
            AngleUnit.deg -> R.string.cpp_deg
            AngleUnit.rad -> R.string.cpp_rad
            AngleUnit.grad -> R.string.cpp_grad
            AngleUnit.turns -> R.string.cpp_turns
            else -> 0
        }

        @StringRes
        fun numeralBaseName(numeralBase: NumeralBase): Int = when (numeralBase) {
            NumeralBase.bin -> R.string.cpp_bin
            NumeralBase.oct -> R.string.cpp_oct
            NumeralBase.dec -> R.string.cpp_dec
            NumeralBase.hex -> R.string.cpp_hex
            else -> 0
        }

        object Output {
            val precision = StringPreference.ofTypedValue(
                "engine.output.precision",
                "5",
                NumberMapper.of(Integer::class.java)
            )
            val notation = StringPreference.ofEnum(
                "engine.output.notation",
                Notation.dec,
                Notation::class.java
            )
            val separator = StringPreference.ofTypedValue(
                "engine.output.separator",
                JsclMathEngine.GROUPING_SEPARATOR_DEFAULT,
                CharacterMapper
            )
        }
    }

    companion object {
        @JvmStatic
        fun isValidName(name: String?): Boolean {
            if (name.isNullOrEmpty()) {
                return false
            }
            return try {
                val parsed = Identifier.parser.parse(Parser.Parameters.get(name), null)
                parsed == name
            } catch (e: jscl.text.ParseException) {
                false
            }
        }
    }
}
