package com.bernaferrari.calculator

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job

/** Await cancellation before replacing the main dispatcher used by these view models. */
suspend fun ViewModelStore.clearAndAwait() {
    val jobs = keys().mapNotNull { get(it)?.viewModelScope?.coroutineContext?.get(Job) }
    clear()
    jobs.forEach { it.join() }
}
