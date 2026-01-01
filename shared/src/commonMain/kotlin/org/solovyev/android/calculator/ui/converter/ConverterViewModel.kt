package org.solovyev.android.calculator.ui.converter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.solovyev.android.calculator.Editor
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.converter.*
import org.solovyev.android.calculator.text.NaturalComparator

data class ConverterUiState(
    val dimensions: List<ConvertibleDimension>,
    val unitsFrom: List<Convertible>,
    val unitsTo: List<Convertible>,
    val selectedDimensionIndex: Int,
    val selectedFromIndex: Int,
    val selectedToIndex: Int,
    val input: String,
    val output: String,
    val inputError: Boolean,
    val isNumeralBase: Boolean
)

class ConverterViewModel(
    private val appPreferences: AppPreferences,
    private val editor: Editor
) : ViewModel() {

    private val dimensions: List<ConvertibleDimension> = buildDimensions()

    private val _state = MutableStateFlow(
        ConverterUiState(
            dimensions = dimensions,
            unitsFrom = emptyList(),
            unitsTo = emptyList(),
            selectedDimensionIndex = 0,
            selectedFromIndex = 0,
            selectedToIndex = 0,
            input = "1",
            output = "",
            inputError = false,
            isNumeralBase = false
        )
    )
    val state: StateFlow<ConverterUiState> = _state

    init {
        viewModelScope.launch {
            val converterPrefs = appPreferences.converter
            val dimensionIndex = converterPrefs.getLastDimension() ?: 0
            val fromIndex = converterPrefs.getLastUnitsFrom() ?: 0
            val toIndex = converterPrefs.getLastUnitsTo() ?: 0
            updateState(
                dimensionIndex = dimensionIndex,
                fromIndex = fromIndex,
                toIndex = toIndex,
                input = _state.value.input,
                preferredTo = null,
                validate = true
            )
        }
    }

    fun onDimensionSelected(index: Int) {
        updateState(
            dimensionIndex = index,
            fromIndex = 0,
            toIndex = 0,
            input = _state.value.input,
            preferredTo = null,
            validate = true
        )
    }

    fun onFromUnitSelected(index: Int) {
        val preferredTo = _state.value.unitsTo.getOrNull(_state.value.selectedToIndex)
        updateState(
            dimensionIndex = _state.value.selectedDimensionIndex,
            fromIndex = index,
            toIndex = 0,
            input = _state.value.input,
            preferredTo = preferredTo,
            validate = true
        )
    }

    fun onToUnitSelected(index: Int) {
        updateState(
            dimensionIndex = _state.value.selectedDimensionIndex,
            fromIndex = _state.value.selectedFromIndex,
            toIndex = index,
            input = _state.value.input,
            preferredTo = null,
            validate = true
        )
    }

    fun onInputChanged(text: String) {
        updateState(
            dimensionIndex = _state.value.selectedDimensionIndex,
            fromIndex = _state.value.selectedFromIndex,
            toIndex = _state.value.selectedToIndex,
            input = text,
            preferredTo = null,
            validate = false
        )
    }

    fun onInputCommitted() {
        updateState(
            dimensionIndex = _state.value.selectedDimensionIndex,
            fromIndex = _state.value.selectedFromIndex,
            toIndex = _state.value.selectedToIndex,
            input = _state.value.input,
            preferredTo = null,
            validate = true
        )
    }

    fun onSwap() {
        val current = _state.value
        val unitsFrom = current.unitsFrom
        val unitsTo = current.unitsTo
        if (unitsFrom.isEmpty() || unitsTo.isEmpty()) {
            return
        }
        val oldFrom = unitsFrom.getOrNull(current.selectedFromIndex) ?: return
        val oldTo = unitsTo.getOrNull(current.selectedToIndex) ?: return
        val newFromIndex = unitsFrom.indexOfFirst { it == oldTo }.coerceAtLeast(0)
        val newUnitsTo = unitsFrom.filter { it != oldTo }
        val newToIndex = newUnitsTo.indexOfFirst { it == oldFrom }.coerceAtLeast(0)
        updateState(
            dimensionIndex = current.selectedDimensionIndex,
            fromIndex = newFromIndex,
            toIndex = newToIndex,
            input = current.output.ifEmpty { current.input },
            preferredTo = oldFrom,
            validate = true
        )
    }

    fun onUse() {
        runCatching { editor.insert(_state.value.output) }
    }

    /**
     * Returns the output text to copy, or null if empty.
     */
    fun getTextToCopy(): String? {
        val output = _state.value.output
        return output.ifEmpty { null }
    }

    fun saveLastUsed() {
        val state = _state.value
        viewModelScope.launch {
            appPreferences.converter.setLastUsed(
                state.selectedDimensionIndex,
                state.selectedFromIndex,
                state.selectedToIndex
            )
        }
    }

    private fun updateState(
        dimensionIndex: Int,
        fromIndex: Int,
        toIndex: Int,
        input: String,
        preferredTo: Convertible?,
        validate: Boolean
    ) {
        val dimension = dimensions.getOrNull(dimensionIndex) ?: dimensions.first()
        val isNumeralBase = dimension is NumeralBaseDimension
        val unitsFrom = dimension.getUnits()
            .sortedWith(NaturalComparator)

        val safeFromIndex = fromIndex.coerceIn(0, (unitsFrom.size - 1).coerceAtLeast(0))
        val fromUnit = unitsFrom.getOrNull(safeFromIndex)

        val unitsTo = unitsFrom.filter { it != fromUnit }
        val preferredToIndex = preferredTo?.let { preferred ->
            unitsTo.indexOfFirst { it == preferred }.takeIf { it >= 0 }
        }
        val safeToIndex = (preferredToIndex ?: toIndex)
            .coerceIn(0, (unitsTo.size - 1).coerceAtLeast(0))

        val output = convert(unitsFrom, unitsTo, safeFromIndex, safeToIndex, input)

        _state.update {
            it.copy(
                unitsFrom = unitsFrom,
                unitsTo = unitsTo,
                selectedDimensionIndex = dimensionIndex.coerceIn(0, dimensions.lastIndex),
                selectedFromIndex = safeFromIndex,
                selectedToIndex = safeToIndex,
                input = input,
                output = output.value,
                inputError = validate && output.isError,
                isNumeralBase = isNumeralBase
            )
        }
    }

    private fun convert(
        unitsFrom: List<Convertible>,
        unitsTo: List<Convertible>,
        fromIndex: Int,
        toIndex: Int,
        input: String
    ): ConversionResult {
        if (input.isBlank()) {
            return ConversionResult("", isError = false)
        }
        val from = unitsFrom.getOrNull(fromIndex)
        val to = unitsTo.getOrNull(toIndex)
        if (from == null || to == null) {
            return ConversionResult("", isError = true)
        }
        return try {
            ConversionResult(from.convert(to, input), isError = false)
        } catch (e: Exception) {
            ConversionResult("", isError = true)
        }
    }

    private fun buildDimensions(): List<ConvertibleDimension> {
        val list = UnitDimension.values().toMutableList<ConvertibleDimension>()
        list.add(NumeralBaseDimension.get())
        return list
    }

    private data class ConversionResult(val value: String, val isError: Boolean)
}
