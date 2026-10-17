package com.bernaferrari.calculator.ui.graph

import com.bernaferrari.calculator.data.GraphDocument
import com.bernaferrari.calculator.data.GraphStore
import com.bernaferrari.calculator.domain.input.Key
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
    override fun load() = document
    override fun save(document: GraphDocument) { this.document = document }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GraphViewModelTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun parametersGetSlidersAndTheNextLetter() {
        val vm = GraphViewModel(MemoryStore())
        vm.onExample("ax²+bx+c")
        assertEquals(listOf('a', 'b', 'c'), vm.state.value.used)
        assertEquals('d', vm.state.value.nextParameter)
        vm.onParameter('a', 2.04)
        assertEquals(2.0, vm.state.value.parameter('a').value) // snapped to the 0.2 step of −10…10
        val plot = (vm.state.value.curves[0] as Curve.Explicit).plot
        assertEquals(2.0 * 9 + 3 + 1, plot.at(3.0)) // b and c start at 1
    }

    @Test fun flickingDownTypesTheNextParameter() {
        val vm = GraphViewModel(MemoryStore())
        vm.onKey(Key.CLEAR)
        vm.onKey(Key.PARAM_A); vm.onKey(Key.VARIABLE)
        assertEquals("ax", vm.state.value.activeFunction.text)
        assertEquals('b', vm.state.value.nextParameter)
    }

    @Test fun everythingSurvivesARelaunch() {
        val store = MemoryStore()
        val first = GraphViewModel(store)
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
