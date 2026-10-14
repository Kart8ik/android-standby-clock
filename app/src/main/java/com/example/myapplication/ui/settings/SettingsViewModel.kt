package com.example.myapplication.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.preferences.PreferencesDataSource
import com.example.myapplication.data.repository.SettingsRepositoryImpl
import com.example.myapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val initialSettings = settingsRepository.getInitialSettings()

    private val _activeColorPicker = MutableStateFlow<ColorPickerType?>(null)
    private val _showActivationInstructions = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        _activeColorPicker,
        _showActivationInstructions
    ) { settings, colorPicker, showActivation ->
        SettingsUiState(
            is24HourFormat = settings.is24HourFormat,
            tempUnit = settings.tempUnit,
            colorBg = settings.colorBg,
            colorActive = settings.colorActive,
            colorInactive = settings.colorInactive,
            activeColorPickerType = colorPicker,
            showActivationInstructions = showActivation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(
            is24HourFormat = initialSettings.is24HourFormat,
            tempUnit = initialSettings.tempUnit,
            colorBg = initialSettings.colorBg,
            colorActive = initialSettings.colorActive,
            colorInactive = initialSettings.colorInactive
        )
    )

    fun on24HourFormatChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.set24HourFormat(enabled)
        }
    }

    fun onTempUnitChanged(isFahrenheit: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTempUnit(if (isFahrenheit) "F" else "C")
        }
    }

    fun openColorPicker(type: ColorPickerType) {
        _activeColorPicker.value = type
    }

    fun dismissColorPicker() {
        _activeColorPicker.value = null
    }

    fun showActivationInstructions() {
        _showActivationInstructions.value = true
    }

    fun dismissActivationInstructions() {
        _showActivationInstructions.value = false
    }

    fun onColorSelected(type: ColorPickerType, color: Int) {
        viewModelScope.launch {
            when (type) {
                ColorPickerType.BACKGROUND -> settingsRepository.setColorBg(color)
                ColorPickerType.ACTIVE_SEGMENT -> settingsRepository.setColorActive(color)
                ColorPickerType.INACTIVE_SEGMENT -> settingsRepository.setColorInactive(color)
            }
        }
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dataSource = PreferencesDataSource(context.applicationContext)
                val repository = SettingsRepositoryImpl(dataSource)
                return SettingsViewModel(repository) as T
            }
        }
    }
}
