package com.example.myapplication.domain.model

import com.example.myapplication.ui.settings.ColorPickerType

data class ClockSettings(
    val is24HourFormat: Boolean = false,
    val tempUnit: String = "C",
    val colorBg: Int = 0xFF050505.toInt(),
    val colorActive: Int = 0xFF78AB78.toInt(),
    val colorInactive: Int = 0xFF0C0F0C.toInt(),
    val recentColorsBg: List<Int> = emptyList(),
    val recentColorsActive: List<Int> = emptyList(),
    val recentColorsInactive: List<Int> = emptyList()
) {
    fun getRecentColors(type: ColorPickerType): List<Int> = when (type) {
        ColorPickerType.BACKGROUND -> recentColorsBg
        ColorPickerType.ACTIVE_SEGMENT -> recentColorsActive
        ColorPickerType.INACTIVE_SEGMENT -> recentColorsInactive
    }
}
