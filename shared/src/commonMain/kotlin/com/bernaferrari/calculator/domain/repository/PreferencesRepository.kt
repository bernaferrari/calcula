package com.bernaferrari.calculator.domain.repository

import com.bernaferrari.calculator.domain.model.ColorStyle
import com.bernaferrari.calculator.domain.model.FlickHints
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.domain.model.DecimalSeparator
import com.bernaferrari.calculator.domain.model.ThemeMode
import com.bernaferrari.calculator.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>

    suspend fun updateThemeMode(themeMode: ThemeMode)

    suspend fun updatePalette(palette: AppPalette)
    suspend fun updateColorStyle(style: ColorStyle)

    suspend fun updateAngleUnit(angleUnit: AngleUnit)

    suspend fun updateDecimalPlaces(decimalPlaces: Int)

    suspend fun updateHapticFeedback(enabled: Boolean)

    suspend fun updateShowHistory(enabled: Boolean)

    suspend fun updateDecimalSeparator(separator: DecimalSeparator)

    /** A hint style for good, or null for automatic (names first, dots once the flicks are known). */
    suspend fun updateFlickHints(hints: FlickHints?)
    /** One more function picked by flick; only counts while the hint style is still automatic. */
    suspend fun countFlick()

    suspend fun updateSoundEnabled(enabled: Boolean)

    suspend fun updateReduceMotion(enabled: Boolean)


    suspend fun markSwipeHintSeen()

    suspend fun updateKeypadScale(scale: Float)
    suspend fun updateGraphKeypadScale(scale: Float)
}
