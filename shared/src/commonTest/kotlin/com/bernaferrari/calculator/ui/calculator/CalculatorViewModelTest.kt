package com.bernaferrari.calculator.ui.calculator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.bernaferrari.calculator.data.*
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.model.*
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
    @AfterTest fun tearDown() { models.clear(); Dispatchers.resetMain() }
    private fun vm(saved: SavedStateHandle = SavedStateHandle()) = CalculatorViewModel(history, preferences, saved).also { models.put("vm${count++}", it) }
    private fun snapshot(saved: SavedStateHandle) = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })

    @Test fun repeatedEqualsSurvivesRecreation() = runTest {
        val saved = SavedStateHandle()
        val original = vm(saved)
        listOf(Key.D2, Key.PLUS, Key.D3, Key.EQUALS).forEach(original::onKey)
        val restored = vm(snapshot(saved))
        restored.onKey(Key.EQUALS)
        assertEquals("8", restored.state.first { it.resultShown }.text)
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
