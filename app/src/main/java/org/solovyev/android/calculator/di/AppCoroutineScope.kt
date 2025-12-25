package org.solovyev.android.calculator.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * Application-scoped CoroutineScope for long-running operations.
 * This scope survives configuration changes and is tied to the app lifecycle.
 *
 * Use this for:
 * - Background work that should continue regardless of UI state
 * - Initialization tasks
 * - Data syncing
 *
 * Don't use this for:
 * - UI-related work (use viewModelScope or lifecycleScope instead)
 * - Work that should be cancelled when a screen closes
 */
@Singleton
class AppCoroutineScope @Inject constructor(
    private val dispatchers: AppDispatchers
) : CoroutineScope {

    private val supervisorJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = supervisorJob + dispatchers.main

    /**
     * Launch a coroutine on the IO dispatcher.
     */
    fun launchIO(block: suspend CoroutineScope.() -> Unit) =
        launch(dispatchers.io, block = block)

    /**
     * Launch a coroutine on the Default dispatcher.
     */
    fun launchDefault(block: suspend CoroutineScope.() -> Unit) =
        launch(dispatchers.default, block = block)

    /**
     * Launch a coroutine on the Main dispatcher.
     */
    fun launchMain(block: suspend CoroutineScope.() -> Unit) =
        launch(dispatchers.main, block = block)
}
