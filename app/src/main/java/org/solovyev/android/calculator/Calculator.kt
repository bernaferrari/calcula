package org.solovyev.android.calculator

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import jscl.JsclArithmeticException
import jscl.MathEngine
import jscl.NumeralBase
import jscl.math.Generic
import jscl.math.function.Constants
import jscl.text.ParseInterruptedException
import jscl.text.ParseException as JsclParseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.calculations.CalculationCancelledEvent
import org.solovyev.android.calculator.calculations.CalculationFailedEvent
import org.solovyev.android.calculator.calculations.CalculationFinishedEvent
import org.solovyev.android.calculator.calculations.ConversionFailedEvent
import org.solovyev.android.calculator.calculations.ConversionFinishedEvent
import org.solovyev.android.calculator.functions.FunctionsRegistry
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.variables.CppVariable
import org.solovyev.common.msg.ListMessageRegistry
import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageRegistry
import org.solovyev.common.msg.MessageType
import kotlinx.atomicfu.atomic
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Calculator @Inject constructor(
    private val appPreferences: AppPreferences,
    private val engine: Engine,
    private val preprocessor: ToJsclTextProcessor,
    private val editor: Editor,
    private val functionsRegistry: FunctionsRegistry,
    private val variablesRegistry: VariablesRegistry
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @VisibleForTesting
    private var synchronous = false

    @Volatile
    private var calculateOnFly = true
    private val initialized = AtomicBoolean(false)

    private val _calculationFinished = MutableSharedFlow<CalculationFinishedEvent>(replay = 1)
    val calculationFinished: SharedFlow<CalculationFinishedEvent> = _calculationFinished.asSharedFlow()

    private val _calculationFailed = MutableSharedFlow<CalculationFailedEvent>(replay = 1)
    val calculationFailed: SharedFlow<CalculationFailedEvent> = _calculationFailed.asSharedFlow()

    private val _calculationCancelled = MutableSharedFlow<CalculationCancelledEvent>(replay = 1)
    val calculationCancelled: SharedFlow<CalculationCancelledEvent> = _calculationCancelled.asSharedFlow()

    private val _conversionFinished = MutableSharedFlow<ConversionFinishedEvent>(replay = 1)
    val conversionFinished: SharedFlow<ConversionFinishedEvent> = _conversionFinished.asSharedFlow()

    private val _conversionFailed = MutableSharedFlow<ConversionFailedEvent>(replay = 1)
    val conversionFailed: SharedFlow<ConversionFailedEvent> = _conversionFailed.asSharedFlow()

    private val _calculationEvents = MutableSharedFlow<CalculationEvent>(extraBufferCapacity = 1)
    val calculationEvents: SharedFlow<CalculationEvent> = _calculationEvents.asSharedFlow()

    @VisibleForTesting
    fun setSynchronous() {
        synchronous = true
    }

    /**
     * Modern coroutine-based initialization.
     * Call from a background coroutine scope.
     */
    suspend fun initAsync() {
        if (!initialized.compareAndSet(false, true)) {
            return
        }
        engine.initAsync()
        setCalculateOnFly(appPreferences.settings.getCalculateOnFlyBlocking())
        observeCalculateOnFly()
        observeEditorChanges()
        observeRegistryChanges()
    }

    fun isCalculateOnFly(): Boolean = calculateOnFly

    fun setCalculateOnFly(calculateOnFly: Boolean) {
        if (this.calculateOnFly != calculateOnFly) {
            this.calculateOnFly = calculateOnFly
            if (this.calculateOnFly) {
                evaluate()
            }
        }
    }

    fun evaluate() {
        val state = editor.state
        evaluate(JsclOperation.numeric, state.getTextString(), state.sequence)
    }

    fun simplify() {
        val state = editor.state
        evaluate(JsclOperation.simplify, state.getTextString(), state.sequence)
    }

    fun evaluate(
        operation: JsclOperation,
        expression: String,
        sequence: Long
    ): Long {
        if (synchronous) {
            evaluateAsync(sequence, operation, expression)
        } else {
            scope.launch {
                evaluateAsync(sequence, operation, expression)
            }
        }
        return sequence
    }

    private fun evaluateAsync(sequence: Long, operation: JsclOperation, expression: String) {
        evaluateAsync(sequence, operation, expression, ListMessageRegistry())
    }

    private fun evaluateAsync(
        sequence: Long,
        operation: JsclOperation,
        expressionRaw: String,
        messageRegistry: MessageRegistry
    ) {
        val expression = expressionRaw.trim()
        if (TextUtils.isEmpty(expression)) {
            val event = CalculationFinishedEvent(operation, expression, sequence)
            emitCalculationFinished(event)
            return
        }

        var preparedExpression: PreparedExpression? = null
        try {
            preparedExpression = prepare(expression)

            try {
                val mathEngine: MathEngine = engine.getMathEngine()
                mathEngine.setMessageRegistry(messageRegistry)

                val result = operation.evaluateGeneric(preparedExpression.value, mathEngine)

                // NOTE: toString() method must be called here as ArithmeticOperationException may occur in it
                result.toString()

                val stringResult = operation.getFromProcessor(engine).process(result)
                val event = CalculationFinishedEvent(
                    operation, expression, sequence, result, stringResult,
                    collectMessages(messageRegistry)
                )
                emitCalculationFinished(event)

            } catch (exception: JsclArithmeticException) {
                val event = CalculationFailedEvent(operation, expression, sequence, exception)
                emitCalculationFailed(event)
            }
        } catch (exception: ArithmeticException) {
            onException(
                sequence, operation, expression, messageRegistry, preparedExpression,
                ParseException(
                    expression,
                    CalculatorMessage(CalculatorMessages.msg_001, MessageType.error, exception.message)
                )
            )
        } catch (exception: StackOverflowError) {
            onException(
                sequence, operation, expression, messageRegistry, preparedExpression,
                ParseException(expression, CalculatorMessage(CalculatorMessages.msg_002, MessageType.error))
            )
        } catch (exception: ParseInterruptedException) {
            val event = CalculationCancelledEvent(operation, expression, sequence)
            emitCalculationCancelled(event)
        } catch (exception: JsclParseException) {
            onException(
                sequence,
                operation,
                expression,
                messageRegistry,
                preparedExpression,
                ParseException(exception)
            )
        } catch (exception: ParseException) {
            onException(sequence, operation, expression, messageRegistry, preparedExpression, exception)
        }
    }

    fun prepare(expression: String): PreparedExpression = preprocessor.process(expression)

    private fun onException(
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
        val event = CalculationFailedEvent(operation, expression, sequence, parseException)
        emitCalculationFailed(event)
    }

    fun convert(state: DisplayState, to: NumeralBase) {
        val value = state.result ?: return
        val from = engine.getMathEngine().getNumeralBase()
        if (from == to) return

        if (synchronous) {
            try {
                val result = convert(value, to)
                emitConversionFinished(ConversionFinishedEvent(result, to, state))
            } catch (e: ConversionException) {
                emitConversionFailed(ConversionFailedEvent(state))
            }
        } else {
            scope.launch {
                try {
                    val result = convert(value, to)
                    emitConversionFinished(ConversionFinishedEvent(result, to, state))
                } catch (e: ConversionException) {
                    emitConversionFailed(ConversionFailedEvent(state))
                }
            }
        }
    }

    fun canConvert(generic: Generic, from: NumeralBase, to: NumeralBase): Boolean {
        if (from == to) {
            return false
        }
        return try {
            convert(generic, to)
            true
        } catch (e: ConversionException) {
            false
        }
    }

    internal fun updateAnsVariable(value: String) {
        val variable = variablesRegistry.get(Constants.ANS)
        val builder = if (variable != null) {
            CppVariable.builder(variable)
        } else {
            CppVariable.builder(Constants.ANS)
        }

        builder.withValue(value)
        builder.withSystem(true)
        builder.withDescription(CalculatorMessages.getBundle().getString(CalculatorMessages.ans_description))

        variablesRegistry.addOrUpdate(builder.build().toJsclConstant(), variable)
    }

    private fun emitCalculationFinished(event: CalculationFinishedEvent) {
        _calculationFinished.tryEmit(event)
        _calculationEvents.tryEmit(CalculationEvent.Finished(event))
        if (event.result != null && event.stringResult.isNotEmpty()) {
            updateAnsVariable(event.stringResult)
        }
    }

    private fun emitCalculationFailed(event: CalculationFailedEvent) {
        _calculationFailed.tryEmit(event)
        _calculationEvents.tryEmit(CalculationEvent.Failed(event))
    }

    private fun emitCalculationCancelled(event: CalculationCancelledEvent) {
        _calculationCancelled.tryEmit(event)
        _calculationEvents.tryEmit(CalculationEvent.Cancelled(event))
    }

    private fun emitConversionFinished(event: ConversionFinishedEvent) {
        _conversionFinished.tryEmit(event)
    }

    private fun emitConversionFailed(event: ConversionFailedEvent) {
        _conversionFailed.tryEmit(event)
    }

    private fun observeCalculateOnFly() {
        mainScope.launch {
            appPreferences.settings.calculateOnFly.collect { enabled ->
                setCalculateOnFly(enabled)
            }
        }
    }

    private fun observeEditorChanges() {
        mainScope.launch {
            editor.changedEvents.collect { event ->
                if (!calculateOnFly) return@collect
                if (!event.shouldEvaluate()) return@collect
                evaluate(JsclOperation.numeric, event.newState.getTextString(), event.newState.sequence)
            }
        }
    }

    private fun observeRegistryChanges() {
        mainScope.launch {
            functionsRegistry.events.collect {
                evaluate()
            }
        }
        mainScope.launch {
            variablesRegistry.addedEvents.collect {
                evaluate()
            }
        }
        mainScope.launch {
            variablesRegistry.removedEvents.collect {
                evaluate()
            }
        }
        mainScope.launch {
            variablesRegistry.changedEvents.collect { event ->
                if (event.newVariable.name != Constants.ANS) {
                    evaluate()
                }
            }
        }
    }

    private fun collectMessages(messageRegistry: MessageRegistry): List<Message> {
        if (messageRegistry !is ListMessageRegistry) {
            return emptyList()
        }
        val messages = mutableListOf<Message>()
        while (messageRegistry.hasMessage()) {
            messages.add(messageRegistry.getMessage())
        }
        return messages
    }

    sealed class CalculationEvent {
        data class Finished(val event: CalculationFinishedEvent) : CalculationEvent()
        data class Failed(val event: CalculationFailedEvent) : CalculationEvent()
        data class Cancelled(val event: CalculationCancelledEvent) : CalculationEvent()
    }

    companion object {
        const val NO_SEQUENCE = -1L

        private val sequencer = atomic(NO_SEQUENCE)

        fun nextSequence(): Long = sequencer.incrementAndGet()

        private fun convert(generic: Generic, to: NumeralBase): String {
            val value = generic.toBigInteger() ?: throw ConversionException()
            return to.toString(value)
        }
    }
}
