package com.bernaferrari.calculator.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.bernaferrari.calculator.domain.model.ThemeMode
import com.bernaferrari.calculator.domain.model.UserPreferences
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.AUTO_DECIMALS
import com.bernaferrari.calculator.domain.model.DecimalSeparator
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calculator_preferences")

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ANGLE_UNIT = stringPreferencesKey("angle_unit")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SHOW_HISTORY = booleanPreferencesKey("show_history")
        val DECIMAL_SEPARATOR = stringPreferencesKey("decimal_separator")
        val SOUND_ENABLED = booleanPreferencesKey("key_sounds")
        val SHOW_FLICK_DOTS = booleanPreferencesKey("show_flick_dots")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val SWIPE_HINT_SEEN = booleanPreferencesKey("swipe_hint_seen")
    }

    override fun getUserPreferences(): Flow<UserPreferences> {
        return context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                UserPreferences(
                    // MATERIAL_YOU used to mean "system + wallpaper colors"; it now reads as SYSTEM with dynamicColor.
                    themeMode = ThemeMode.valueOf(preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
                        .let { if (it == ThemeMode.MATERIAL_YOU) ThemeMode.SYSTEM else it },
                    dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
                    angleUnit = AngleUnit.valueOf(
                        preferences[PreferencesKeys.ANGLE_UNIT] ?: AngleUnit.DEGREES.name
                    ),
                    decimalPlaces = preferences[PreferencesKeys.DECIMAL_PLACES] ?: AUTO_DECIMALS,
                    hapticFeedback = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true,
                    showHistory = preferences[PreferencesKeys.SHOW_HISTORY] ?: true,
                    decimalSeparator = preferences[PreferencesKeys.DECIMAL_SEPARATOR]
                        ?.let { runCatching { DecimalSeparator.valueOf(it) }.getOrNull() } ?: DecimalSeparator.AUTO,
                    soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: false,
                    showFlickDots = preferences[PreferencesKeys.SHOW_FLICK_DOTS] ?: true,
                    reduceMotion = preferences[PreferencesKeys.REDUCE_MOTION] ?: false,
                    highContrast = preferences[PreferencesKeys.HIGH_CONTRAST] ?: false,
                    swipeHintSeen = preferences[PreferencesKeys.SWIPE_HINT_SEEN] ?: false,
                )
            }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    override suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    override suspend fun updateAngleUnit(angleUnit: AngleUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANGLE_UNIT] = angleUnit.name
        }
    }

    override suspend fun updateDecimalPlaces(decimalPlaces: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DECIMAL_PLACES] = decimalPlaces
        }
    }

    override suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabled
        }
    }

    override suspend fun updateShowHistory(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_HISTORY] = enabled
        }
    }

    override suspend fun updateDecimalSeparator(separator: DecimalSeparator) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DECIMAL_SEPARATOR] = separator.name
        }
    }

    override suspend fun updateShowFlickDots(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_FLICK_DOTS] = enabled
        }
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    override suspend fun updateReduceMotion(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REDUCE_MOTION] = enabled
        }
    }

    override suspend fun updateHighContrast(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_CONTRAST] = enabled
        }
    }

    override suspend fun markSwipeHintSeen() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SWIPE_HINT_SEEN] = true
        }
    }
}
