package org.solovyev.android.calculator.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides coroutine dispatchers for the application.
 * This replaces @Named("thread-*") Executor pattern with modern coroutines.
 *
 * Benefits:
 * - Type-safe injection
 * - Structured concurrency
 * - Easy to mock in tests
 * - Consistent cancellation handling
 */
@Singleton
class AppDispatchers @Inject constructor() {
    /**
     * Main/UI thread dispatcher.
     * Use for UI updates and short, non-blocking operations.
     */
    val main: CoroutineDispatcher = Dispatchers.Main

    /**
     * Main dispatcher with immediate execution if already on main thread.
     * Avoids unnecessary dispatch when already on the correct thread.
     */
    val mainImmediate: CoroutineDispatcher = Dispatchers.Main.immediate

    /**
     * IO dispatcher for disk and network operations.
     * Use for file I/O, database, network calls.
     */
    val io: CoroutineDispatcher = Dispatchers.IO

    /**
     * Default dispatcher for CPU-intensive work.
     * Use for parsing, sorting, complex calculations.
     */
    val default: CoroutineDispatcher = Dispatchers.Default

    /**
     * Unconfined dispatcher - runs in the current thread until first suspension.
     * Use sparingly, mainly for testing or specific edge cases.
     */
    val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
