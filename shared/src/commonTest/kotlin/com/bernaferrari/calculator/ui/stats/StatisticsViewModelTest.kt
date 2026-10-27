package com.bernaferrari.calculator.ui.stats

import com.bernaferrari.calculator.domain.model.*
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun deletingAnEntryReopensThePreviousValueOnlyOnTheNextPress() {
        val prefs = MemoryPreferences(listOf("12", "34"))
        val vm = StatisticsViewModel(prefs)
        vm.onDigit('5')
        vm.onDelete()
        assertEquals(StatsState(listOf("12", "34"), ""), vm.state.value)
        vm.onDelete()
        assertEquals(StatsState(listOf("12"), "34"), vm.state.value)
        assertEquals(listOf("12"), prefs.state.value.statsData)
        vm.onDelete()
        assertEquals("3", vm.state.value.entry)
        assertEquals(listOf("12"), vm.state.value.values)
    }

    @Test fun reopenedValuesCanBeCorrectedAndAddedWithoutDuplicates() {
        val prefs = MemoryPreferences(listOf("12", "−34.5"))
        val vm = StatisticsViewModel(prefs)
        vm.onDelete()
        assertEquals("−34.5", vm.state.value.entry)
        vm.onDelete()
        vm.onDigit('6')
        assertTrue(vm.onAdd())
        assertEquals(StatsState(listOf("12", "−34.6"), ""), vm.state.value)
        assertEquals(vm.state.value.values, prefs.state.value.statsData)
    }

    @Test fun repeatedBackspaceWalksThroughTheListAndStopsAtEmpty() {
        val vm = StatisticsViewModel(MemoryPreferences(listOf("1", "2")))
        repeat(6) { vm.onDelete() }
        assertEquals(StatsState(), vm.state.value)
    }
}

private class MemoryPreferences(values: List<String>) : PreferencesRepository {
    val state = MutableStateFlow(UserPreferences(statsData = values))
    override fun getUserPreferences() = state
    override suspend fun update(change: (UserPreferences) -> UserPreferences) { state.value = change(state.value) }
    override suspend fun updateThemeMode(themeMode: ThemeMode) = update { it.copy(themeMode = themeMode) }
    override suspend fun updatePalette(palette: AppPalette) = update { it.copy(palette = palette) }
    override suspend fun updateColorStyle(style: ColorStyle) = update { it.copy(colorStyle = style) }
    override suspend fun updateAngleUnit(angleUnit: AngleUnit) = update { it.copy(angleUnit = angleUnit) }
    override suspend fun updateDecimalPlaces(decimalPlaces: Int) = update { it.copy(decimalPlaces = decimalPlaces) }
    override suspend fun updateHapticFeedback(enabled: Boolean) = update { it.copy(hapticFeedback = enabled) }
    override suspend fun updateShowHistory(enabled: Boolean) = update { it.copy(showHistory = enabled) }
    override suspend fun updateDecimalSeparator(separator: DecimalSeparator) = update { it.copy(decimalSeparator = separator) }
    override suspend fun updateFlickHints(hints: FlickHints?) = update { it.copy(flickHints = hints) }
    override suspend fun updateSoundEnabled(enabled: Boolean) = update { it.copy(soundEnabled = enabled) }
    override suspend fun updateReduceMotion(enabled: Boolean) = update { it.copy(reduceMotion = enabled) }
    override suspend fun markSwipeHintSeen() = update { it.copy(swipeHintSeen = true) }
    override suspend fun updateKeypadScale(scale: Float) = update { it.copy(keypadScale = scale) }
    override suspend fun updateGraphKeypadScale(scale: Float) = update { it.copy(graphKeypadScale = scale) }
}
