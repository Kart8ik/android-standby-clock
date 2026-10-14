package com.example.myapplication.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import com.example.myapplication.domain.model.ClockSettings
import com.example.myapplication.ui.settings.ColorPickerType
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

        private const val KEY_RECENT_COLORS_BG = "recent_colors_background"
        private const val KEY_RECENT_COLORS_ACTIVE = "recent_colors_active"
        private const val KEY_RECENT_COLORS_INACTIVE = "recent_colors_inactive"
        private const val KEY_RECENT_COLORS_LEGACY = "recent_colors"

        private const val DEFAULT_COLOR_BG = 0xFF050505.toInt()
        private const val DEFAULT_COLOR_ACTIVE = 0xFF78AB78.toInt()
        private const val DEFAULT_COLOR_INACTIVE = 0xFF0C0F0C.toInt()
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun checkAndMigrateLegacyRecentColors() {
        if (!prefs.contains(KEY_RECENT_COLORS_BG) && prefs.contains(KEY_RECENT_COLORS_LEGACY)) {
            val legacyRaw = prefs.getString(KEY_RECENT_COLORS_LEGACY, null)
            val editor = prefs.edit()
            if (!legacyRaw.isNullOrBlank()) {
                val legacyColors = legacyRaw.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .take(5)
                if (legacyColors.isNotEmpty()) {
                    editor.putString(KEY_RECENT_COLORS_BG, legacyColors.joinToString(","))
                }
            }
            editor.remove(KEY_RECENT_COLORS_LEGACY)
            editor.apply()
        }
    }

    private fun getKeyForType(type: ColorPickerType): String = when (type) {
        ColorPickerType.BACKGROUND -> KEY_RECENT_COLORS_BG
        ColorPickerType.ACTIVE_SEGMENT -> KEY_RECENT_COLORS_ACTIVE
        ColorPickerType.INACTIVE_SEGMENT -> KEY_RECENT_COLORS_INACTIVE
    }

    fun getRecentColors(type: ColorPickerType): List<Int> {
        checkAndMigrateLegacyRecentColors()
        val key = getKeyForType(type)
        val raw = prefs.getString(key, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .take(5)
    }

    fun addRecentColor(type: ColorPickerType, color: Int) {
        checkAndMigrateLegacyRecentColors()
        val key = getKeyForType(type)
        val current = getRecentColors(type).toMutableList()
        current.remove(color)
        current.add(0, color)
        val updated = current.take(5)
        val serialized = updated.joinToString(",")
        prefs.edit().putString(key, serialized).apply()
    }

    fun getSettings(): ClockSettings {
        checkAndMigrateLegacyRecentColors()
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
            colorInactive = colorInactive,
            recentColorsBg = getRecentColors(ColorPickerType.BACKGROUND),
            recentColorsActive = getRecentColors(ColorPickerType.ACTIVE_SEGMENT),
            recentColorsInactive = getRecentColors(ColorPickerType.INACTIVE_SEGMENT)
        )
    }

    fun getInitialSettings(): ClockSettings = getSettings()

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
