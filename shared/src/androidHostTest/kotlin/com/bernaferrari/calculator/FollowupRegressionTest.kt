package com.bernaferrari.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.bernaferrari.calculator.data.*
import com.bernaferrari.calculator.domain.input.*
import com.bernaferrari.calculator.domain.math.*
import com.bernaferrari.calculator.domain.model.*
import com.bernaferrari.calculator.ui.calculator.CalculatorViewModel
import com.bernaferrari.calculator.ui.stats.StatisticsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FollowupRegressionTest {
    private val models = ViewModelStore()
    @BeforeTest fun setup() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun cleanup() = runTest { models.clearAndAwait(); UserFunctions.all = emptyList(); Dispatchers.resetMain() }

    @Test fun implicitMultiplicationSubstitutesEveryVariable() {
        UserFunctions.all = listOf(UserFunction("sq", "xx"), UserFunction("wave", "xsin(x)"), UserFunction("tax", "x*2"), UserFunction("total", "tax(x)+x"))
        assertEquals(Decimal(9), CalcEngine.evaluate("sq(3)"))
        assertEquals("15", NumberFormatter.format(CalcEngine.evaluate("wave(30)")))
        assertEquals(Decimal(9), CalcEngine.evaluate("total(3)"))
    }
    @Test fun eightFunctionLevelsAreAllowed() {
        val names = listOf("aa", "bb", "cc", "dd", "ff", "gg", "hh", "ii")
        UserFunctions.all = names.mapIndexed { i, name -> UserFunction(name, if (i == 7) "x" else "${names[i + 1]}(x)") }
        assertEquals(Decimal(2), CalcEngine.evaluate("aa(2)"))
    }
    @Test fun clearingRpnPreservesNotation() {
        assertEquals(Notation.SCIENTIFIC, Rpn.press(RpnState(stack = listOf(Decimal(2)), notation = Notation.SCIENTIFIC), Key.CLEAR).first.notation)
    }
    @Test fun rpnCanOperateOnLargePreciseNumbers() {
        val value = Decimal("1.234567890123456E5000")
        val (state, outcome) = Rpn.press(RpnState(stack = listOf(value)), Key.ABS)
        assertTrue(outcome is RpnOutcome.Evaluated, "$outcome")
        assertNull(state.error)
    }
    @Test fun libraryFunctionsRoundRpnLikeKeypadOperations() = runTest {
        val store = MemoryStringStore()
        val prefs = StoredPreferencesRepository(store)
        prefs.update { it.copy(rpn = true, decimalPlaces = 2) }
        val saved = SavedStateHandle(mapOf("rpn_stack" to arrayOf("2")))
        val vm = CalculatorViewModel(StoredHistoryRepository(store), prefs, saved, UnconfinedTestDispatcher())
        models.put("calculator", vm)
        vm.onInsertText("√(")
        assertEquals("1.41", saved.get<Array<String>>("rpn_stack")!!.first())
    }
    @Test fun libraryRpnOperationsRecordHistoryAndReportDomainErrors() = runTest {
        val store = MemoryStringStore()
        val prefs = StoredPreferencesRepository(store)
        prefs.update { it.copy(rpn = true) }
        val history = StoredHistoryRepository(store)
        val saved = SavedStateHandle(mapOf("rpn_stack" to arrayOf("9")))
        val vm = CalculatorViewModel(history, prefs, saved, UnconfinedTestDispatcher())
        models.put("calculator", vm)
        vm.onInsertText("√(")
        assertEquals("3", history.getAllHistory().first().single().result)
        vm.onKey(Key.NEGATE)
        vm.onInsertText("√(")
        assertEquals(CalcError.DOMAIN, vm.state.first { it.rpn }.error)
        assertEquals(1, history.getAllHistory().first().size)
        assertEquals("-3", saved.get<Array<String>>("rpn_stack")!!.first())
    }
    @Test fun libraryFunctionsAcceptLargeRpnArguments() = runTest {
        val store = MemoryStringStore()
        val prefs = StoredPreferencesRepository(store)
        prefs.update { it.copy(rpn = true) }
        val saved = SavedStateHandle(mapOf("rpn_stack" to arrayOf("1.234567890123456E5000")))
        val vm = CalculatorViewModel(StoredHistoryRepository(store), prefs, saved, UnconfinedTestDispatcher())
        models.put("calculator", vm)
        vm.onInsertText("abs(")
        assertEquals(Decimal("1.23456789012346E5000"), Decimal(saved.get<Array<String>>("rpn_stack")!!.first()))
    }
    @Test fun statisticsReopensScientificValuesForDecimalEditing() {
        val prefs = StoredPreferencesRepository(MemoryStringStore())
        runTest { prefs.update { it.copy(statsData = listOf("1E−10")) } }
        val vm = StatisticsViewModel(prefs)
        models.put("statistics", vm)
        vm.onDelete()
        assertEquals("0.0000000001", vm.state.value.entry)
        vm.onDelete(); vm.onDigit('2')
        assertTrue(vm.onAdd())
        assertEquals("2E−10", vm.state.value.values.single())
    }
    @Test fun editingHistoryClearsOldSelection() = runTest {
        val store = MemoryStringStore()
        val saved = SavedStateHandle()
        val vm = CalculatorViewModel(StoredHistoryRepository(store), StoredPreferencesRepository(store), saved)
        models.put("calculator", vm)
        vm.loadExpression("12345")
        vm.onSelect(Span(0, 5))
        vm.onEditExpression(CalculationHistory(expression = "2+3", result = "5"))
        vm.onKey(Key.D4)
        assertEquals("2+34", saved.get<String>("text"))
    }
}
