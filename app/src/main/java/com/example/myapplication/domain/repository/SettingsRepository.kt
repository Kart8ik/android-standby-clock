package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.ClockSettings
import com.example.myapplication.ui.settings.ColorPickerType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<ClockSettings>
    fun getInitialSettings(): ClockSettings
    
    suspend fun set24HourFormat(use24Hour: Boolean)
    suspend fun setTempUnit(unit: String)
    suspend fun setColorBg(color: Int)
    suspend fun setColorActive(color: Int)
    suspend fun setColorInactive(color: Int)
    suspend fun addRecentColor(type: ColorPickerType, color: Int)

    fun getStandbyHintLaunchCount(): Int
    suspend fun incrementStandbyHintLaunchCount(): Int
}
