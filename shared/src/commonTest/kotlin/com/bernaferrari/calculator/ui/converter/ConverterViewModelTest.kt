package com.bernaferrari.calculator.ui.converter

import androidx.lifecycle.SavedStateHandle
import com.bernaferrari.calculator.domain.convert.Category
import kotlin.test.assertEquals
import kotlin.test.Test

class ConverterViewModelTest {
    private fun vm() = ConverterViewModel(SavedStateHandle())

    @Test fun typesIntoTopAndConvertsToBottom() {
        val vm = vm()
        vm.onClear()
        vm.onDigit('1'); vm.onDigit('0')
        assertEquals("10", vm.state.value.topText)
        assertEquals("6.21371192237", vm.state.value.bottomText) // km → mi
    }

    @Test fun activatingOtherRowCarriesItsValue() {
        val vm = vm()
        vm.onCategory(Category.TEMPERATURE)
        vm.onClear(); vm.onDigit('1'); vm.onDigit('0'); vm.onDigit('0')
        assertEquals("212", vm.state.value.bottomText)
        vm.onActivate(top = false)
        assertEquals("212", vm.state.value.bottomText)
        vm.onDelete()
        assertEquals("21", vm.state.value.bottomText)
        assertEquals("−6.11111111111", vm.state.value.topText)
    }

    @Test fun negateDecimalAndSwap() {
        val vm = vm()
        vm.onCategory(Category.TEMPERATURE)
        vm.onClear(); vm.onDecimal(); vm.onDigit('5'); vm.onNegate()
        assertEquals("−0.5", vm.state.value.topText)
        vm.onSwap()
        assertEquals("°F", vm.state.value.top.symbol)
    }
}
