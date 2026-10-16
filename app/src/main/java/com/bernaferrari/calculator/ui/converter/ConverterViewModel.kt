package com.bernaferrari.calculator.ui.converter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.bernaferrari.calculator.domain.convert.Category
import com.bernaferrari.calculator.domain.convert.MeasureUnit
import com.bernaferrari.calculator.domain.convert.Units
import com.bernaferrari.calculator.domain.math.NumberFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal

data class ConverterState(
    val category: Category,
    val top: MeasureUnit,
    val bottom: MeasureUnit,
    /** What the person typed, in the expression language's number format (`.` decimal, `−` sign). */
    val input: String,
    /** True when the top row is the one being typed into. */
    val topActive: Boolean = true,
) {
    private val inputValue: BigDecimal? get() = input.replace('−', '-').removeSuffix(".").takeIf { it.isNotEmpty() && it != "-" }?.toBigDecimalOrNull()

    /** The other row's value, formatted for display; empty while nothing is typed. */
    val output: String
        get() {
            val value = inputValue ?: return ""
            val (from, to) = if (topActive) top to bottom else bottom to top
            return NumberFormatter.format(Units.convert(value, from, to), significantDigits = 12)
        }

    val topText: String get() = if (topActive) input else output
    val bottomText: String get() = if (topActive) output else input
}

class ConverterViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val _state = MutableStateFlow(restore())
    val state: StateFlow<ConverterState> = _state

    fun onCategory(category: Category) {
        if (category == _state.value.category) return
        val (top, bottom) = Units.defaults(category)
        set(ConverterState(category, top, bottom, input = "1"))
    }

    fun onUnit(top: Boolean, unit: MeasureUnit) = _state.update {
        when {
            // Picking the unit the other row already shows just swaps them.
            top && unit == it.bottom -> it.copy(top = it.bottom, bottom = it.top)
            !top && unit == it.top -> it.copy(top = it.bottom, bottom = it.top)
            top -> it.copy(top = unit)
            else -> it.copy(bottom = unit)
        }
    }.also { persist() }

    fun onSwap() = set(_state.value.let { it.copy(top = it.bottom, bottom = it.top) })

    /** Tapping the other row makes it the input, carrying its current value over. */
    fun onActivate(top: Boolean) {
        val s = _state.value
        if (s.topActive == top) return
        val carried = if (top) s.topText else s.bottomText
        set(s.copy(topActive = top, input = carried.takeUnless { 'E' in it }.orEmpty()))
    }

    fun onDigit(digit: Char) = edit { input ->
        when {
            input == "0" -> digit.toString()
            input == "−0" -> "−$digit"
            input.count { it.isDigit() } >= 15 -> input
            else -> input + digit
        }
    }

    fun onDecimal() = edit { if ('.' in it) it else if (it.isEmpty() || it == "−") "${it}0." else "$it." }

    fun onDelete() = edit { it.dropLast(1) }

    fun onClear() = edit { "" }

    fun onNegate() = edit { if (it.startsWith("−")) it.drop(1) else "−$it" }

    private fun edit(change: (String) -> String) = set(_state.value.let { it.copy(input = change(it.input)) })

    private fun set(state: ConverterState) {
        _state.value = state
        persist()
    }

    private fun persist() {
        val s = _state.value
        saved["category"] = s.category.name
        saved["top"] = s.top.id
        saved["bottom"] = s.bottom.id
        saved["input"] = s.input
        saved["topActive"] = s.topActive
    }

    private fun restore(): ConverterState {
        val category = saved.get<String>("category")?.let { runCatching { Category.valueOf(it) }.getOrNull() } ?: Category.LENGTH
        val (top, bottom) = Units.defaults(category)
        return ConverterState(
            category = category,
            top = saved.get<String>("top")?.let(Units::byId) ?: top,
            bottom = saved.get<String>("bottom")?.let(Units::byId) ?: bottom,
            input = saved["input"] ?: "1",
            topActive = saved["topActive"] ?: true,
        )
    }
}
