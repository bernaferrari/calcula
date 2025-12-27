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
import org.solovyev.android.calculator.preferences.SettingsPreferencesStore
import javax.inject.Inject
import javax.inject.Singleton

// DataStore instances as extension properties
private val Context.uiDataStore: DataStore<Preferences> by preferencesDataStore(name = "ui_preferences")
private val Context.floatingDataStore: DataStore<Preferences> by preferencesDataStore(name = "floating_preferences")
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_preferences")
private val Context.converterDataStore: DataStore<Preferences> by preferencesDataStore(name = "converter_preferences")
private val Context.wizardDataStore: DataStore<Preferences> by preferencesDataStore(name = "wizard_preferences")

/**
 * Modern preferences management using Jetpack DataStore.
 * Provides type-safe, async access to app preferences.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val ui: UiPreferencesStore by lazy { UiPreferencesStore(context.uiDataStore) }
    val floating: FloatingPreferencesStore by lazy {
        FloatingPreferencesStore(context.floatingDataStore)
    }
    val settings: SettingsPreferencesStore by lazy {
        SettingsPreferencesStore(context.settingsDataStore)
    }
    val converter: ConverterPreferencesStore by lazy { ConverterPreferencesStore(context.converterDataStore) }
    val wizard: WizardPreferencesStore by lazy { WizardPreferencesStore(context.wizardDataStore) }
}

/**
 * UI-related preferences (app opens count, rate us shown, etc.)
 */
class UiPreferencesStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        val APP_VERSION = intPreferencesKey("app_version")
        val OPENED = intPreferencesKey("opened")
        val RATE_US_SHOWN = booleanPreferencesKey("rate_us_shown")
        val SHOW_FIXABLE_ERROR_DIALOG = booleanPreferencesKey("show_fixable_error_dialog")
    }

    val appVersion: Flow<Int?> = dataStore.data.map { it[APP_VERSION] }
    val opened: Flow<Int> = dataStore.data.map { it[OPENED] ?: 0 }
    val rateUsShown: Flow<Boolean> = dataStore.data.map { it[RATE_US_SHOWN] ?: false }
    val showFixableErrorDialog: Flow<Boolean> = dataStore.data.map { it[SHOW_FIXABLE_ERROR_DIALOG] ?: true }

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

    suspend fun setShowFixableErrorDialog(show: Boolean) {
        dataStore.edit { it[SHOW_FIXABLE_ERROR_DIALOG] = show }
    }

    // Synchronous access for legacy code migration
    fun getOpenedBlocking(): Int = runBlocking { opened.first() }
    fun getAppVersionBlocking(): Int? = runBlocking { appVersion.first() }
    fun getRateUsShownBlocking(): Boolean = runBlocking { rateUsShown.first() }
    fun getShowFixableErrorDialogBlocking(): Boolean = runBlocking { showFixableErrorDialog.first() }
}

/**
 * Floating calculator preferences
 */
class FloatingPreferencesStore(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val SHOW_APP_ICON = booleanPreferencesKey("show_app_icon")
        val X_POSITION = intPreferencesKey("x_position")
        val Y_POSITION = intPreferencesKey("y_position")
        val WIDTH = intPreferencesKey("width")
        val HEIGHT = intPreferencesKey("height")
    }

    val showAppIcon: Flow<Boolean> = dataStore.data.map { it[SHOW_APP_ICON] ?: true }
    val xPosition: Flow<Int?> = dataStore.data.map { it[X_POSITION] }
    val yPosition: Flow<Int?> = dataStore.data.map { it[Y_POSITION] }
    val width: Flow<Int?> = dataStore.data.map { it[WIDTH] }
    val height: Flow<Int?> = dataStore.data.map { it[HEIGHT] }

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

    fun getXBlocking(): Int? = runBlocking { xPosition.first() }
    fun getYBlocking(): Int? = runBlocking { yPosition.first() }
    fun getWidthBlocking(): Int? = runBlocking { width.first() }
    fun getHeightBlocking(): Int? = runBlocking { height.first() }

}

/**
 * Converter dialog preferences.
 */
class ConverterPreferencesStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        val LAST_DIMENSION = intPreferencesKey("converter_last_dimension")
        val LAST_UNITS_FROM = intPreferencesKey("converter_last_units_from")
        val LAST_UNITS_TO = intPreferencesKey("converter_last_units_to")
    }

    val lastDimension: Flow<Int?> = dataStore.data.map { it[LAST_DIMENSION] }
    val lastUnitsFrom: Flow<Int?> = dataStore.data.map { it[LAST_UNITS_FROM] }
    val lastUnitsTo: Flow<Int?> = dataStore.data.map { it[LAST_UNITS_TO] }

    suspend fun setLastUsed(dimension: Int, from: Int, to: Int) {
        dataStore.edit {
            it[LAST_DIMENSION] = dimension
            it[LAST_UNITS_FROM] = from
            it[LAST_UNITS_TO] = to
        }
    }

    fun getLastDimensionBlocking(): Int? = runBlocking { lastDimension.first() }
    fun getLastUnitsFromBlocking(): Int? = runBlocking { lastUnitsFrom.first() }
    fun getLastUnitsToBlocking(): Int? = runBlocking { lastUnitsTo.first() }
}

/**
 * Wizard flow preferences.
 */
class WizardPreferencesStore(private val dataStore: DataStore<Preferences>) {

    private fun lastStepKey(flowName: String) = stringPreferencesKey("wizard_last_step_$flowName")
    private fun finishedKey(flowName: String) = booleanPreferencesKey("wizard_finished_$flowName")

    fun getLastStepBlocking(flowName: String): String? =
        runBlocking { dataStore.data.map { it[lastStepKey(flowName)] }.first() }

    fun getFinishedBlocking(flowName: String): Boolean =
        runBlocking { dataStore.data.map { it[finishedKey(flowName)] ?: false }.first() }

    suspend fun setLastStep(flowName: String, stepName: String) {
        dataStore.edit { it[lastStepKey(flowName)] = stepName }
    }

    suspend fun setFinished(flowName: String, finished: Boolean) {
        dataStore.edit { it[finishedKey(flowName)] = finished }
    }
}
