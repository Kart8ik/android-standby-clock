package com.example.myapplication.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import com.example.myapplication.domain.model.ClockSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PreferencesDataSource(private val context: Context) {

    companion object {
        private const val PREF_NAME = "clock_settings"
        private const val KEY_24_HOUR_FORMAT = "24_hour_format"
        private const val KEY_TEMP_UNIT = "temp_unit"
        private const val KEY_COLOR_BG = "color_bg"
        private const val KEY_COLOR_ACTIVE = "color_active"
        private const val KEY_COLOR_INACTIVE = "color_inactive"

        private const val DEFAULT_COLOR_BG = 0xFF050505.toInt()
        private const val DEFAULT_COLOR_ACTIVE = 0xFF78AB78.toInt()
        private const val DEFAULT_COLOR_INACTIVE = 0xFF0C0F0C.toInt()
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getSettings(): ClockSettings {
        val is24Hour = if (prefs.contains(KEY_24_HOUR_FORMAT)) {
            prefs.getBoolean(KEY_24_HOUR_FORMAT, false)
        } else {
            DateFormat.is24HourFormat(context)
        }
        val tempUnit = prefs.getString(KEY_TEMP_UNIT, "C") ?: "C"
        val colorBg = prefs.getInt(KEY_COLOR_BG, DEFAULT_COLOR_BG)
        val colorActive = prefs.getInt(KEY_COLOR_ACTIVE, DEFAULT_COLOR_ACTIVE)
        val colorInactive = prefs.getInt(KEY_COLOR_INACTIVE, DEFAULT_COLOR_INACTIVE)

        return ClockSettings(
            is24HourFormat = is24Hour,
            tempUnit = tempUnit,
            colorBg = colorBg,
            colorActive = colorActive,
            colorInactive = colorInactive
        )
    }

    val settingsFlow: Flow<ClockSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getSettings())
        }
        // Emit initial value
        trySend(getSettings())
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun set24HourFormat(use24Hour: Boolean) {
        prefs.edit().putBoolean(KEY_24_HOUR_FORMAT, use24Hour).apply()
    }

    fun setTempUnit(unit: String) {
        prefs.edit().putString(KEY_TEMP_UNIT, unit).apply()
    }

    fun setColorBg(color: Int) {
        prefs.edit().putInt(KEY_COLOR_BG, color).apply()
    }

    fun setColorActive(color: Int) {
        prefs.edit().putInt(KEY_COLOR_ACTIVE, color).apply()
    }

    fun setColorInactive(color: Int) {
        prefs.edit().putInt(KEY_COLOR_INACTIVE, color).apply()
    }
}
