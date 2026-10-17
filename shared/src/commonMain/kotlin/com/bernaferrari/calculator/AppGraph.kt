package com.bernaferrari.calculator

import com.bernaferrari.calculator.data.GraphStore
import com.bernaferrari.calculator.data.StoredGraphStore
import com.bernaferrari.calculator.data.StoredHistoryRepository
import com.bernaferrari.calculator.data.StoredPreferencesRepository
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import com.bernaferrari.calculator.platform.keyValueStore

/** The app's few long-lived objects, created on first use. */
object AppGraph {
    private val store by lazy { keyValueStore() }
    val preferences: PreferencesRepository by lazy { StoredPreferencesRepository(store) }
    val history: CalculatorRepository by lazy { StoredHistoryRepository(store) }
    val graph: GraphStore by lazy { StoredGraphStore(store) }
}

/** Shown in Settings → About. Keep in step with the Android versionName and the iOS bundle version. */
const val APP_VERSION = "3.0"
