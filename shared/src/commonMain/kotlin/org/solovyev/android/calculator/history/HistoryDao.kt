package org.solovyev.android.calculator.history

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class OperatorUsage(
    @ColumnInfo(name = "firstChar") val firstChar: String,
    @ColumnInfo(name = "count") val count: Int
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries WHERE isSaved = 0 ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedHistory(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEntry(): HistoryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry)

    @Delete
    suspend fun delete(entry: HistoryEntry)

    @androidx.room.Update
    suspend fun update(entry: HistoryEntry)

    @Query("DELETE FROM history_entries WHERE isSaved = 0")
    suspend fun clearRecent()

    @Query("DELETE FROM history_entries WHERE isSaved = 1")
    suspend fun clearSaved()

    @Query("DELETE FROM history_entries")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM history_entries")
    suspend fun count(): Int

    @Query("SELECT * FROM history_entries WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getHistorySince(since: Long): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getHistoryBetween(start: Long, end: Long): List<HistoryEntry>

    @Query("SELECT DISTINCT substr(expression, 1, 1) as firstChar, COUNT(*) as count FROM history_entries GROUP BY firstChar")
    suspend fun getOperatorUsage(): List<OperatorUsage>
}
