package com.bernaferrari.calculator.domain.repository

import com.bernaferrari.calculator.domain.model.CalculationHistory
import kotlinx.coroutines.flow.Flow

interface CalculatorRepository {
    fun getAllHistory(): Flow<List<CalculationHistory>>

    fun getRecentHistory(limit: Int = 50): Flow<List<CalculationHistory>>

    suspend fun insertCalculation(calculation: CalculationHistory)

    suspend fun deleteCalculation(calculation: CalculationHistory)

    suspend fun clearAllHistory()

    suspend fun deleteOldHistory(cutoffTime: Long)

    suspend fun getHistoryCount(): Int
}
