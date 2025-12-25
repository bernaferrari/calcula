package org.solovyev.android.calculator

import android.content.SharedPreferences
import com.squareup.otto.Bus
import org.junit.Before
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.solovyev.android.calculator.calculations.CalculationFailedEvent
import org.solovyev.android.calculator.calculations.CalculationFinishedEvent
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.jscl.JsclOperation.numeric
import org.solovyev.common.msg.Message

abstract class BaseCalculatorTest {
    protected lateinit var calculator: Calculator
    protected lateinit var bus: Bus
    protected lateinit var engine: Engine

    @Before
    fun setUp() {
        bus = mock(Bus::class.java)
        calculator = Calculator(mock(SharedPreferences::class.java), bus)
        calculator.setSynchronous()
        engine = Tests.makeEngine()
        engine.variablesRegistry.bus = bus
        calculator.engine = engine
        val processor = ToJsclTextProcessor()
        processor.engine = engine
        calculator.preprocessor = processor
    }

    protected fun assertError(expression: String) {
        calculator.evaluate(numeric, expression, 0)
        verify(calculator.bus, atLeastOnce()).post(ArgumentMatchers.argThat(failed()))
    }

    protected fun assertEval(expected: String, expression: String) {
        assertEval(expected, expression, numeric)
    }

    protected fun assertEval(expected: String, expression: String, operation: JsclOperation) {
        calculator.evaluate(operation, expression, 0)
        verify(calculator.bus, atLeastOnce()).post(finishedEvent(expected, expression, operation))
    }

    companion object {
        private fun failed(): ArgumentMatcher<CalculationFailedEvent> {
            return ArgumentMatcher { o ->
                o.operation == numeric
            }
        }

        fun finishedEvent(expected: String, expression: String, operation: JsclOperation): CalculationFinishedEvent {
            return ArgumentMatchers.argThat(finished(expected, expression, operation))
        }

        fun anyFinishedEvent(): CalculationFinishedEvent {
            return ArgumentMatchers.argThat { true }
        }

        fun finished(expected: String, expression: String, operation: JsclOperation): ArgumentMatcher<CalculationFinishedEvent> {
            return object : ArgumentMatcher<CalculationFinishedEvent> {
                override fun matches(e: CalculationFinishedEvent): Boolean {
                    return e.expression == expression && e.stringResult == expected
                }

                override fun toString(): String {
                    return CalculationFinishedEvent(
                        operation, expression, 0, null, expected,
                        emptyList<Message>()
                    ).toString()
                }
            }
        }
    }
}
