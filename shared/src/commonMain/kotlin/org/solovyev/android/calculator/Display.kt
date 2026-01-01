package org.solovyev.android.calculator

import jscl.text.ParseException
import org.solovyev.android.calculator.jscl.JsclOperation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.calculations.CalculationCancelledEvent
import org.solovyev.android.calculator.calculations.CalculationFailedEvent
import org.solovyev.android.calculator.calculations.CalculationFinishedEvent
import org.solovyev.android.calculator.calculations.ConversionFailedEvent
import org.solovyev.android.calculator.calculations.ConversionFinishedEvent
import kotlinx.atomicfu.atomic
import org.solovyev.android.calculator.UiPreferences

class Display(
    private val notifier: Notifier,
    private val appPreferences: AppPreferences,
    private val calculator: Calculator,
    private val resourceProvider: ResourceProvider
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val initialized = atomic(false)

    private val _stateFlow = MutableStateFlow(DisplayState.empty())
    val stateFlow: StateFlow<DisplayState> = _stateFlow.asStateFlow()

    private val _changedEvents = MutableSharedFlow<ChangedEvent>()
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

    private val uiPreferences: UiPreferences
        get() = appPreferences.ui

    fun init() {
        if (!initialized.compareAndSet(false, true)) {
            return
        }
        scope.launch { calculator.calculationFinished.collect(::onCalculationFinished) }
        scope.launch { calculator.calculationCancelled.collect(::onCalculationCancelled) }
        scope.launch { calculator.calculationFailed.collect(::onCalculationFailed) }
        scope.launch { calculator.conversionFinished.collect(::onConversionFinished) }
        scope.launch { calculator.conversionFailed.collect(::onConversionFailed) }
    }

    /**
     * Returns the text to copy if the display has valid content, null otherwise.
     */
    fun getTextToCopy(): String? {
        val currentState = _stateFlow.value
        return if (currentState.valid && currentState.text.isNotEmpty()) {
            currentState.text
        } else {
            null
        }
    }

    fun showCopiedMessage() {
        notifier.showMessage(resourceProvider.getString(CalculatorMessages.text_copied))
    }

    fun onCalculationFinished(e: CalculationFinishedEvent) {
        if (e.sequence < _stateFlow.value.sequence) return
        setState(DisplayState.createValid(e.operation, e.result, e.stringResult, e.sequence))
        if (e.messages.isNotEmpty() && uiPreferences.getShowFixableErrorDialogBlocking()) {
            notifier.showFixableErrorDialog(e.messages)
        }
    }

    fun onCalculationCancelled(e: CalculationCancelledEvent) {
        if (e.sequence < _stateFlow.value.sequence) return
        val error = resourceProvider.getString(CalculatorMessages.syntax_error)
        setState(DisplayState.createError(e.operation, error, e.sequence))
    }

    fun onCalculationFailed(e: CalculationFailedEvent) {
        if (e.sequence < _stateFlow.value.sequence) return
        if (calculator.isCalculateOnFly() && e.exception is ParseException) {
            val previous = _stateFlow.value
            if (previous.valid && previous.text.isNotEmpty()) {
                setState(
                    DisplayState(
                        text = previous.text,
                        valid = false,
                        sequence = e.sequence,
                        operation = e.operation,
                        result = previous.result
                    )
                )
                return
            }
            setState(DisplayState.createError(e.operation, "", e.sequence))
            return
        }
        val error = if (e.exception is ParseException) {
            getErrorMessage(e.exception)
        } else {
            resourceProvider.getString(CalculatorMessages.syntax_error)
        }
        setState(DisplayState.createError(e.operation, error, e.sequence))
    }

    fun onConversionFinished(e: ConversionFinishedEvent) {
        if (e.state.sequence != _stateFlow.value.sequence) return
        val result = e.numeralBase.getJsclPrefix() + e.result
        setState(
            DisplayState.createValid(
                e.state.operation,
                e.state.result,
                result,
                e.state.sequence
            )
        )
    }

    fun onConversionFailed(e: ConversionFailedEvent) {
        if (e.state.sequence != _stateFlow.value.sequence) return
        setState(
            DisplayState.createError(
                e.state.operation,
                resourceProvider.getString(CalculatorMessages.syntax_error),
                e.state.sequence
            )
        )
    }

    fun getState(): DisplayState {
        return _stateFlow.value
    }

    fun setState(newState: DisplayState) {
        val oldState = _stateFlow.value
        _stateFlow.value = newState
        scope.launch {
            _changedEvents.emit(ChangedEvent(oldState, newState))
        }
    }

    data class ChangedEvent(
        val oldState: DisplayState,
        val newState: DisplayState
    )

    private fun getErrorMessage(error: Throwable): String {
        return error.message ?: error::class.simpleName ?: "Error"
    }
}
