package com.bernaferrari.calculator.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.bernaferrari.calculator.domain.model.ThemeMode
import com.bernaferrari.calculator.domain.model.UserPreferences
import com.bernaferrari.calculator.domain.model.AngleUnit
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
        val ANGLE_UNIT = stringPreferencesKey("angle_unit")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SHOW_HISTORY = booleanPreferencesKey("show_history")
        val COMMA_DECIMAL_SEPARATOR = booleanPreferencesKey("comma_decimal_separator")
    }

    override fun getUserPreferences(): Flow<UserPreferences> {
        return context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                UserPreferences(
                    themeMode = ThemeMode.valueOf(
                        preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.MATERIAL_YOU.name
                    ),
                    angleUnit = AngleUnit.valueOf(
                        preferences[PreferencesKeys.ANGLE_UNIT] ?: AngleUnit.RADIANS.name
                    ),
                    decimalPlaces = preferences[PreferencesKeys.DECIMAL_PLACES] ?: 10,
                    hapticFeedback = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true,
                    showHistory = preferences[PreferencesKeys.SHOW_HISTORY] ?: true,
                    useCommaDecimalSeparator = preferences[PreferencesKeys.COMMA_DECIMAL_SEPARATOR] ?: false
                )
            }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
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

    override suspend fun updateCommaDecimalSeparator(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMMA_DECIMAL_SEPARATOR] = enabled
        }
    }
}