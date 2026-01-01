package org.solovyev.android.calculator

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.NumeralBase
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
import jscl.math.precision.Real
// duplicate Real import removed
// Registry imports removed as they are in same package
import org.solovyev.android.calculator.preferences.PreferenceEntry

class Engine(
    private val mathEngine: JsclMathEngine,
    private val appPreferences: AppPreferences,
    private val errorReporter: ErrorReporter,
    val functionsRegistry: FunctionsRegistry,
    val variablesRegistry: VariablesRegistry,
    val operatorsRegistry: OperatorsRegistry,
    val postfixFunctionsRegistry: PostfixFunctionsRegistry
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _multiplicationSign = MutableStateFlow<String?>("×")
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
        scope.launch {
            val baseFlow = combine(
                appPreferences.settings.angleUnit,
                appPreferences.settings.numeralBase,
                appPreferences.settings.outputPrecision
            ) { angleUnitId, numeralBaseId, precision ->
                val angleUnit = AngleUnit.entries.getOrElse(angleUnitId) { AngleUnit.deg } // Assuming ordinal or mapping
                val numeralBase = NumeralBase.entries.getOrElse(numeralBaseId) { NumeralBase.dec }
                BaseSnapshot(angleUnit, numeralBase, precision)
            }
            val outputFlow = combine(
                appPreferences.settings.outputNotation,
                appPreferences.settings.outputSeparator,
                appPreferences.settings.multiplicationSign
            ) { notationId, separator, multiplicationSign ->
                val notation = Notation.entries.find { it.notationId == notationId } ?: Notation.dec
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
        val nameResId: String // Changed to String Key
    ) : PreferenceEntry {
        dec(Real.NumberFormat.FSE_NONE, "cpp_number_format_dec"),
        eng(Real.NumberFormat.FSE_ENG, "cpp_number_format_eng"),
        sci(Real.NumberFormat.FSE_SCI, "cpp_number_format_sci");

        override val id: CharSequence
            get() = name

        override fun getName(context: Any): CharSequence {
            // Context is generic Any, user must cast it or we use ResourceProvider in a different method
            // For now, return name or implement properly with ResourceProvider
             if (context is ResourceProvider) {
                 return context.getString(nameResId)
             }
             return name
        }
    }

    object ChangedEvent

    object Preferences {
        // These can be removed or kept as constants if needed solely for keys
        // But logic should move to AppPreferences implementation
         const val multiplicationSignKey = "engine.multiplicationSign"
         const val multiplicationSignDefault = "×"
    }

    companion object {
        
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
