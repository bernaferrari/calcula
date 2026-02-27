package org.solovyev.android.calculator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.calculator.calculations.CalculationCancelledEvent
import org.solovyev.android.calculator.calculations.CalculationFailedEvent
import org.solovyev.android.calculator.calculations.CalculationFinishedEvent
import org.solovyev.android.calculator.calculations.ConversionFailedEvent
import org.solovyev.android.calculator.calculations.ConversionFinishedEvent
// FunctionsRegistry is in same package, unexpected import removed
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.variables.CppVariable 
import jscl.common.msg.ListMessageRegistry
import jscl.common.msg.MessageRegistry
import jscl.common.msg.MessageType
import jscl.common.msg.Message
import kotlinx.atomicfu.atomic
import jscl.math.function.Constants
import jscl.math.Generic
import jscl.NumeralBase
import kotlin.time.Clock
// import org.solovyev.android.calculator.preferences.PreferenceEntry

interface ResourceProvider {
    fun getString(id: String): String
}

class Calculator(
    private val appPreferences: AppPreferences,
    private val engine: Engine,
    private val preprocessor: ToJsclTextProcessor,
    private val editor: Editor,
    private val functionsRegistry: FunctionsRegistry,
    private val variablesRegistry: VariablesRegistry,
    private val resourceProvider: ResourceProvider // Injected
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _calculationFinished = MutableSharedFlow<CalculationFinishedEvent>(extraBufferCapacity = 1)
    val calculationFinished: SharedFlow<CalculationFinishedEvent> = _calculationFinished.asSharedFlow()

    private val _calculationFailed = MutableSharedFlow<CalculationFailedEvent>(extraBufferCapacity = 1)
    val calculationFailed: SharedFlow<CalculationFailedEvent> = _calculationFailed.asSharedFlow()

    private val _calculationCancelled = MutableSharedFlow<CalculationCancelledEvent>(extraBufferCapacity = 1)
    val calculationCancelled: SharedFlow<CalculationCancelledEvent> = _calculationCancelled.asSharedFlow()

    private val _conversionFinished = MutableSharedFlow<ConversionFinishedEvent>(extraBufferCapacity = 1)
    val conversionFinished: SharedFlow<ConversionFinishedEvent> = _conversionFinished.asSharedFlow()

    private val _conversionFailed = MutableSharedFlow<ConversionFailedEvent>(extraBufferCapacity = 1)
    val conversionFailed: SharedFlow<ConversionFailedEvent> = _conversionFailed.asSharedFlow()

    private val _calculationEvents = MutableSharedFlow<CalculationEvent>(extraBufferCapacity = 1)
    val calculationEvents: SharedFlow<CalculationEvent> = _calculationEvents.asSharedFlow()

    private val _lastEvaluationLatencyMs = MutableStateFlow<Long?>(null)
    val lastEvaluationLatencyMs: StateFlow<Long?> = _lastEvaluationLatencyMs.asStateFlow()

    private val evaluationRequests = MutableSharedFlow<EvaluationRequest>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var calculateOnFly: Boolean = true

    init {
        scope.launch {
            evaluationRequests.collectLatest { request ->
                evaluateInternal(request)
            }
        }
    }

    fun setCalculateOnFly(enabled: Boolean) {
        if (calculateOnFly != enabled) {
            calculateOnFly = enabled
            if (enabled) {
                evaluate()
            }
        }
    }

    fun evaluate() {
        val state = editor.state
        evaluate(JsclOperation.numeric, state.text, state.sequence)
    }

    fun evaluate(operation: JsclOperation, expression: String, sequence: Long) {
        val request = EvaluationRequest(operation, expression, sequence)
        if (evaluationRequests.tryEmit(request)) {
            return
        }
        scope.launch {
            evaluationRequests.emit(request)
        }
    }

    /**
     * Synchronously evaluates an expression for live preview.
     * Returns the result string or throws on error.
     */
    suspend fun evaluateForPreview(expression: String): String {
        return withContext(Dispatchers.Default) {
            try {
                val normalizedExpression = BitwiseInfixNormalizer.normalize(expression)
                val preparedExpression = preprocessor.process(normalizedExpression).value
                val genericResult = JsclOperation.numeric.evaluateGeneric(preparedExpression, engine.getMathEngine())
                JsclOperation.numeric.getFromProcessor(engine).process(genericResult)
            } catch (e: Exception) {
                ""
            }
        }
    }

    
    // ...

    fun updateAnsVariable(value: String) {
        val variable = variablesRegistry.get(jscl.math.function.Constants.ANS)
        val builder = if (variable != null) {
            CppVariable.builder(variable)
        } else {
            CppVariable.builder(jscl.math.function.Constants.ANS)
        }

        builder.withValue(value)
        builder.withSystem(true)
        builder.withDescription(resourceProvider.getString(CalculatorMessages.ans_description))

        variablesRegistry.addOrUpdate(builder.build().toJsclConstant())
    }

    fun isCalculateOnFly(): Boolean = calculateOnFly

    private suspend fun evaluateInternal(request: EvaluationRequest) {
        if (calculateOnFly && request.sequence < editor.state.sequence) {
            return
        }
        val startedAtMs = Clock.System.now().toEpochMilliseconds()
        try {
            val normalizedExpression = BitwiseInfixNormalizer.normalize(request.expression)
            val preparedExpression = preprocessor.process(normalizedExpression).value
            val genericResult = request.operation.evaluateGeneric(preparedExpression, engine.getMathEngine())
            val result = request.operation.getFromProcessor(engine).process(genericResult)
            val messages = collectMessages(engine.getMathEngine().getMessageRegistry())
            val durationMs = (Clock.System.now().toEpochMilliseconds() - startedAtMs).coerceAtLeast(0L)
            _lastEvaluationLatencyMs.value = durationMs

            if (calculateOnFly && request.sequence < editor.state.sequence) {
                return
            }

            emitCalculationFinished(
                CalculationFinishedEvent(
                    request.operation,
                    request.expression,
                    request.sequence,
                    genericResult,
                    result,
                    messages
                )
            )
        } catch (e: Exception) {
            if (e is CancellationException) {
                return
            }
            val durationMs = (Clock.System.now().toEpochMilliseconds() - startedAtMs).coerceAtLeast(0L)
            _lastEvaluationLatencyMs.value = durationMs
            if (calculateOnFly && request.sequence < editor.state.sequence) {
                return
            }
            emitCalculationFailed(CalculationFailedEvent(request.operation, request.expression, request.sequence, e))
        }
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

    private data class EvaluationRequest(
        val operation: JsclOperation,
        val expression: String,
        val sequence: Long
    )

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
