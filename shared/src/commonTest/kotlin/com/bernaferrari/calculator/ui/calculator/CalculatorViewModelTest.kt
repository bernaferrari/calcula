package com.bernaferrari.calculator.ui.calculator

import com.bernaferrari.calculator.clearAndAwait
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.bernaferrari.calculator.data.*
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {
    private val models = ViewModelStore()
    private var count = 0
    private val store = MemoryStringStore()
    private val preferences = StoredPreferencesRepository(store)
    private val history = StoredHistoryRepository(store)
    @BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun tearDown() = runTest { models.clearAndAwait(); Dispatchers.resetMain() }
    private fun vm(
        saved: SavedStateHandle = SavedStateHandle(),
        dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
    ) = CalculatorViewModel(history, preferences, saved, dispatcher).also { models.put("vm${count++}", it) }
    private fun snapshot(saved: SavedStateHandle) = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })

    @Test fun exportIncludesEntriesBeyondDisplayLimit() = runTest {
        repeat(250) { history.insertCalculation(CalculationHistory(expression = "$it+1", result = "${it + 1}")) }
        assertEquals(250, vm().historyForExport().size)
    }

    @Test fun repeatedEqualsSurvivesRecreation() = runTest {
        val saved = SavedStateHandle()
        val original = vm(saved)
        listOf(Key.D2, Key.PLUS, Key.D3, Key.EQUALS).forEach(original::onKey)
        val restored = vm(snapshot(saved))
        restored.onKey(Key.EQUALS)
        assertEquals("8", restored.state.first { it.resultShown }.text)
    }

    @Test fun chainedOperandSurvivesRecreationAndHistoryEditing() = runTest {
        preferences.updateDecimalPlaces(2)
        val saved = SavedStateHandle()
        val original = vm(saved)
        listOf(Key.D1, Key.D0, Key.DIVIDE, Key.D3, Key.EQUALS, Key.TIMES, Key.D3).forEach(original::onKey)

        val restored = vm(snapshot(saved))
        val preview = restored.state.first { it.preview != null }
        assertEquals("3.33×3", preview.text)
        assertEquals("10", preview.preview)
        restored.onKey(Key.EQUALS)
        assertEquals("10", restored.state.first { it.resultShown }.text)

        val entry = history.getAllHistory().first().single { it.result == "10" }
        restored.onEditExpression(entry)
        restored.onKey(Key.EQUALS)
        assertEquals("10", restored.state.first { it.resultShown }.text)
    }

    @Test fun keysContinueFromPendingEqualsAndClearCancelsIt() = runTest {
        val saved = SavedStateHandle()
        val calculator = vm(saved, StandardTestDispatcher(testScheduler))
        listOf(Key.D1, Key.DIVIDE, Key.D3, Key.EQUALS).forEach(calculator::onKey)
        assertEquals("1÷3", saved.get<String>("text"))
        listOf(Key.TIMES, Key.D3, Key.EQUALS).forEach(calculator::onKey)
        runCurrent()
        assertEquals("1", saved.get<String>("text"))

        calculator.loadExpression("3000!")
        calculator.onKey(Key.EQUALS)
        calculator.onKey(Key.CLEAR)
        calculator.onKey(Key.D7)
        runCurrent()
        assertEquals("7", saved.get<String>("text"))
        assertEquals(2, history.getHistoryCount())
    }

    @Test fun tapePersistsTheExactResultAlongsideTheDisplayedDecimal() = runTest {
        val calculator = vm()
        calculator.loadExpression("1÷3")
        calculator.onKey(Key.EQUALS)

        val entry = StoredHistoryRepository(store).getAllHistory().first().single()
        assertEquals("0.333333333333333", entry.result)
        assertEquals("1⁄3", entry.exactResult)
        assertEquals("1/3", Calculator.exactResultForCopy(entry.copy(resultOperand = null)))
    }

    @Test fun moreDigitsRequestsProgressivelyLargerBatches() = runTest {
        val calculator = vm()
        calculator.loadExpression("1÷3")
        calculator.onKey(Key.EQUALS)

        for (digits in listOf(120, 500, 1_000)) {
            calculator.onMoreDigits()
            val result = calculator.state.first().extendedDisplay
            assertEquals(digits, result?.significantDigits)
            assertEquals("0." + "3".repeat(digits), result?.text)
        }
    }

    @Test fun reopeningUnchangedHistoryRetainsItsOriginalAngleMode() = runTest {
        val calculator = vm()
        calculator.loadExpression("sin(30)")
        calculator.onKey(Key.EQUALS)
        val entry = history.getAllHistory().first().single()
        assertNotNull(entry.resultOperand)

        preferences.updateAngleUnit(AngleUnit.RADIANS)
        calculator.onEditExpression(entry)
        calculator.onKey(Key.EQUALS)
        assertEquals("0.5", calculator.state.first { it.resultShown }.text)
        assertFalse(history.getAllHistory().first().first().approximate)
    }

    @Test fun cursorSurvivesRecreation() = runTest {
        val saved = SavedStateHandle()
        val original = vm(saved)
        listOf(Key.D1, Key.D2, Key.D3).forEach(original::onKey)
        original.onCursorMoved(1)
        val restored = vm(snapshot(saved))
        restored.onKey(Key.D9)
        assertEquals("1923", restored.state.first { it.text == "1923" }.text)
    }

    @Test fun rpnStackAndUnfinishedEntrySurviveRecreation() = runTest {
        preferences.update { it.copy(rpn = true) }
        val saved = SavedStateHandle()
        val original = vm(saved)
        listOf(Key.D5, Key.EQUALS, Key.D1, Key.D2, Key.DOT).forEach(original::onKey)
        val restored = vm(snapshot(saved))
        assertEquals(listOf("12.", "5"), restored.state.first { it.rpn }.stack)
        restored.onKey(Key.D5)
        restored.onKey(Key.PLUS)
        assertEquals(listOf("17.5"), restored.state.first { it.stack == listOf("17.5") }.stack)
    }

    @Test fun undoDoesNotUndoPreferences() = runTest {
        val calculator = vm()
        listOf(Key.D1, Key.D2).forEach(calculator::onKey)
        preferences.updateAngleUnit(AngleUnit.RADIANS)
        preferences.updateDecimalPlaces(2)
        calculator.onKey(Key.UNDO)
        assertEquals(AngleUnit.RADIANS, calculator.state.first { it.text == "1" }.angleUnit)
        calculator.onKey(Key.DIVIDE); calculator.onKey(Key.D3); calculator.onKey(Key.EQUALS)
        assertEquals("0.33", calculator.state.first { it.resultShown }.text)
    }

    @Test fun deleteCalculateAndUndoThroughViewModelKeepsBothResults() = runTest {
        val calculator = vm()
        listOf(Key.D1, Key.PLUS, Key.D1, Key.EQUALS).forEach(calculator::onKey)
        val deleted = history.getAllHistory().first().single()
        calculator.onDeleteEntry(deleted)
        listOf(Key.D2, Key.PLUS, Key.D2, Key.EQUALS).forEach(calculator::onKey)
        calculator.onRestore(listOf(deleted))
        assertEquals(setOf("2", "4"), history.getAllHistory().first().map { it.result }.toSet())
    }
}
