package com.example.myapplication.ui.settings

enum class ColorPickerType {
    BACKGROUND,
    ACTIVE_SEGMENT,
    INACTIVE_SEGMENT
}

data class SettingsUiState(
    val is24HourFormat: Boolean = false,
    val tempUnit: String = "C",
    val colorBg: Int = 0xFF050505.toInt(),
    val colorActive: Int = 0xFF78AB78.toInt(),
    val colorInactive: Int = 0xFF0C0F0C.toInt(),
    val activeColorPickerType: ColorPickerType? = null,
    val showActivationInstructions: Boolean = false
)
