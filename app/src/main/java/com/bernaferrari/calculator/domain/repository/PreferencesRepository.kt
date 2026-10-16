package com.bernaferrari.calculator.domain.repository

import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.ThemeMode
import com.bernaferrari.calculator.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>

    suspend fun updateThemeMode(themeMode: ThemeMode)

    suspend fun updateDynamicColor(enabled: Boolean)

    suspend fun updateAngleUnit(angleUnit: AngleUnit)

    suspend fun updateDecimalPlaces(decimalPlaces: Int)

    suspend fun updateHapticFeedback(enabled: Boolean)

    suspend fun updateShowHistory(enabled: Boolean)

    suspend fun updateCommaDecimalSeparator(enabled: Boolean)

    suspend fun updateSoundEnabled(enabled: Boolean)

    suspend fun updateReduceMotion(enabled: Boolean)

    suspend fun updateHighContrast(enabled: Boolean)

    suspend fun markSwipeHintSeen()
}
