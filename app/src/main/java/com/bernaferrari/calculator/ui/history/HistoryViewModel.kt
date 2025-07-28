package com.bernaferrari.calculator.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.domain.model.CalculationHistory
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryState(
    val history: List<CalculationHistory> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: CalculatorRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(HistoryState())
    val state = _state.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            repository.getAllHistory().collect { history ->
                _state.value = _state.value.copy(
                    history = history,
                    isLoading = false
                )
            }
        }
    }
    
    fun deleteHistoryItem(history: CalculationHistory) {
        viewModelScope.launch {
            repository.deleteCalculation(history)
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}