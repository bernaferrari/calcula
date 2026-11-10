package com.bernaferrari.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.bernaferrari.calculator.data.MemoryStringStore
import com.bernaferrari.calculator.data.StoredHistoryRepository
import com.bernaferrari.calculator.data.StoredPreferencesRepository
import com.bernaferrari.calculator.domain.input.*
import com.bernaferrari.calculator.domain.convert.Category
import com.bernaferrari.calculator.domain.model.Notation
import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.ui.calculator.CalculatorViewModel
import com.bernaferrari.calculator.ui.converter.ConverterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class EditingRegressionTest {
    private val models = ViewModelStore()
    @BeforeTest fun setup() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun cleanup() = runTest { models.clearAndAwait(); Dispatchers.resetMain() }

    @Test fun cursorStepLetsYouEditTheResult() {
        val saved = SavedStateHandle()
        val store = MemoryStringStore()
        val vm = CalculatorViewModel(StoredHistoryRepository(store), StoredPreferencesRepository(store), saved, UnconfinedTestDispatcher())
        models.put("calc", vm)
        listOf(Key.D3, Key.PLUS, Key.D9, Key.EQUALS).forEach(vm::onKey)
        assertTrue(vm.onCursorStep(-1))
        vm.onKey(Key.D5)
        assertEquals("152", saved.get<String>("text"))
    }
    @Test fun cursorJumpLetsYouEditTheResult() {
        val saved = SavedStateHandle()
        val store = MemoryStringStore()
        val vm = CalculatorViewModel(StoredHistoryRepository(store), StoredPreferencesRepository(store), saved, UnconfinedTestDispatcher())
        models.put("calc", vm)
        listOf(Key.D3, Key.PLUS, Key.D9, Key.EQUALS).forEach(vm::onKey)
        assertTrue(vm.onCursorJump(-1))
        vm.onKey(Key.D5)
        assertEquals("512", saved.get<String>("text"))
    }
    @Test fun engineeringNotationAtSmallMagnitudesRemainsReusable() {
        val (state, outcome) = Calculator.press(CalcState(editor = Editor("1E−10000×1"), notation = Notation.ENGINEERING, decimalPlaces = 0), Key.EQUALS)
        assertTrue(outcome is Outcome.Evaluated)
        assertEquals("100E−10002", state.editor.text)
        assertEquals(Decimal("1E-10000"), CalcEngine.evaluate(state.editor.text))
        assertFailsWith<IllegalArgumentException> { Decimal("1E-2147483648") }
    }
    @Test fun exponentDigitsCannotTakeADecimalPoint() {
        for (text in listOf("1E3", "1E−3", "1E-3", "1E+3")) {
            val editor = Editor(text)
            assertEquals(editor, ExpressionEditor.insertDecimal(editor), text)
        }
        assertEquals("1.2E−3", ExpressionEditor.insertDecimal(Editor("12E−3", 1)).text)
    }
    @Test fun malformedDecimalDoesNotBypassEvaluation() {
        for (text in listOf(".", "1..2", "1.2.3", "−..")) {
            assertFalse(Calculator.isPlainNumber(text), text)
            assertTrue(Calculator.press(CalcState(editor = Editor(text)), Key.EQUALS).second is Outcome.Failed, text)
        }
        for (text in listOf("", "1.", ".5", "−1.5", "2E−3")) assertTrue(Calculator.isPlainNumber(text), text)
    }
    @Test fun roundedResultDoesNotClaimADifferentFraction() {
        val (state, _) = Calculator.press(CalcState(editor = Editor("1÷3"), decimalPlaces = 0), Key.EQUALS)
        assertEquals("0", state.editor.text)
        assertNull(state.fraction)
        val (exact, _) = Calculator.press(CalcState(editor = Editor("3÷8"), decimalPlaces = 3), Key.EQUALS)
        assertEquals("3/8", exact.fraction)
        assertNotNull(Calculator.press(CalcState(editor = Editor("1÷3")), Key.EQUALS).first.fraction)
    }
    @Test fun changingCategoryKeepsTheActiveInputRow() {
        val vm = ConverterViewModel(SavedStateHandle())
        vm.onActivate(false)
        val value = vm.state.value.input
        vm.onCategory(Category.MASS)
        assertFalse(vm.state.value.topActive)
        assertEquals(value, vm.state.value.bottomText)
    }
    @Test fun restoredUnitsMustBelongToTheRestoredCategory() {
        val vm = ConverterViewModel(SavedStateHandle(mapOf("category" to "LENGTH", "top" to "kg", "bottom" to "f", "input" to "10")))
        assertEquals(Category.LENGTH, vm.state.value.top.category)
        assertEquals(Category.LENGTH, vm.state.value.bottom.category)
        assertTrue(vm.state.value.output.isNotEmpty())
    }
}
