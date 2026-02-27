package org.solovyev.android.calculator.memory

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

data class MemoryRegisterState(
    val name: String,
    val value: String?
)

interface Memory {
    val valueReadyEvents: SharedFlow<String>
    val activeRegister: StateFlow<String>
    val registers: StateFlow<List<MemoryRegisterState>>

    suspend fun store(value: String)
    suspend fun add(value: String)
    suspend fun subtract(value: String)
    fun recall()
    suspend fun clear()
    suspend fun setActiveRegister(name: String)
}
