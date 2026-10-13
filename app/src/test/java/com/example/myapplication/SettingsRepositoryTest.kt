package com.example.myapplication

import com.example.myapplication.domain.model.ClockSettings
import com.example.myapplication.domain.repository.SettingsRepository
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

    class FakeSettingsRepository : SettingsRepository {
        private val _settings = MutableStateFlow(ClockSettings())
        override val settings: Flow<ClockSettings> = _settings

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
    }
}
