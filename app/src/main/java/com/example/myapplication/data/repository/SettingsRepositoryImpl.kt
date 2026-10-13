package com.example.myapplication.data.repository

import com.example.myapplication.data.preferences.PreferencesDataSource
import com.example.myapplication.domain.model.ClockSettings
import com.example.myapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val dataSource: PreferencesDataSource
) : SettingsRepository {

    override val settings: Flow<ClockSettings> = dataSource.settingsFlow

    override suspend fun set24HourFormat(use24Hour: Boolean) {
        dataSource.set24HourFormat(use24Hour)
    }

    override suspend fun setTempUnit(unit: String) {
        dataSource.setTempUnit(unit)
    }

    override suspend fun setColorBg(color: Int) {
        dataSource.setColorBg(color)
    }

    override suspend fun setColorActive(color: Int) {
        dataSource.setColorActive(color)
    }

    override suspend fun setColorInactive(color: Int) {
        dataSource.setColorInactive(color)
    }
}
