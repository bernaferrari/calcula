package org.solovyev.android.calculator.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a single history entry stored in the database.
 */
@Entity(tableName = "history_entries")
@Serializable
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long,
    val editorSelection: Int = 0,
    val comment: String = "",
    val isSaved: Boolean = false
)
