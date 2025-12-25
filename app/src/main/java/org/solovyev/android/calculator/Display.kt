/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator

import android.app.Application
import android.content.Context
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import dagger.Lazy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.solovyev.android.Check
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
    private val bus: Bus,
    private val application: Application,
    private val engine: Engine,
    private val clipboard: Lazy<Clipboard>,
    private val notifier: Lazy<Notifier>,
    private val uiPreferences: Lazy<UiPreferences>
) {

    private var view: DisplayView? = null

    private val _stateFlow = MutableStateFlow(DisplayState.empty())
    val stateFlow: StateFlow<DisplayState> = _stateFlow.asStateFlow()

    init {
        bus.register(this)
    }

    @Subscribe
    fun onCopy(o: CopyOperation) {
        copy()
    }

    fun copy() {
        val currentState = _stateFlow.value
        if (!currentState.valid) {
            return
        }
        clipboard.get().setText(currentState.text)
        notifier.get().showMessage(R.string.cpp_text_copied)
    }

    @Subscribe
    fun onCalculationFinished(e: CalculationFinishedEvent) {
        if (e.sequence < _stateFlow.value.sequence) return
        setState(DisplayState.createValid(e.operation, e.result, e.stringResult, e.sequence))
        if (e.messages.isNotEmpty() && uiPreferences.get().showFixableErrorDialog) {
            val context: Context = view?.context ?: application
            FixableErrorsActivity.show(context, e.messages)
        }
    }

    @Subscribe
    fun onCalculationCancelled(e: CalculationCancelledEvent) {
        if (e.sequence < _stateFlow.value.sequence) return
        val error = CalculatorMessages.getBundle().getString(CalculatorMessages.syntax_error)
        setState(DisplayState.createError(e.operation, error, e.sequence))
    }

    @Subscribe
    fun onCalculationFailed(e: CalculationFailedEvent) {
        if (e.sequence < _stateFlow.value.sequence) return
        val error = if (e.exception is ParseException) {
            Utils.getErrorMessage(e.exception)
        } else {
            CalculatorMessages.getBundle().getString(CalculatorMessages.syntax_error)
        }
        setState(DisplayState.createError(e.operation, error, e.sequence))
    }

    @Subscribe
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

    @Subscribe
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
        this.view?.setState(_stateFlow.value)
        this.view?.setEngine(engine)
    }

    fun getState(): DisplayState {
        Check.isMainThread()
        return _stateFlow.value
    }

    fun setState(newState: DisplayState) {
        Check.isMainThread()

        val oldState = _stateFlow.value
        _stateFlow.value = newState
        view?.setState(newState)
        bus.post(ChangedEvent(oldState, newState))
    }

    class CopyOperation

    data class ChangedEvent(
        val oldState: DisplayState,
        val newState: DisplayState
    )
}
