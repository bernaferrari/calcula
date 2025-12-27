package org.solovyev.android.calculator.ui.compose.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.history.HistoryState
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryComposeViewModel @Inject constructor(
    private val history: History,
    private val editor: Editor
) : ViewModel() {

    private val _refreshTick = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = _refreshTick.asStateFlow()

    init {
        viewModelScope.launch {
            history.recentEvents.collect { bump() }
        }
        viewModelScope.launch {
            history.savedEvents.collect { bump() }
        }
    }

    fun getRecent(): List<HistoryState> = history.getRecent()

    fun getSaved(): List<HistoryState> = history.getSaved()

    fun useState(state: HistoryState) {
        editor.setState(state.editor)
    }

    fun clearRecent() {
        history.clearRecent()
    }

    fun clearSaved() {
        history.clearSaved()
    }

    fun removeSaved(state: HistoryState) {
        history.removeSaved(state)
    }

    fun updateSaved(state: HistoryState) {
        history.updateSaved(state)
    }

    private fun bump() {
        _refreshTick.value = _refreshTick.value + 1
    }
}
