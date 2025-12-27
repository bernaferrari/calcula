package org.solovyev.android.calculator

import android.content.Context
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import midpcalc.Real
import org.solovyev.android.Check
import org.solovyev.android.calculator.functions.FunctionsRegistry
import org.solovyev.android.calculator.operators.OperatorsRegistry
import org.solovyev.android.calculator.operators.PostfixFunctionsRegistry
import org.solovyev.android.calculator.preferences.PreferenceEntry
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.prefs.IntegerPreference
import org.solovyev.android.prefs.Preference
import org.solovyev.android.prefs.StringPreference
import org.solovyev.common.text.CharacterMapper
import org.solovyev.common.text.EnumMapper
import org.solovyev.common.text.NumberKind
import org.solovyev.common.text.NumberMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Engine @Inject constructor(
    private val mathEngine: JsclMathEngine,
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val errorReporter: ErrorReporter,
    val functionsRegistry: FunctionsRegistry,
    val variablesRegistry: VariablesRegistry,
    val operatorsRegistry: OperatorsRegistry,
    val postfixFunctionsRegistry: PostfixFunctionsRegistry
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _multiplicationSign = MutableStateFlow<String?>(Preferences.multiplicationSign.defaultValue)
    val multiplicationSign: StateFlow<String?> = _multiplicationSign.asStateFlow()

    private val _changedEvents = MutableSharedFlow<ChangedEvent>(extraBufferCapacity = 1)
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

    init {
        mathEngine.setPrecision(5)
        mathEngine.setGroupingSeparator(JsclMathEngine.GROUPING_SEPARATOR_DEFAULT)
    }

    suspend fun initAsync() {
        withContext(Dispatchers.Main) {
            observePreferences()
        }
        withContext(Dispatchers.Default) {
            init(variablesRegistry)
            init(functionsRegistry)
            init(operatorsRegistry)
            init(postfixFunctionsRegistry)
        }
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

    private fun observePreferences() {
        Check.isMainThread()
        scope.launch {
            val baseFlow = combine(
                appPreferences.settings.angleUnit,
                appPreferences.settings.numeralBase,
                appPreferences.settings.outputPrecision
            ) { angleUnit, numeralBase, precision ->
                BaseSnapshot(angleUnit, numeralBase, precision)
            }
            val outputFlow = combine(
                appPreferences.settings.outputNotation,
                appPreferences.settings.outputSeparator,
                appPreferences.settings.multiplicationSign
            ) { notation, separator, multiplicationSign ->
                OutputSnapshot(notation, separator, multiplicationSign)
            }
            combine(baseFlow, outputFlow) { base, output ->
                PreferenceSnapshot(
                    angleUnit = base.angleUnit,
                    numeralBase = base.numeralBase,
                    precision = base.precision,
                    notation = output.notation,
                    separator = output.separator,
                    multiplicationSign = output.multiplicationSign
                )
            }.collect { snapshot ->
                mathEngine.setAngleUnits(snapshot.angleUnit)
                mathEngine.setNumeralBase(snapshot.numeralBase)
                _multiplicationSign.value = snapshot.multiplicationSign
                mathEngine.setPrecision(snapshot.precision)
                mathEngine.setNotation(snapshot.notation.notationId)
                mathEngine.setGroupingSeparator(snapshot.separator)
                _changedEvents.emit(ChangedEvent)
            }
        }
    }

    private data class PreferenceSnapshot(
        val angleUnit: AngleUnit,
        val numeralBase: NumeralBase,
        val precision: Int,
        val notation: Notation,
        val separator: Char,
        val multiplicationSign: String?
    )

    private data class BaseSnapshot(
        val angleUnit: AngleUnit,
        val numeralBase: NumeralBase,
        val precision: Int
    )

    private data class OutputSnapshot(
        val notation: Notation,
        val separator: Char,
        val multiplicationSign: String
    )

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
        }

        @StringRes
        fun numeralBaseName(numeralBase: NumeralBase): Int = when (numeralBase) {
            NumeralBase.bin -> R.string.cpp_bin
            NumeralBase.oct -> R.string.cpp_oct
            NumeralBase.dec -> R.string.cpp_dec
            NumeralBase.hex -> R.string.cpp_hex
        }

        object Output {
            val precision = StringPreference.ofTypedValue(
                "engine.output.precision",
                "5",
                NumberMapper.of(NumberKind.Int)
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
