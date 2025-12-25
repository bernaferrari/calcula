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
import android.text.TextUtils
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import jscl.JsclArithmeticException
import jscl.MathEngine
import jscl.NumeralBase
import jscl.math.Generic
import jscl.math.function.Constants
import jscl.text.ParseInterruptedException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.solovyev.android.Check
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
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import javax.measure.converter.ConversionException

@Singleton
class Calculator @Inject constructor(
    private val preferences: SharedPreferences,
    internal val bus: Bus
) : SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var editor: Editor

    @Inject
    lateinit var engine: Engine

    @Inject
    lateinit var preprocessor: ToJsclTextProcessor

    private val executor = TaskExecutor()

    @Volatile
    private var calculateOnFly = true

    private val _calculationEvents = MutableSharedFlow<CalculationEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val calculationEvents: SharedFlow<CalculationEvent> = _calculationEvents.asSharedFlow()

    init {
        bus.register(this)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    @VisibleForTesting
    fun setSynchronous() {
        executor.setSynchronous()
    }

    fun evaluate() {
        val state = editor.getState()
        evaluate(JsclOperation.numeric, state.getTextString(), state.sequence)
    }

    fun simplify() {
        val state = editor.getState()
        evaluate(JsclOperation.simplify, state.getTextString(), state.sequence)
    }

    fun evaluate(
        operation: JsclOperation,
        expression: String,
        sequence: Long
    ): Long {
        executor.execute({
            evaluateAsync(sequence, operation, expression)
        }, true)
        return sequence
    }

    fun init(init: Executor) {
        engine.init(init)
        setCalculateOnFly(Preferences.Calculations.calculateOnFly.getPreference(preferences) ?: true)
    }

    /**
     * Modern coroutine-based initialization.
     * Call from a background coroutine scope.
     */
    suspend fun initAsync() {
        engine.initAsync()
        setCalculateOnFly(Preferences.Calculations.calculateOnFly.getPreference(preferences) ?: true)
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
            bus.post(event)
            _calculationEvents.tryEmit(CalculationEvent.Finished(event))
            return
        }

        var preparedExpression: PreparedExpression? = null
        try {
            preparedExpression = prepare(expression)

            try {
                val mathEngine = engine.getMathEngine()
                mathEngine.setMessageRegistry(messageRegistry)

                val result = operation.evaluateGeneric(preparedExpression.value, mathEngine)

                // NOTE: toString() method must be called here as ArithmeticOperationException may occur in it
                result.toString()

                val stringResult = operation.getFromProcessor(engine).process(result)
                val event = CalculationFinishedEvent(
                    operation, expression, sequence, result, stringResult,
                    collectMessages(messageRegistry)
                )
                bus.post(event)
                _calculationEvents.tryEmit(CalculationEvent.Finished(event))

            } catch (exception: JsclArithmeticException) {
                val event = CalculationFailedEvent(operation, expression, sequence, exception)
                bus.post(event)
                _calculationEvents.tryEmit(CalculationEvent.Failed(event))
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
        } catch (exception: jscl.text.ParseException) {
            onException(sequence, operation, expression, messageRegistry, preparedExpression, ParseException(exception))
        } catch (exception: ParseInterruptedException) {
            val event = CalculationCancelledEvent(operation, expression, sequence)
            bus.post(event)
            _calculationEvents.tryEmit(CalculationEvent.Cancelled(event))
        } catch (exception: ParseException) {
            onException(sequence, operation, expression, messageRegistry, preparedExpression, exception)
        } catch (exception: RuntimeException) {
            onException(
                sequence, operation, expression, messageRegistry, preparedExpression,
                ParseException(expression, CalculatorMessage(CalculatorMessages.syntax_error, MessageType.error))
            )
        }
    }

    private fun collectMessages(messageRegistry: MessageRegistry): List<Message> {
        if (!messageRegistry.hasMessage()) {
            return emptyList()
        }

        return try {
            buildList {
                while (messageRegistry.hasMessage()) {
                    add(messageRegistry.getMessage())
                }
            }
        } catch (exception: Throwable) {
            // Several threads might use the same instance of MessageRegistry, as no proper synchronization is done
            Log.e("Calculator", exception.message, exception)
            emptyList()
        }
    }

    fun prepare(expression: String): PreparedExpression {
        return preprocessor.process(expression)
    }

    private fun onException(
        sequence: Long,
        operation: JsclOperation,
        expression: String,
        messageRegistry: MessageRegistry,
        preparedExpression: PreparedExpression?,
        parseException: ParseException
    ) {
        if (operation == JsclOperation.numeric &&
            preparedExpression != null &&
            preparedExpression.hasUndefinedVariables()) {
            evaluateAsync(sequence, JsclOperation.simplify, expression, messageRegistry)
            return
        }
        val event = CalculationFailedEvent(operation, expression, sequence, parseException)
        bus.post(event)
        _calculationEvents.tryEmit(CalculationEvent.Failed(event))
    }

    fun convert(state: DisplayState, to: NumeralBase) {
        val value = state.getResult()
        Check.isNotNull(value)
        val from = engine.getMathEngine().getNumeralBase()
        if (from == to) {
            return
        }

        executor.execute({
            try {
                val result = convert(value!!, to)
                bus.post(ConversionFinishedEvent(result, to, state))
            } catch (e: ConversionException) {
                bus.post(ConversionFailedEvent(state))
            }
        }, false)
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

    @Subscribe
    fun onEditorChanged(e: Editor.ChangedEvent) {
        if (!calculateOnFly) {
            return
        }
        if (!e.shouldEvaluate()) {
            return
        }
        evaluate(JsclOperation.numeric, e.newState.getTextString(), e.newState.sequence)
    }

    @Subscribe
    fun onDisplayChanged(e: Display.ChangedEvent) {
        val newState = e.newState
        if (!newState.valid) {
            return
        }
        val text = newState.text
        if (TextUtils.isEmpty(text)) {
            return
        }
        updateAnsVariable(text)
    }

    internal fun updateAnsVariable(value: String) {
        val variablesRegistry = engine.variablesRegistry
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

    @Subscribe
    fun onFunctionAdded(event: FunctionsRegistry.AddedEvent) {
        evaluate()
    }

    @Subscribe
    fun onFunctionsChanged(event: FunctionsRegistry.ChangedEvent) {
        evaluate()
    }

    @Subscribe
    fun onFunctionsRemoved(event: FunctionsRegistry.RemovedEvent) {
        evaluate()
    }

    @Subscribe
    fun onVariableRemoved(e: VariablesRegistry.RemovedEvent) {
        evaluate()
    }

    @Subscribe
    fun onVariableAdded(e: VariablesRegistry.AddedEvent) {
        evaluate()
    }

    @Subscribe
    fun onVariableChanged(e: VariablesRegistry.ChangedEvent) {
        if (e.newVariable.name != Constants.ANS) {
            evaluate()
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if (Preferences.Calculations.calculateOnFly.key == key) {
            setCalculateOnFly(Preferences.Calculations.calculateOnFly.getPreference(prefs) ?: true)
        }
    }

    companion object {
        const val NO_SEQUENCE = -1L

        private val SEQUENCER = AtomicLong(NO_SEQUENCE)

        private fun convert(generic: Generic, to: NumeralBase): String {
            val value = generic.toBigInteger() ?: throw ConversionException()
            return to.toString(value)
        }

        fun nextSequence(): Long = SEQUENCER.incrementAndGet()
    }

    sealed class CalculationEvent {
        data class Finished(val event: CalculationFinishedEvent) : CalculationEvent()
        data class Failed(val event: CalculationFailedEvent) : CalculationEvent()
        data class Cancelled(val event: CalculationCancelledEvent) : CalculationEvent()
    }
}
