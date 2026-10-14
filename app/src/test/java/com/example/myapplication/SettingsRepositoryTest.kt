package com.example.myapplication

import com.example.myapplication.domain.model.ClockSettings
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.ui.settings.ColorPickerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var repository: FakeSettingsRepository

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
    }

    @Test
    fun defaultSettings_hasDefaultValues() {
        runBlocking {
            val settings = repository.settings.first()
            assertFalse(settings.is24HourFormat)
            assertEquals("C", settings.tempUnit)
            assertTrue(settings.recentColorsBg.isEmpty())
            assertTrue(settings.recentColorsActive.isEmpty())
            assertTrue(settings.recentColorsInactive.isEmpty())
        }
    }

    @Test
    fun set24HourFormat_updatesSettings() {
        runBlocking {
            repository.set24HourFormat(true)
            val settings = repository.settings.first()
            assertTrue(settings.is24HourFormat)
        }
    }

    @Test
    fun setTempUnit_updatesSettings() {
        runBlocking {
            repository.setTempUnit("F")
            val settings = repository.settings.first()
            assertEquals("F", settings.tempUnit)
        }
    }

    @Test
    fun setColorBg_updatesSettings() {
        runBlocking {
            val newColor = 0xFF123456.toInt()
            repository.setColorBg(newColor)
            val settings = repository.settings.first()
            assertEquals(newColor, settings.colorBg)
        }
    }

    @Test
    fun addRecentColor_updatesCategorySpecificQueueOnly() {
        runBlocking {
            repository.addRecentColor(ColorPickerType.BACKGROUND, 100)
            repository.addRecentColor(ColorPickerType.ACTIVE_SEGMENT, 200)
            repository.addRecentColor(ColorPickerType.INACTIVE_SEGMENT, 300)

            val settings = repository.settings.first()
            assertEquals(listOf(100), settings.recentColorsBg)
            assertEquals(listOf(200), settings.recentColorsActive)
            assertEquals(listOf(300), settings.recentColorsInactive)
        }
    }

    @Test
    fun addRecentColor_allowsSameColorInMultipleCategories() {
        runBlocking {
            val sharedColor = 0xFF445566.toInt()
            repository.addRecentColor(ColorPickerType.BACKGROUND, sharedColor)
            repository.addRecentColor(ColorPickerType.ACTIVE_SEGMENT, sharedColor)

            val settings = repository.settings.first()
            assertEquals(listOf(sharedColor), settings.recentColorsBg)
            assertEquals(listOf(sharedColor), settings.recentColorsActive)
        }
    }

    @Test
    fun addRecentColor_deduplicatesAndLimitsToMaxFivePerCategory() {
        runBlocking {
            for (i in 1..6) {
                repository.addRecentColor(ColorPickerType.BACKGROUND, i)
            }
            var settings = repository.settings.first()
            assertEquals(listOf(6, 5, 4, 3, 2), settings.recentColorsBg)

            // Deduplicate within same category
            repository.addRecentColor(ColorPickerType.BACKGROUND, 4)
            settings = repository.settings.first()
            assertEquals(listOf(4, 6, 5, 3, 2), settings.recentColorsBg)
        }
    }

    class FakeSettingsRepository : SettingsRepository {
        private val _settings = MutableStateFlow(ClockSettings())
        override val settings: Flow<ClockSettings> = _settings
        override fun getInitialSettings(): ClockSettings = _settings.value

        override suspend fun set24HourFormat(use24Hour: Boolean) {
            _settings.value = _settings.value.copy(is24HourFormat = use24Hour)
        }

        override suspend fun setTempUnit(unit: String) {
            _settings.value = _settings.value.copy(tempUnit = unit)
        }

        override suspend fun setColorBg(color: Int) {
            _settings.value = _settings.value.copy(colorBg = color)
        }

        override suspend fun setColorActive(color: Int) {
            _settings.value = _settings.value.copy(colorActive = color)
        }

        override suspend fun setColorInactive(color: Int) {
            _settings.value = _settings.value.copy(colorInactive = color)
        }

        override suspend fun addRecentColor(type: ColorPickerType, color: Int) {
            val list = _settings.value.getRecentColors(type).toMutableList()
            list.remove(color)
            list.add(0, color)
            val updated = list.take(5)
            _settings.value = when (type) {
                ColorPickerType.BACKGROUND -> _settings.value.copy(recentColorsBg = updated)
                ColorPickerType.ACTIVE_SEGMENT -> _settings.value.copy(recentColorsActive = updated)
                ColorPickerType.INACTIVE_SEGMENT -> _settings.value.copy(recentColorsInactive = updated)
            }
        }
    }
}
