package org.solovyev.android.calculator.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.history.HistoryState

class HistoryViewModel(
    private val history: History
) : ViewModel() {

    val recent = history.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val saved = history.observeSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSave(state: HistoryState) {
        viewModelScope.launch { history.updateSaved(state) }
    }

    fun onEdit(state: HistoryState) {
        viewModelScope.launch { history.updateSaved(state) }
    }

    fun onDelete(state: HistoryState) {
        viewModelScope.launch { history.removeSaved(state) }
    }

    fun onClearRecent() {
        viewModelScope.launch { history.clearRecent() }
    }

    fun onClearSaved() {
        viewModelScope.launch { history.clearSaved() }
    }
}
