package org.solovyev.android.calculator

data class TapeEntry(
    val id: Long,
    val expression: String,
    val result: String,
    val timestamp: Long,
    val committed: Boolean
)
