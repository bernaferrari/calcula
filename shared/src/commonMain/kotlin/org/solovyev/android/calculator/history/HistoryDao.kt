package org.solovyev.android.calculator.history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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
}
