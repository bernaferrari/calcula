package org.solovyev.android.calculator.ui.compose.converter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Clipboard
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.Named
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.converter.Convertible
import org.solovyev.android.calculator.converter.ConvertibleDimension
import org.solovyev.android.calculator.converter.NumeralBaseDimension
import org.solovyev.android.calculator.converter.UnitDimension
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.text.NaturalComparator
import javax.inject.Inject

data class ConverterUiState(
    val dimensions: List<Named<ConvertibleDimension>>,
    val unitsFrom: List<Named<Convertible>>,
    val unitsTo: List<Named<Convertible>>,
    val selectedDimensionIndex: Int,
    val selectedFromIndex: Int,
    val selectedToIndex: Int,
    val input: String,
    val output: String,
    val inputError: Boolean,
    val isNumeralBase: Boolean
)

@HiltViewModel
class ConverterComposeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val clipboard: Clipboard,
    private val editor: Editor
) : ViewModel() {

    private val dimensions: List<Named<ConvertibleDimension>> = buildDimensions()

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
            val dimensionIndex = appPreferences.converter.getLastDimensionBlocking() ?: 0
            val fromIndex = appPreferences.converter.getLastUnitsFromBlocking() ?: 0
            val toIndex = appPreferences.converter.getLastUnitsToBlocking() ?: 0
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
        val preferredTo = _state.value.unitsTo.getOrNull(_state.value.selectedToIndex)?.item
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
        val oldFrom = unitsFrom.getOrNull(current.selectedFromIndex)?.item ?: return
        val oldTo = unitsTo.getOrNull(current.selectedToIndex)?.item ?: return
        val newFromIndex = unitsFrom.indexOfFirst { it.item == oldTo }.coerceAtLeast(0)
        val newUnitsTo = unitsFrom.filter { it.item != oldTo }
        val newToIndex = newUnitsTo.indexOfFirst { it.item == oldFrom }.coerceAtLeast(0)
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

    fun onCopy() {
        runCatching { clipboard.setText(_state.value.output) }
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
        val isNumeralBase = dimension.item is NumeralBaseDimension
        val unitsFrom = dimension.item.getUnits()
            .map { it.named(context) }
            .sortedWith(NaturalComparator)

        val safeFromIndex = fromIndex.coerceIn(0, (unitsFrom.size - 1).coerceAtLeast(0))
        val fromUnit = unitsFrom.getOrNull(safeFromIndex)?.item

        val unitsTo = unitsFrom.filter { it.item != fromUnit }
        val preferredToIndex = preferredTo?.let { preferred ->
            unitsTo.indexOfFirst { it.item == preferred }.takeIf { it >= 0 }
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
        unitsFrom: List<Named<Convertible>>,
        unitsTo: List<Named<Convertible>>,
        fromIndex: Int,
        toIndex: Int,
        input: String
    ): ConversionResult {
        if (input.isBlank()) {
            return ConversionResult("", isError = false)
        }
        val from = unitsFrom.getOrNull(fromIndex)?.item
        val to = unitsTo.getOrNull(toIndex)?.item
        if (from == null || to == null) {
            return ConversionResult("", isError = true)
        }
        return try {
            ConversionResult(from.convert(to, input), isError = false)
        } catch (e: RuntimeException) {
            ConversionResult("", isError = true)
        }
    }

    private fun buildDimensions(): List<Named<ConvertibleDimension>> {
        val list = UnitDimension.values().map { it.named(context) }.toMutableList()
        list.add(NumeralBaseDimension.get().named(context))
        return list
    }

    private data class ConversionResult(val value: String, val isError: Boolean)
}
