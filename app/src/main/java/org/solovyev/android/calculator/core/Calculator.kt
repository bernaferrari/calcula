package org.solovyev.android.calculator.core

import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import jscl.JsclArithmeticException
import jscl.NumeralBase
import jscl.math.Generic
import jscl.text.ParseInterruptedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.Check
import org.solovyev.android.calculator.CalculatorMessage
import org.solovyev.android.calculator.CalculatorMessages
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.ParseException
import org.solovyev.android.calculator.PreparedExpression
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.ToJsclTextProcessor
import org.solovyev.android.calculator.VariablesRegistry
import org.solovyev.android.calculator.calculations.CalculationCancelledEvent
import org.solovyev.android.calculator.calculations.CalculationFailedEvent
import org.solovyev.android.calculator.calculations.CalculationFinishedEvent
import org.solovyev.android.calculator.calculations.ConversionFailedEvent
import org.solovyev.android.calculator.calculations.ConversionFinishedEvent
import org.solovyev.android.calculator.functions.FunctionsRegistry
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.variables.CppVariable
import org.solovyev.common.msg.ListMessageRegistry
import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageRegistry
import org.solovyev.common.msg.MessageType
import com.ionspin.kotlin.bignum.integer.BigInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import jscl.math.function.Constants

@Singleton
class Calculator @Inject constructor(
    private val preferences: SharedPreferences,
    private val engine: Engine,
    private val preprocessor: ToJsclTextProcessor
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Volatile
    var calculateOnFly: Boolean = true
        private set

    // Events
    private val _calculationFinished = MutableSharedFlow<CalculationFinishedEvent>()
    val calculationFinished: SharedFlow<CalculationFinishedEvent> = _calculationFinished.asSharedFlow()

    private val _calculationFailed = MutableSharedFlow<CalculationFailedEvent>()
    val calculationFailed: SharedFlow<CalculationFailedEvent> = _calculationFailed.asSharedFlow()

    private val _calculationCancelled = MutableSharedFlow<CalculationCancelledEvent>()
    val calculationCancelled: SharedFlow<CalculationCancelledEvent> = _calculationCancelled.asSharedFlow()

    private val _conversionFinished = MutableSharedFlow<ConversionFinishedEvent>()
    val conversionFinished: SharedFlow<ConversionFinishedEvent> = _conversionFinished.asSharedFlow()

    private val _conversionFailed = MutableSharedFlow<ConversionFailedEvent>()
    val conversionFailed: SharedFlow<ConversionFailedEvent> = _conversionFailed.asSharedFlow()

    // For editor integration
    lateinit var editor: Editor

    fun init() {
        preferences.registerOnSharedPreferenceChangeListener(this)
        setCalculateOnFly(Preferences.Calculations.calculateOnFly.getPreference(preferences) ?: true)

        // Subscribe to editor changes
        mainScope.launch {
            editor.changedEvents.collect { e ->
                if (!calculateOnFly) return@collect
                if (!e.shouldEvaluate()) return@collect
                evaluate(JsclOperation.numeric, e.newState.getTextString(), e.newState.sequence)
            }
        }
    }

    fun evaluate(state: EditorState) {
        evaluate(JsclOperation.numeric, state.getTextString(), state.sequence)
    }

    fun evaluate() {
        val state = editor.state
        evaluate(JsclOperation.numeric, state.getTextString(), state.sequence)
    }

    fun simplify() {
        val state = editor.state
        evaluate(JsclOperation.simplify, state.getTextString(), state.sequence)
    }

    fun evaluate(operation: JsclOperation, expression: String, sequence: Long): Long {
        scope.launch {
            evaluateAsync(sequence, operation, expression)
        }
        return sequence
    }

    fun setCalculateOnFly(value: Boolean) {
        if (calculateOnFly != value) {
            calculateOnFly = value
            if (calculateOnFly) {
                evaluate()
            }
        }
    }

    private suspend fun evaluateAsync(
        sequence: Long,
        operation: JsclOperation,
        expression: String,
        messageRegistry: MessageRegistry = ListMessageRegistry()
    ) {
        val expr = expression.trim()
        if (expr.isEmpty()) {
            _calculationFinished.emit(CalculationFinishedEvent(operation, expr, sequence))
            return
        }

        var preparedExpression: PreparedExpression? = null
        try {
            preparedExpression = prepare(expr)

            try {
                val mathEngine = engine.getMathEngine()
                mathEngine.setMessageRegistry(messageRegistry)

                val result = operation.evaluateGeneric(preparedExpression.value, mathEngine)
                result.toString() // Check for ArithmeticOperationException

                val stringResult = operation.getFromProcessor(engine).process(result)
                _calculationFinished.emit(
                    CalculationFinishedEvent(
                        operation, expr, sequence, result, stringResult, collectMessages(messageRegistry)
                    )
                )
            } catch (e: JsclArithmeticException) {
                _calculationFailed.emit(CalculationFailedEvent(operation, expr, sequence, e))
            }
        } catch (e: ArithmeticException) {
            onException(
                sequence, operation, expr, messageRegistry, preparedExpression,
                ParseException(expr, CalculatorMessage(CalculatorMessages.msg_001, MessageType.error, e.message))
            )
        } catch (e: StackOverflowError) {
            onException(
                sequence, operation, expr, messageRegistry, preparedExpression,
                ParseException(expr, CalculatorMessage(CalculatorMessages.msg_002, MessageType.error))
            )
        } catch (e: jscl.text.ParseException) {
            onException(
                sequence, operation, expr, messageRegistry, preparedExpression,
                ParseException(e)
            )
        } catch (e: ParseInterruptedException) {
            _calculationCancelled.emit(CalculationCancelledEvent(operation, expr, sequence))
        } catch (e: ParseException) {
            onException(sequence, operation, expr, messageRegistry, preparedExpression, e)
        } catch (e: RuntimeException) {
            onException(
                sequence, operation, expr, messageRegistry, preparedExpression,
                ParseException(expr, CalculatorMessage(CalculatorMessages.syntax_error, MessageType.error))
            )
        }
    }

    private fun collectMessages(messageRegistry: MessageRegistry): List<Message> {
        if (!messageRegistry.hasMessage()) return emptyList()
        return try {
            val messages = mutableListOf<Message>()
            while (messageRegistry.hasMessage()) {
                messages.add(messageRegistry.getMessage())
            }
            messages
        } catch (e: Throwable) {
            Log.e("Calculator", e.message, e)
            emptyList()
        }
    }

    fun prepare(expression: String): PreparedExpression = preprocessor.process(expression)

    private suspend fun onException(
        sequence: Long,
        operation: JsclOperation,
        expression: String,
        messageRegistry: MessageRegistry,
        preparedExpression: PreparedExpression?,
        parseException: ParseException
    ) {
        if (operation == JsclOperation.numeric && preparedExpression?.hasUndefinedVariables() == true) {
            evaluateAsync(sequence, JsclOperation.simplify, expression, messageRegistry)
            return
        }
        _calculationFailed.emit(CalculationFailedEvent(operation, expression, sequence, parseException))
    }

    suspend fun convert(state: DisplayState, to: NumeralBase) {
        val value = state.result ?: return
        val from = engine.getMathEngine().getNumeralBase()
        if (from == to) return

        withContext(Dispatchers.Default) {
            try {
                val result = convert(value, to)
                _conversionFinished.emit(ConversionFinishedEvent(result, to, state))
            } catch (e: ConversionException) {
                _conversionFailed.emit(ConversionFailedEvent(state))
            }
        }
    }

    fun canConvert(generic: Generic, from: NumeralBase, to: NumeralBase): Boolean {
        if (from == to) return false
        return try {
            convert(generic, to)
            true
        } catch (e: ConversionException) {
            false
        }
    }

    fun updateAnsVariable(value: String) {
        val variablesRegistry = engine.variablesRegistry
        val variable = variablesRegistry.get(Constants.ANS)

        val builder = if (variable != null) CppVariable.builder(variable) else CppVariable.builder(Constants.ANS)
        builder.withValue(value)
        builder.withSystem(true)
        builder.withDescription(CalculatorMessages.getBundle().getString(CalculatorMessages.ans_description))

        variablesRegistry.addOrUpdate(builder.build().toJsclConstant(), variable)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if (Preferences.Calculations.calculateOnFly.key == key) {
            setCalculateOnFly(Preferences.Calculations.calculateOnFly.getPreference(prefs) ?: true)
        }
    }

    companion object {
        const val NO_SEQUENCE = -1L
        private val SEQUENCER = AtomicLong(NO_SEQUENCE)

        @JvmStatic
        fun nextSequence(): Long = SEQUENCER.incrementAndGet()

        @Throws(ConversionException::class)
        private fun convert(generic: Generic, to: NumeralBase): String {
            val value = generic.toBigInteger() ?: throw ConversionException()
            return to.toString(value)
        }
    }
}
