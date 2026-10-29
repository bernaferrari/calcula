package com.bernaferrari.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.bernaferrari.calculator.data.*
import com.bernaferrari.calculator.domain.input.*
import com.bernaferrari.calculator.domain.math.*
import com.bernaferrari.calculator.domain.model.*
import com.bernaferrari.calculator.domain.programmer.*
import com.bernaferrari.calculator.ui.calculator.CalculatorViewModel
import com.bernaferrari.calculator.ui.converter.ConverterViewModel
import com.bernaferrari.calculator.ui.graph.GraphViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewAuditTest {
    private val models = ViewModelStore()
    @BeforeTest fun setup() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun cleanup() { models.clear(); UserFunctions.all = emptyList(); Dispatchers.resetMain() }

    @Test fun customRpnFunctionPreservesY() = runTest {
        val store = MemoryStringStore()
        val prefs = StoredPreferencesRepository(store)
        prefs.update { it.copy(rpn = true, functions = listOf(UserFunction("double", "x*2"))) }
        val saved = SavedStateHandle(mapOf("rpn_stack" to arrayOf("3", "7", "9")))
        val vm = CalculatorViewModel(StoredHistoryRepository(store), prefs, saved)
        models.put("calc", vm)
        vm.onInsertText("double(")
        assertEquals(listOf("6", "7", "9"), saved.get<Array<String>>("rpn_stack")!!.toList())
    }

    @Test fun customRpnFunctionUsesRadians() = runTest {
        val store = MemoryStringStore()
        val prefs = StoredPreferencesRepository(store)
        prefs.update { it.copy(rpn = true, angleUnit = AngleUnit.RADIANS, functions = listOf(UserFunction("wave", "sin(x)"))) }
        val saved = SavedStateHandle(mapOf("rpn_stack" to arrayOf("1")))
        val vm = CalculatorViewModel(StoredHistoryRepository(store), prefs, saved)
        models.put("calc", vm)
        vm.onInsertText("wave(")
        val actual = saved.get<Array<String>>("rpn_stack")!!.first().toDouble()
        assertEquals(kotlin.math.sin(1.0), actual, 1e-12)
    }

    @Test fun removingEarlierGraphKeepsLastFunctionSelected() {
        val store = object : GraphStore {
            override fun load() = GraphDocument(functions = listOf("x", "x²", "x³"), active = 2)
            override fun save(document: GraphDocument) {}
        }
        val vm = GraphViewModel(store)
        models.put("graph", vm)
        vm.onRemove(0)
        assertEquals("x³", vm.state.value.activeFunction.text)
    }

    @Test fun converterActivatingOutputPreservesSmallValue() {
        val vm = ConverterViewModel(SavedStateHandle())
        vm.seed("0.000000001")
        val before = vm.state.value.output
        assertTrue(before.contains('E'))
        vm.onActivate(false)
        assertEquals(before, vm.state.value.input)
    }

    @Test fun unsignedDivisionUsesUnsignedOperands() {
        val s = ProgState(tokens = listOf("255", "÷", "2"), word = WordSize.BYTE, signed = false)
        assertEquals(127L, Programmer.value(s))
    }

    @Test fun sameBasePreservesNegativeHighBitLiteral() {
        val s = ProgState(tokens = listOf("−u", "255"), word = WordSize.BYTE)
        assertEquals(Programmer.value(s), Programmer.value(Programmer.withBase(s, Base.DEC)))
    }

    @Test fun rpnFormattingHonorsScientificPreference() = runTest {
        val store = MemoryStringStore()
        val prefs = StoredPreferencesRepository(store)
        prefs.update { it.copy(rpn = true, notation = Notation.SCIENTIFIC) }
        val vm = CalculatorViewModel(StoredHistoryRepository(store), prefs, SavedStateHandle())
        models.put("calc", vm)
        listOf(Key.D2, Key.EQUALS, Key.D3, Key.PLUS).forEach(vm::onKey)
        val state = vm.state.first { it.rpn }
        assertTrue(state.stack.first().contains('E'), "Scientific stack display was ${state.stack}")
    }

    @Test fun savedFunctionDoesNotTurnEmptyArgumentIntoZero() {
        UserFunctions.all = listOf(UserFunction("double", "x*2"))
        assertNull(CalcEngine.evaluateOrNull("double()"))
    }

    @Test fun usageFlushesWhenViewModelIsCleared() = runTest {
        val store = MemoryStringStore()
        val prefs = StoredPreferencesRepository(store)
        val vm = CalculatorViewModel(StoredHistoryRepository(store), prefs)
        models.put("calc", vm)
        vm.onKey(Key.D7)
        models.clear()
        assertEquals(1, prefs.getUserPreferences().first().usage.keys["D7"])
    }

    @Test fun shortcutScientificNotationPreservesExponent() {
        assertEquals(Decimal(1000), CalcEngine.evaluate(SpokenMath.normalize("1E3")))
    }
}
