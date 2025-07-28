package com.bernaferrari.calculator.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bernaferrari.calculator.data.local.dao.CalculationHistoryDao
import com.bernaferrari.calculator.data.local.entities.CalculationHistoryEntity

@Database(
    entities = [CalculationHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun calculationHistoryDao(): CalculationHistoryDao
}
