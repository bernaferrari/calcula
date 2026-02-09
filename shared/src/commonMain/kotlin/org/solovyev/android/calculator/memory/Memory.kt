package org.solovyev.android.calculator.memory

import kotlinx.coroutines.flow.SharedFlow

interface Memory {
    val valueReadyEvents: SharedFlow<String>

    suspend fun store(value: String)
    suspend fun add(value: String)
    suspend fun subtract(value: String)
    fun recall()
    suspend fun clear()
}
