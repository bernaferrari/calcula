package org.solovyev.android.calculator

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RpnMachineTest {

    @Test
    fun appendDigitAppliesDecimalAndLeadingZeroRules() {
        val machine = machineWithResponses()

        machine.appendDigit(".")
        machine.appendDigit(".")
        machine.appendDigit("5")
        assertEquals("0.5", machine.snapshot().entry)

        machine.clear()
        machine.appendDigit("0")
        machine.appendDigit("7")
        assertEquals("7", machine.snapshot().entry)
    }

    @Test
    fun snapshotExposesVisibleStackAndXFromEntry() = runBlocking {
        val machine = machineWithResponses()
        machine.setEntry("12")
        var snapshot = machine.snapshot()
        assertEquals(listOf("12"), snapshot.visibleStack)
        assertEquals("12", snapshot.x)

        machine.enter()
        snapshot = machine.snapshot()
        assertEquals(listOf("12"), snapshot.visibleStack)
        assertEquals("12", snapshot.x)
    }

    @Test
    fun enterPushesEntryAndDuplicatesTopWhenEntryIsEmpty() = runBlocking {
        val machine = machineWithResponses()
        machine.setEntry("7")
        machine.enter()
        assertEquals(listOf("7"), machine.snapshot().stack)
        assertEquals("", machine.snapshot().entry)

        machine.enter()
        assertEquals(listOf("7", "7"), machine.snapshot().stack)
    }

    @Test
    fun backspaceRemovesEntryThenStack() = runBlocking {
        val machine = machineWithResponses()
        machine.setEntry("123")
        assertTrue(machine.backspace())
        assertEquals("12", machine.snapshot().entry)

        machine.setEntry("9")
        machine.enter()
        machine.setEntry("")
        assertTrue(machine.backspace())
        assertTrue(machine.snapshot().stack.isEmpty())
        assertTrue(!machine.backspace())
    }

    @Test
    fun toggleSignUpdatesEntryOrTopOfStack() = runBlocking {
        val machine = machineWithResponses()
        machine.setEntry("4")
        machine.toggleSign()
        assertEquals("-4", machine.snapshot().entry)
        machine.toggleSign()
        assertEquals("4", machine.snapshot().entry)

        machine.enter()
        machine.setEntry("")
        machine.toggleSign()
        assertEquals(listOf("-4"), machine.snapshot().stack)
    }

    @Test
    fun applyBinaryOperatorUsesCommittedEntryAndNormalizedOperator() = runBlocking {
        val calls = mutableListOf<String>()
        val machine = machineWithResponses(
            responses = mapOf("(2)*(3)" to "6"),
            calls = calls
        )
        machine.setEntry("2")
        machine.enter()
        machine.setEntry("3")
        machine.enter()

        val result = machine.applyBinaryOperator("×")
        assertIs<RpnActionResult.Success>(result)
        assertEquals(listOf("6"), machine.snapshot().stack)
        assertEquals(listOf("(2)*(3)"), calls)
    }

    @Test
    fun applyBinaryOperatorNormalizesDivisionAlias() = runBlocking {
        val calls = mutableListOf<String>()
        val machine = machineWithResponses(
            responses = mapOf("(8)/(2)" to "4"),
            calls = calls
        )
        machine.setEntry("8")
        machine.enter()
        machine.setEntry("2")
        machine.enter()

        val result = machine.applyBinaryOperator("÷")
        assertIs<RpnActionResult.Success>(result)
        assertEquals(listOf("4"), machine.snapshot().stack)
        assertEquals(listOf("(8)/(2)"), calls)
    }

    @Test
    fun applyBinaryOperatorCommitsEntryBeforeArityValidation() = runBlocking {
        val machine = machineWithResponses()
        machine.setEntry("5")

        val result = machine.applyBinaryOperator("+")
        val error = assertIs<RpnActionResult.Error>(result)
        assertEquals("RPN requires at least two stack values", error.message)
        assertEquals(listOf("5"), machine.snapshot().stack)
        assertEquals("", machine.snapshot().entry)
    }

    @Test
    fun applyBinaryOperatorRestoresOperandsOnEvaluationFailure() = runBlocking {
        val machine = machineWithResponses()
        machine.setEntry("2")
        machine.enter()
        machine.setEntry("3")
        machine.enter()

        val result = machine.applyBinaryOperator("+")
        val error = assertIs<RpnActionResult.Error>(result)
        assertEquals("Invalid RPN operation", error.message)
        assertEquals(listOf("2", "3"), machine.snapshot().stack)
    }

    @Test
    fun applyUnaryFunctionBuildsExpressionAndRestoresOnFailure() = runBlocking {
        val successMachine = machineWithResponses(responses = mapOf("sqrt(9)" to "3"))
        successMachine.setEntry("9")
        val success = successMachine.applyUnaryFunction("sqrt")
        assertIs<RpnActionResult.Success>(success)
        assertEquals(listOf("3"), successMachine.snapshot().stack)

        val genericMachine = machineWithResponses(responses = mapOf("sin(0)" to "0"))
        genericMachine.setEntry("0")
        val genericSuccess = genericMachine.applyUnaryFunction("sin")
        assertIs<RpnActionResult.Success>(genericSuccess)
        assertEquals(listOf("0"), genericMachine.snapshot().stack)

        val failureMachine = machineWithResponses()
        failureMachine.setEntry("9")
        val failure = failureMachine.applyUnaryFunction("sin")
        val error = assertIs<RpnActionResult.Error>(failure)
        assertEquals("Invalid RPN function", error.message)
        assertEquals(listOf("9"), failureMachine.snapshot().stack)
    }

    @Test
    fun applyUnaryExpressionBuildsTemplateAndRestoresOnFailure() = runBlocking {
        val successMachine = machineWithResponses(responses = mapOf("(4)^2" to "16"))
        successMachine.setEntry("4")
        val success = successMachine.applyUnaryExpression("(%s)^2")
        assertIs<RpnActionResult.Success>(success)
        assertEquals(listOf("16"), successMachine.snapshot().stack)

        val failureMachine = machineWithResponses()
        failureMachine.setEntry("4")
        val failure = failureMachine.applyUnaryExpression("(%s)!")
        val error = assertIs<RpnActionResult.Error>(failure)
        assertEquals("Invalid RPN operation", error.message)
        assertEquals(listOf("4"), failureMachine.snapshot().stack)
    }

    @Test
    fun applyUnaryActionsRequireAtLeastOneValue() = runBlocking {
        val machine = machineWithResponses()
        val functionResult = machine.applyUnaryFunction("sin")
        val functionError = assertIs<RpnActionResult.Error>(functionResult)
        assertEquals("RPN requires at least one stack value", functionError.message)

        val expressionResult = machine.applyUnaryExpression("(%s)^3")
        val expressionError = assertIs<RpnActionResult.Error>(expressionResult)
        assertEquals("RPN requires at least one stack value", expressionError.message)
    }

    @Test
    fun stackLimitTrimsOldestValues() = runBlocking {
        val machine = machineWithResponses(stackLimit = 3)
        machine.setEntry("1")
        machine.enter()
        machine.setEntry("2")
        machine.enter()
        machine.setEntry("3")
        machine.enter()
        machine.setEntry("4")
        machine.enter()
        assertEquals(listOf("2", "3", "4"), machine.snapshot().stack)

        machine.setEntry("")
        machine.enter()
        assertEquals(listOf("3", "4", "4"), machine.snapshot().stack)
    }

    private fun machineWithResponses(
        responses: Map<String, String?> = emptyMap(),
        calls: MutableList<String>? = null,
        stackLimit: Int = 64
    ): RpnMachine {
        return RpnMachine(
            evaluator = { expression ->
                calls?.add(expression)
                responses[expression]
            },
            stackLimit = stackLimit
        )
    }
}
