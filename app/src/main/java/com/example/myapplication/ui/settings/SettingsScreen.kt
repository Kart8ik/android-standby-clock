package com.example.myapplication.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.AppSettings
import com.example.myapplication.LcdClockView
import com.example.myapplication.R
import com.example.myapplication.ui.settings.components.ActivationInstructionsBottomSheet
import com.example.myapplication.ui.settings.components.ActivationSettingRow
import com.example.myapplication.ui.settings.components.ColorPickerDialog
import com.example.myapplication.ui.settings.components.ColorSettingRow
import com.example.myapplication.ui.settings.components.SettingsSwitchRow

@Composable
fun SettingsScreenRoute(
    viewModel: SettingsViewModel,
    onEnterStandby: () -> Unit,
    onOpenScreenSaverSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        on24HourFormatChanged = viewModel::on24HourFormatChanged,
        onTempUnitChanged = viewModel::onTempUnitChanged,
        onOpenColorPicker = viewModel::openColorPicker,
        onColorSelected = viewModel::onColorSelected,
        onDismissColorPicker = viewModel::dismissColorPicker,
        onShowActivationInstructions = viewModel::showActivationInstructions,
        onDismissActivationInstructions = viewModel::dismissActivationInstructions,
        onEnterStandby = onEnterStandby,
        onOpenScreenSaverSettings = onOpenScreenSaverSettings,
        modifier = modifier
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    on24HourFormatChanged: (Boolean) -> Unit,
    onTempUnitChanged: (Boolean) -> Unit,
    onOpenColorPicker: (ColorPickerType) -> Unit,
    onColorSelected: (ColorPickerType, Int) -> Unit,
    onDismissColorPicker: () -> Unit,
    onShowActivationInstructions: () -> Unit,
    onDismissActivationInstructions: () -> Unit,
    onEnterStandby: () -> Unit,
    onOpenScreenSaverSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {

            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(bottom = 16.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        LcdClockView(context).apply {
                            updateTime()
                        }
                    },
                    update = { clockView ->
                        clockView.setOverrideColors(
                            bg = uiState.colorBg,
                            active = uiState.colorActive,
                            inactive = uiState.colorInactive
                        )
                        clockView.updateTime()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Settings Menu Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SettingsSwitchRow(
                        title = stringResource(R.string.use_24_hr_format),
                        checked = uiState.is24HourFormat,
                        onCheckedChange = on24HourFormatChanged
                    )

                    SettingsSwitchRow(
                        title = if (uiState.tempUnit == "C") stringResource(R.string.temperature_unit_c) else stringResource(R.string.temperature_unit_f),
                        checked = uiState.tempUnit == "F",
                        onCheckedChange = onTempUnitChanged
                    )

                    ColorSettingRow(
                        title = stringResource(R.string.background_color),
                        color = Color(uiState.colorBg),
                        onClick = { onOpenColorPicker(ColorPickerType.BACKGROUND) }
                    )

                    ColorSettingRow(
                        title = stringResource(R.string.active_segment_color),
                        color = Color(uiState.colorActive),
                        onClick = { onOpenColorPicker(ColorPickerType.ACTIVE_SEGMENT) }
                    )

                    ColorSettingRow(
                        title = stringResource(R.string.inactive_segment_color),
                        color = Color(uiState.colorInactive),
                        onClick = { onOpenColorPicker(ColorPickerType.INACTIVE_SEGMENT) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    ActivationSettingRow(
                        title = stringResource(R.string.how_to_activate_title),
                        subtitle = stringResource(R.string.how_to_activate_desc),
                        onClick = onShowActivationInstructions
                    )
                }
            }

            Button(
                onClick = onEnterStandby,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.enter_standby_mode),
                    style = MaterialTheme.typography.titleMedium
                )
            }

        }
    }

    // Active Color Picker Dialog
    uiState.activeColorPickerType?.let { pickerType ->
        val (title, currentColor) = when (pickerType) {
            ColorPickerType.BACKGROUND -> Pair(stringResource(R.string.background_color), uiState.colorBg)
            ColorPickerType.ACTIVE_SEGMENT -> Pair(stringResource(R.string.active_segment_color), uiState.colorActive)
            ColorPickerType.INACTIVE_SEGMENT -> Pair(stringResource(R.string.inactive_segment_color), uiState.colorInactive)
        }

        ColorPickerDialog(
            title = title,
            initialColor = currentColor,
            pickerType = pickerType,
            recentColors = uiState.getRecentColorsFor(pickerType),
            onColorSelected = { color -> onColorSelected(pickerType, color) },
            onDismiss = onDismissColorPicker
        )
    }

    // Activation Instructions Bottom Sheet
    if (uiState.showActivationInstructions) {
        ActivationInstructionsBottomSheet(
            onOpenScreenSaverSettings = onOpenScreenSaverSettings,
            onDismiss = onDismissActivationInstructions
        )
    }
}
