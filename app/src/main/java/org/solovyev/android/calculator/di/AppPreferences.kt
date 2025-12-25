package org.solovyev.android.calculator.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

// DataStore instances as extension properties
private val Context.uiDataStore: DataStore<Preferences> by preferencesDataStore(name = "ui_preferences")
private val Context.floatingDataStore: DataStore<Preferences> by preferencesDataStore(name = "floating_preferences")
private val Context.tabsDataStore: DataStore<Preferences> by preferencesDataStore(name = "tabs_preferences")

/**
 * Modern preferences management using Jetpack DataStore.
 * Provides type-safe, async access to app preferences.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val ui: UiPreferencesStore by lazy { UiPreferencesStore(context.uiDataStore) }
    val floating: FloatingPreferencesStore by lazy { FloatingPreferencesStore(context.floatingDataStore) }
    val tabs: TabsPreferencesStore by lazy { TabsPreferencesStore(context.tabsDataStore) }
}

/**
 * UI-related preferences (app opens count, rate us shown, etc.)
 */
class UiPreferencesStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        val APP_VERSION = intPreferencesKey("app_version")
        val OPENED = intPreferencesKey("opened")
        val RATE_US_SHOWN = booleanPreferencesKey("rate_us_shown")
    }

    val appVersion: Flow<Int?> = dataStore.data.map { it[APP_VERSION] }
    val opened: Flow<Int> = dataStore.data.map { it[OPENED] ?: 0 }
    val rateUsShown: Flow<Boolean> = dataStore.data.map { it[RATE_US_SHOWN] ?: false }

    suspend fun setAppVersion(version: Int) {
        dataStore.edit { it[APP_VERSION] = version }
    }

    suspend fun setOpened(count: Int) {
        dataStore.edit { it[OPENED] = count }
    }

    suspend fun incrementOpened(): Int {
        var newCount = 0
        dataStore.edit { prefs ->
            newCount = (prefs[OPENED] ?: 0) + 1
            prefs[OPENED] = newCount
        }
        return newCount
    }

    suspend fun setRateUsShown(shown: Boolean) {
        dataStore.edit { it[RATE_US_SHOWN] = shown }
    }

    // Synchronous access for legacy code migration
    fun getOpenedBlocking(): Int = runBlocking { opened.first() }
    fun getAppVersionBlocking(): Int? = runBlocking { appVersion.first() }
    fun getRateUsShownBlocking(): Boolean = runBlocking { rateUsShown.first() }
}

/**
 * Floating calculator preferences
 */
class FloatingPreferencesStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        val SHOW_APP_ICON = booleanPreferencesKey("show_app_icon")
        val X_POSITION = intPreferencesKey("x_position")
        val Y_POSITION = intPreferencesKey("y_position")
        val WIDTH = intPreferencesKey("width")
        val HEIGHT = intPreferencesKey("height")
    }

    val showAppIcon: Flow<Boolean> = dataStore.data.map { it[SHOW_APP_ICON] ?: true }
    val xPosition: Flow<Int> = dataStore.data.map { it[X_POSITION] ?: 0 }
    val yPosition: Flow<Int> = dataStore.data.map { it[Y_POSITION] ?: 0 }

    suspend fun setShowAppIcon(show: Boolean) {
        dataStore.edit { it[SHOW_APP_ICON] = show }
    }

    suspend fun setPosition(x: Int, y: Int) {
        dataStore.edit {
            it[X_POSITION] = x
            it[Y_POSITION] = y
        }
    }

    suspend fun setSize(width: Int, height: Int) {
        dataStore.edit {
            it[WIDTH] = width
            it[HEIGHT] = height
        }
    }
}

/**
 * Tab-related preferences
 */
class TabsPreferencesStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        val SELECTED_TAB = stringPreferencesKey("selected_tab")
    }

    val selectedTab: Flow<String?> = dataStore.data.map { it[SELECTED_TAB] }

    suspend fun setSelectedTab(tab: String) {
        dataStore.edit { it[SELECTED_TAB] = tab }
    }
}
