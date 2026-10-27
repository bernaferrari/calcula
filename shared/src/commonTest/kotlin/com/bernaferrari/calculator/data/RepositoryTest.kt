package com.bernaferrari.calculator.data

import com.bernaferrari.calculator.platform.StringStore
import com.bernaferrari.calculator.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MemoryStringStore : StringStore {
    private val values = mutableMapOf<String, String>()
    override fun get(key: String) = values[key]
    override fun put(key: String, value: String?) { if (value == null) values.remove(key) else values[key] = value }
}

class RepositoryTest {
    @Test fun deleteThenCalculateThenUndoKeepsBothEntries() = runTest {
        val store = MemoryStringStore()
        val repository = StoredHistoryRepository(store)
        repository.insertCalculation(CalculationHistory(expression = "1+1", result = "2"))
        val deleted = repository.getAllHistory().first().single()
        repository.deleteCalculation(deleted)
        // Recreate the repository too: the allocator must survive a fresh launch.
        val restored = StoredHistoryRepository(store)
        restored.insertCalculation(CalculationHistory(expression = "2+2", result = "4"))
        restored.insertCalculation(deleted)
        assertEquals(setOf("2", "4"), restored.getAllHistory().first().map { it.result }.toSet())
        assertEquals(2, StoredHistoryRepository(store).getHistoryCount())
    }

    @Test fun clearThenCalculateThenRestoreKeepsAllEntries() = runTest {
        val repository = StoredHistoryRepository(MemoryStringStore())
        repeat(3) { repository.insertCalculation(CalculationHistory(expression = "$it", result = "$it")) }
        val deleted = repository.getAllHistory().first()
        repository.clearAllHistory()
        repository.insertCalculation(CalculationHistory(expression = "9", result = "9"))
        deleted.forEach { repository.insertCalculation(it) }
        assertEquals(4, repository.getHistoryCount())
    }

    @Test fun preferencesRoundTripAndUpdatesPreserveOtherSettings() = runTest {
        val store = MemoryStringStore()
        val preferences = StoredPreferencesRepository(store)
        preferences.updateAngleUnit(AngleUnit.RADIANS)
        preferences.updateDecimalPlaces(3)
        preferences.updateReduceMotion(true)
        val loaded = StoredPreferencesRepository(store).getUserPreferences().first()
        assertEquals(AngleUnit.RADIANS, loaded.angleUnit)
        assertEquals(3, loaded.decimalPlaces)
        assertTrue(loaded.reduceMotion)
    }

    @Test fun legacyGraphitePreferenceMigratesToNeutralWithoutLosingSettings() = runTest {
        val store = MemoryStringStore()
        store.put("preferences", """{"palette":"GRAPHITE","colorStyle":"VIBRANT","decimalPlaces":3}""")
        val preferences = StoredPreferencesRepository(store)
        val migrated = preferences.getUserPreferences().first()
        assertEquals(AppPalette.INDIGO, migrated.palette)
        assertEquals(ColorStyle.NEUTRAL, migrated.colorStyle)
        assertEquals(3, migrated.decimalPlaces)
        preferences.updateReduceMotion(true)
        assertEquals(migrated.copy(reduceMotion = true), StoredPreferencesRepository(store).getUserPreferences().first())
    }
}
