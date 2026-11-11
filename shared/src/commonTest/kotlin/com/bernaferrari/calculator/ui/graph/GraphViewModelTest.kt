package com.bernaferrari.calculator.ui.graph

import com.bernaferrari.calculator.data.GraphDocument
import com.bernaferrari.calculator.data.GraphStore
import com.bernaferrari.calculator.data.SavedGraph
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.library.CONSTANTS
import com.bernaferrari.calculator.domain.math.Curve
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class MemoryStore(var document: GraphDocument? = null) : GraphStore {
    var recent: List<SavedGraph> = emptyList()
    override fun load() = document
    override fun save(document: GraphDocument) { this.document = document }
    override fun loadRecent() = recent
    override fun saveRecent(graphs: List<SavedGraph>) { recent = graphs }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GraphViewModelTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun aNewGraphKeepsTheOldOneInRecent() {
        val store = MemoryStore()
        val vm = GraphViewModel(store)
        vm.onKey(Key.CLEAR)
        vm.onExample("x²")
        vm.onNewGraph()
        assertEquals(listOf(""), vm.state.value.functions.map { it.text })
        assertEquals(listOf(listOf("x²")), vm.recent.value.map { it.document.functions })
        assertEquals(store.recent, vm.recent.value)
        // An empty graph isn't worth keeping.
        vm.onNewGraph()
        assertEquals(1, vm.recent.value.size)
    }

    @Test fun restoringSwapsAndTheSameGraphIsKeptOnce() {
        val vm = GraphViewModel(MemoryStore())
        vm.onKey(Key.CLEAR)
        vm.onExample("x²")
        vm.onParameter('a', 2.0)
        vm.onNewGraph()
        vm.onExample("sin(x)")
        val squared = vm.recent.value.single()
        vm.onRestore(squared.id)
        assertEquals(listOf("x²"), vm.state.value.functions.map { it.text })
        // sin(x) went to recent; x² is current, so it isn't listed twice.
        assertEquals(listOf(listOf("sin(x)")), vm.visibleRecent().map { it.document.functions })
        vm.onNewGraph()
        assertEquals(2, vm.recent.value.size)
        vm.onExample("x²")
        vm.onNewGraph()
        assertEquals(2, vm.recent.value.size)
    }

    @Test fun pinnedGraphsStayAndOthersRollOff() {
        val vm = GraphViewModel(MemoryStore())
        vm.onKey(Key.CLEAR)
        vm.onExample("x+0")
        vm.onNewGraph()
        vm.onTogglePin(vm.recent.value.single().id)
        repeat(RECENT_LIMIT + 5) { i ->
            vm.onExample("x+${i + 1}")
            vm.onNewGraph()
        }
        assertEquals(RECENT_LIMIT + 1, vm.recent.value.size)
        assertTrue(vm.recent.value.any { it.pinned && it.document.functions == listOf("x+0") })
        vm.onDelete(vm.recent.value.first { it.pinned }.id)
        assertTrue(vm.recent.value.none { it.pinned })
    }

    @Test fun parametersGetSlidersAndTheNextLetter() {
        val vm = GraphViewModel(MemoryStore())
        vm.onKey(Key.CLEAR)
        vm.onExample("ax²+bx+c")
        assertEquals(listOf('a', 'b', 'c'), vm.state.value.used)
        assertEquals('d', vm.state.value.nextParameter)
        vm.onParameter('a', 2.04)
        assertEquals(2.0, vm.state.value.parameter('a').value) // snapped to the 0.2 step of −10…10
        val plot = (vm.state.value.curves[0] as Curve.Explicit).plot
        assertEquals(2.0 * 9 + 3 + 1, plot.at(3.0)) // b and c start at 1
    }

    @Test fun examplesFillAnEmptyRowOrAddACurveWithoutReplacingExistingOnes() {
        val vm = GraphViewModel(MemoryStore())
        vm.onAdd()
        vm.onActivate(0)
        vm.onExample("x²+y²=9")
        assertEquals(listOf("sin(x)", "x²+y²=9"), vm.state.value.functions.map { it.text })
        assertEquals(1, vm.state.value.active)
        assertTrue(vm.state.value.curves[1] is Curve.Implicit)

        vm.onExample("a×sin(b×x)")
        assertEquals(listOf("sin(x)", "x²+y²=9", "a×sin(b×x)"), vm.state.value.functions.map { it.text })
        assertEquals(listOf('a', 'b'), vm.state.value.used)
        repeat(MAX_FUNCTIONS - 3) { vm.onExample("x²") }
        val full = vm.state.value
        vm.onExample("x³−x")
        assertEquals(full, vm.state.value)
    }

    @Test fun flickingDownTypesTheNextParameter() {
        val vm = GraphViewModel(MemoryStore())
        vm.onKey(Key.CLEAR)
        vm.onKey(Key.PARAM_A); vm.onKey(Key.VARIABLE)
        assertEquals("ax", vm.state.value.activeFunction.text)
        assertEquals('b', vm.state.value.nextParameter)
    }

    @Test fun libraryConstantsUseShortSymbolsAndMultiplyAdjacentFactors() {
        val vm = GraphViewModel(MemoryStore())
        vm.onInsertConstant(CONSTANTS.first { it.id == "pi" })
        assertEquals("sin(x)×π", vm.state.value.activeFunction.text)
        val plot = (vm.state.value.curves.single() as Curve.Explicit).plot
        assertEquals(kotlin.math.PI, plot.at(kotlin.math.PI / 2))

        vm.onCursorMoved(0)
        vm.onInsertConstant(CONSTANTS.first { it.id == "sqrt2" })
        assertEquals("√2×sin(x)×π", vm.state.value.activeFunction.text)
        assertEquals(2, vm.state.value.activeFunction.editor.cursor)
    }

    @Test fun insertingAConstantKeepsTheScientificLiteralAtTheCursorIntact() {
        val vm = GraphViewModel(MemoryStore())
        vm.onKey(Key.CLEAR)
        vm.onExample("x×2E−3")
        vm.onCursorMoved(5)
        vm.onInsertConstant(CONSTANTS.first { it.id == "gconst" })
        assertEquals("x×2E−3×6.67430E−11", vm.state.value.activeFunction.text)
        assertTrue(vm.state.value.curves.single() is Curve.Explicit)
    }

    @Test fun everythingSurvivesARelaunch() {
        val store = MemoryStore()
        val first = GraphViewModel(store)
        first.onKey(Key.CLEAR)
        first.onExample("a×sin(x)")
        first.onParameter('a', 3.0)
        first.onViewport(Viewport(1.0, 2.0, 8.0))
        first.onAdd()
        first.onExample("x²+y²=9")
        first.onCleared()

        val second = GraphViewModel(store)
        val s = second.state.value
        assertEquals(listOf("a×sin(x)", "x²+y²=9"), s.functions.map { it.text })
        assertEquals(1, s.active)
        assertEquals(Viewport(1.0, 2.0, 8.0), s.viewport)
        assertEquals(3.0, s.parameter('a').value)
        assertTrue(s.curves[1] is Curve.Implicit)
    }
}
