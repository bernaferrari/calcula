package org.solovyev.android.calculator.history

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [HistoryEntry::class],
    version = 1,
    exportSchema = false
)
@ConstructedBy(CalculatorDatabaseConstructor::class)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}

// The Room compiler generates the 'actual' for all platforms.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object CalculatorDatabaseConstructor : RoomDatabaseConstructor<CalculatorDatabase> {
    override fun initialize(): CalculatorDatabase
}
