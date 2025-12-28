package org.solovyev.android.calculator

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.jscl.JsclOperation.numeric
import org.solovyev.android.calculator.testutils.MainDispatcherRule

abstract class BaseCalculatorTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    protected lateinit var calculator: Calculator
    protected lateinit var engine: Engine

    @Before
    open fun setUp() {
        val env = Tests.createCalculatorEnvironment()
        calculator = env.calculator
        engine = env.engine
    }

    protected fun assertError(expression: String) = runBlocking {
        val sequence = Calculator.nextSequence()
        calculator.evaluate(numeric, expression, sequence)
        calculator.calculationFailed.first { it.sequence == sequence }
    }

    protected fun assertEval(expected: String, expression: String) {
        assertEval(expected, expression, numeric)
    }

    protected fun assertEval(expected: String, expression: String, operation: JsclOperation) = runBlocking {
        val sequence = Calculator.nextSequence()
        calculator.evaluate(operation, expression, sequence)
        val event = calculator.calculationFinished.first { it.sequence == sequence }
        org.junit.Assert.assertEquals(expected, event.stringResult)
    }
}
