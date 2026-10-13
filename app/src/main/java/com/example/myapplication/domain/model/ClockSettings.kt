package com.example.myapplication.domain.model

data class ClockSettings(
    val is24HourFormat: Boolean = false,
    val tempUnit: String = "C",
    val colorBg: Int = 0xFF050505.toInt(),
    val colorActive: Int = 0xFF78AB78.toInt(),
    val colorInactive: Int = 0xFF0C0F0C.toInt()
)
