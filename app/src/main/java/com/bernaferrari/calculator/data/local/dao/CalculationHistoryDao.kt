package com.bernaferrari.calculator.data.local.dao

import androidx.room.*
import com.bernaferrari.calculator.data.local.entities.CalculationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
    
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistoryEntity>>
    
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<CalculationHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: CalculationHistoryEntity)
    
    @Delete
    suspend fun deleteCalculation(calculation: CalculationHistoryEntity)
    
    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()
    
    @Query("DELETE FROM calculation_history WHERE timestamp < :cutoffTime")
    suspend fun deleteOldHistory(cutoffTime: Long)
    
    @Query("SELECT COUNT(*) FROM calculation_history")
    suspend fun getHistoryCount(): Int
}