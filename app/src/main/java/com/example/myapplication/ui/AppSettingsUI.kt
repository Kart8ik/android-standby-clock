package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.AppTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource

@Composable
fun AppSettingsUI(modifier: Modifier) {
    Surface() {
        SettingsScreen(Modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier){
    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = stringResource(R.string.app_name),
            style = typography.headlineLarge,
            modifier = modifier.padding(top = 32.dp, bottom = 32.dp)
        )
        StandbyPreviewWindow(modifier)
        SettingsMenu(modifier)

        Column(modifier = modifier.padding(top = 16.dp, bottom = 16.dp)) {
            Button(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.enter_standby_mode),
                    style = typography.titleMedium
                )
            }

            Button(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.open_screen_saver_settings),
                    style = typography.titleMedium
                )
            }
        }

        Text(
            text = stringResource(R.string.instructions),
            style = typography.bodySmall,
            modifier = modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun StandbyPreviewWindow(modifier: Modifier) {
    Image(
        painterResource(id = R.drawable.preview_image_placeholder),
        contentDescription = stringResource(R.string.preview_desc),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(shapes.medium),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun SettingsMenu(modifier: Modifier){
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SettingsSwitchRow(stringResource(R.string.use_24_hr_format),modifier)
            SettingsSwitchRow(stringResource(R.string.temperature_unit_c),modifier)
            SettingsButtonRow(stringResource(R.string.background_color),modifier)
            SettingsButtonRow(stringResource(R.string.active_segment_color),modifier)
            SettingsButtonRow(stringResource(R.string.inactive_segment_color),modifier)
        }
    }
}

@Composable
fun SettingsSwitchRow(settingsName: String, modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = settingsName,
            style = typography.bodyMedium
        )

        Switch(
            checked = true,
            onCheckedChange = {}
        )
    }
}

@Composable
fun SettingsButtonRow(settingsName: String, modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = settingsName,
            style = typography.bodyMedium
        )
        Surface(
            onClick = {},
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color.Gray
        ) {}
    }
}

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme(darkTheme = false){
        AppSettingsUI(Modifier)
    }
}
