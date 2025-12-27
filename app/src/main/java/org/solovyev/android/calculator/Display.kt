package org.solovyev.android.calculator

import android.app.Application
import dagger.Lazy
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
import org.solovyev.android.Check
import org.solovyev.android.calculator.calculations.CalculationCancelledEvent
import org.solovyev.android.calculator.calculations.CalculationFailedEvent
import org.solovyev.android.calculator.calculations.CalculationFinishedEvent
import org.solovyev.android.calculator.calculations.ConversionFailedEvent
import org.solovyev.android.calculator.calculations.ConversionFinishedEvent
import org.solovyev.android.calculator.errors.FixableErrorsActivity
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Display @Inject constructor(
    private val application: Application,
    private val clipboard: Lazy<Clipboard>,
    private val notifier: Lazy<Notifier>,
    private val uiPreferences: Lazy<UiPreferences>,
    private val calculator: Calculator
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val initialized = AtomicBoolean(false)

    private val _stateFlow = MutableStateFlow(DisplayState.empty())
    val stateFlow: StateFlow<DisplayState> = _stateFlow.asStateFlow()

    private val _changedEvents = MutableSharedFlow<ChangedEvent>()
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

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

    fun copy() {
        val currentState = _stateFlow.value
        if (!currentState.valid) {
            return
        }
        clipboard.get().setText(currentState.text)
        notifier.get().showMessage(R.string.cpp_text_copied)
    }

    fun onCalculationFinished(e: CalculationFinishedEvent) {
        if (e.sequence < _stateFlow.value.sequence) return
        setState(DisplayState.createValid(e.operation, e.result, e.stringResult, e.sequence))
        if (e.messages.isNotEmpty() && uiPreferences.get().showFixableErrorDialog) {
            FixableErrorsActivity.show(application, e.messages)
        }
    }

    fun onCalculationCancelled(e: CalculationCancelledEvent) {
        if (e.sequence < _stateFlow.value.sequence) return
        val error = CalculatorMessages.getBundle().getString(CalculatorMessages.syntax_error)
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
            Utils.getErrorMessage(e.exception)
        } else {
            CalculatorMessages.getBundle().getString(CalculatorMessages.syntax_error)
        }
        setState(DisplayState.createError(e.operation, error, e.sequence))
    }

    fun onConversionFinished(e: ConversionFinishedEvent) {
        if (e.state.sequence != _stateFlow.value.sequence) return
        val result = e.numeralBase.getJsclPrefix() + e.result
        setState(
            DisplayState.createValid(
                e.state.getOperation(),
                e.state.getResult(),
                result,
                e.state.sequence
            )
        )
    }

    fun onConversionFailed(e: ConversionFailedEvent) {
        if (e.state.sequence != _stateFlow.value.sequence) return
        setState(
            DisplayState.createError(
                e.state.getOperation(),
                CalculatorMessages.getBundle().getString(CalculatorMessages.syntax_error),
                e.state.sequence
            )
        )
    }

    fun getState(): DisplayState {
        Check.isMainThread()
        return _stateFlow.value
    }

    fun setState(newState: DisplayState) {
        Check.isMainThread()

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
}
