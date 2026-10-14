package com.example.myapplication.ui.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.ColorWheelView
import com.example.myapplication.LcdClockView
import com.example.myapplication.ui.settings.ColorPickerType

@Composable
fun ColorPickerDialog(
    title: String,
    initialColor: Int,
    pickerType: ColorPickerType,
    recentColors: List<Int> = emptyList(),
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var currentColor by remember(initialColor, pickerType) { mutableIntStateOf(initialColor) }
    val themePrimaryColor = MaterialTheme.colorScheme.primary.toArgb()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Prominent Real-time Clock Preview Container
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(20f / 9f, matchHeightConstraintsFirst = true)
                ) {
                    AndroidView(
                        factory = { context ->
                            LcdClockView(context).apply {
                                setWeather(LcdClockView.PREVIEW_MOCK_WEATHER)
                                updateTime()
                            }
                        },
                        update = { clockView ->
                            if (clockView.getWeatherState() == null) {
                                clockView.setWeather(LcdClockView.PREVIEW_MOCK_WEATHER)
                            }
                            when (pickerType) {
                                ColorPickerType.BACKGROUND -> clockView.setOverrideColors(bg = currentColor)
                                ColorPickerType.ACTIVE_SEGMENT -> clockView.setOverrideColors(active = currentColor)
                                ColorPickerType.INACTIVE_SEGMENT -> clockView.setOverrideColors(inactive = currentColor)
                            }
                            clockView.updateTime()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Swatch + Hex value indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(currentColor),
                        modifier = Modifier.size(32.dp)
                    ) {}
                    Text(
                        text = String.format("#%06X", 0xFFFFFF and currentColor),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Recent Colors Section (5 slots)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Recent Colors",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            if (i < recentColors.size) {
                                val recentColor = recentColors[i]
                                Surface(
                                    onClick = { currentColor = recentColor },
                                    shape = CircleShape,
                                    color = Color(recentColor),
                                    modifier = Modifier.size(36.dp)
                                ) {}
                            } else {
                                // Empty Placeholder Slot (not clickable)
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = BorderStroke(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {}
                            }
                        }
                    }
                }

                // Interactive Color Wheel
                AndroidView(
                    factory = { context ->
                        ColorWheelView(context).apply {
                            primaryColor = themePrimaryColor
                            selectedColor = currentColor
                            onColorChanged = { newColor ->
                                currentColor = newColor
                            }
                        }
                    },
                    update = { colorWheel ->
                        colorWheel.primaryColor = themePrimaryColor
                        if (colorWheel.selectedColor != currentColor) {
                            colorWheel.selectedColor = currentColor
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                // Polished Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onColorSelected(currentColor)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
