package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.text.format.DateFormat

object AppSettings {
    private const val PREF_NAME = "clock_settings"
    private const val KEY_24_HOUR_FORMAT = "24_hour_format"
    private const val KEY_TEMP_UNIT = "temp_unit"
    private const val KEY_COLOR_BG = "color_bg"
    private const val KEY_COLOR_ACTIVE = "color_active"
    private const val KEY_COLOR_INACTIVE = "color_inactive"

    private const val DEFAULT_COLOR_BG = 0xFF050505.toInt()
    private const val DEFAULT_COLOR_ACTIVE = 0xFF78AB78.toInt()
    private const val DEFAULT_COLOR_INACTIVE = 0xFF0C0F0C.toInt()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun is24HourFormat(context: Context): Boolean {
        val prefs = getPrefs(context)
        return if (prefs.contains(KEY_24_HOUR_FORMAT)) {
            prefs.getBoolean(KEY_24_HOUR_FORMAT, false)
        } else {
            DateFormat.is24HourFormat(context)
        }
    }

    fun set24HourFormat(context: Context, use24Hour: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_24_HOUR_FORMAT, use24Hour).apply()
    }

    fun getTempUnit(context: Context): String {
        return getPrefs(context).getString(KEY_TEMP_UNIT, "C") ?: "C"
    }

    fun setTempUnit(context: Context, unit: String) {
        getPrefs(context).edit().putString(KEY_TEMP_UNIT, unit).apply()
    }

    fun getColorBg(context: Context): Int {
        return getPrefs(context).getInt(KEY_COLOR_BG, DEFAULT_COLOR_BG)
    }

    fun setColorBg(context: Context, color: Int) {
        getPrefs(context).edit().putInt(KEY_COLOR_BG, color).apply()
    }

    fun getColorActive(context: Context): Int {
        return getPrefs(context).getInt(KEY_COLOR_ACTIVE, DEFAULT_COLOR_ACTIVE)
    }

    fun setColorActive(context: Context, color: Int) {
        getPrefs(context).edit().putInt(KEY_COLOR_ACTIVE, color).apply()
    }

    fun getColorInactive(context: Context): Int {
        return getPrefs(context).getInt(KEY_COLOR_INACTIVE, DEFAULT_COLOR_INACTIVE)
    }

    fun setColorInactive(context: Context, color: Int) {
        getPrefs(context).edit().putInt(KEY_COLOR_INACTIVE, color).apply()
    }
}
