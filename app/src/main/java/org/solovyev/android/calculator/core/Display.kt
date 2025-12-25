package org.solovyev.android.calculator.core

import android.app.Application
import android.content.Context
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
import org.solovyev.android.calculator.CalculatorMessages
import org.solovyev.android.calculator.Clipboard
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.DisplayView
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Notifier
import org.solovyev.android.calculator.ParseException
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.UiPreferences
import org.solovyev.android.calculator.Utils
import org.solovyev.android.calculator.calculations.CalculationCancelledEvent
import org.solovyev.android.calculator.calculations.CalculationFailedEvent
import org.solovyev.android.calculator.calculations.CalculationFinishedEvent
import org.solovyev.android.calculator.calculations.ConversionFailedEvent
import org.solovyev.android.calculator.calculations.ConversionFinishedEvent
import org.solovyev.android.calculator.errors.FixableErrorsActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Display @Inject constructor(
    private val application: Application,
    private val engine: Engine,
    private val clipboard: Lazy<Clipboard>,
    private val notifier: Lazy<Notifier>,
    private val uiPreferences: Lazy<UiPreferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var view: DisplayView? = null

    private val _state = MutableStateFlow(DisplayState.empty())
    val stateFlow: StateFlow<DisplayState> = _state.asStateFlow()

    val state: DisplayState get() = _state.value

    private val _changedEvents = MutableSharedFlow<ChangedEvent>()
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

    fun copy() {
        if (!state.valid) {
            return
        }
        clipboard.get().setText(state.text)
        notifier.get().showMessage(R.string.cpp_text_copied)
    }

    fun onCalculationFinished(e: CalculationFinishedEvent) {
        if (e.sequence < state.sequence) return
        setState(DisplayState.createValid(e.operation, e.result, e.stringResult, e.sequence))
        if (e.messages.isNotEmpty() && uiPreferences.get().showFixableErrorDialog) {
            val context: Context = view?.context ?: application
            FixableErrorsActivity.show(context, e.messages)
        }
    }

    fun onCalculationCancelled(e: CalculationCancelledEvent) {
        if (e.sequence < state.sequence) return
        val error = CalculatorMessages.getBundle().getString(CalculatorMessages.syntax_error)
        setState(DisplayState.createError(e.operation, error, e.sequence))
    }

    fun onCalculationFailed(e: CalculationFailedEvent) {
        if (e.sequence < state.sequence) return
        val error = if (e.exception is ParseException) {
            Utils.getErrorMessage(e.exception)
        } else {
            CalculatorMessages.getBundle().getString(CalculatorMessages.syntax_error)
        }
        setState(DisplayState.createError(e.operation, error, e.sequence))
    }

    fun onConversionFinished(e: ConversionFinishedEvent) {
        if (e.state.sequence != state.sequence) return
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
        if (e.state.sequence != state.sequence) return
        setState(
            DisplayState.createError(
                e.state.operation,
                CalculatorMessages.getBundle().getString(CalculatorMessages.syntax_error),
                e.state.sequence
            )
        )
    }

    fun clearView(view: DisplayView) {
        Check.isMainThread()
        if (this.view != view) {
            return
        }
        this.view?.onDestroy()
        this.view = null
    }

    fun setView(view: DisplayView) {
        Check.isMainThread()
        this.view = view
        view.setState(state)
        view.setEngine(engine)
    }

    fun setState(newState: DisplayState) {
        Check.isMainThread()
        val oldState = _state.value
        _state.value = newState
        view?.setState(newState)
        scope.launch {
            _changedEvents.emit(ChangedEvent(oldState, newState))
        }
    }

    data class ChangedEvent(
        val oldState: DisplayState,
        val newState: DisplayState
    )

    object CopyOperation
}
