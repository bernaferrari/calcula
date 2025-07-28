package com.bernaferrari.calculator.data.repository

import com.bernaferrari.calculator.data.local.dao.CalculationHistoryDao
import com.bernaferrari.calculator.data.local.entities.CalculationHistoryEntity
import com.bernaferrari.calculator.domain.model.CalculationHistory
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculatorRepositoryImpl
    @Inject
    constructor(
        private val dao: CalculationHistoryDao,
    ) : CalculatorRepository {
        override fun getAllHistory(): Flow<List<CalculationHistory>> =
            dao.getAllHistory().map { entities ->
                entities.map { it.toDomainModel() }
            }

        override fun getRecentHistory(limit: Int): Flow<List<CalculationHistory>> =
            dao.getRecentHistory(limit).map { entities ->
                entities.map { it.toDomainModel() }
            }

        override suspend fun insertCalculation(calculation: CalculationHistory) {
            dao.insertCalculation(calculation.toEntity())
        }

        override suspend fun deleteCalculation(calculation: CalculationHistory) {
            dao.deleteCalculation(calculation.toEntity())
        }

        override suspend fun clearAllHistory() {
            dao.clearAllHistory()
        }

        override suspend fun deleteOldHistory(cutoffTime: Long) {
            dao.deleteOldHistory(cutoffTime)
        }

        override suspend fun getHistoryCount(): Int = dao.getHistoryCount()

        private fun CalculationHistoryEntity.toDomainModel(): CalculationHistory =
            CalculationHistory(
                id = id,
                expression = expression,
                result = result,
                timestamp = timestamp,
            )

        private fun CalculationHistory.toEntity(): CalculationHistoryEntity =
            CalculationHistoryEntity(
                id = id,
                expression = expression,
                result = result,
                timestamp = timestamp,
            )
    }
