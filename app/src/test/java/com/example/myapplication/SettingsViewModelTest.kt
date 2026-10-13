package com.example.myapplication

import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.ui.settings.ColorPickerType
import com.example.myapplication.ui.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: SettingsRepositoryTest.FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = SettingsRepositoryTest.FakeSettingsRepository()
        viewModel = SettingsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun on24HourFormatChanged_updatesState() = runTest {
        viewModel.on24HourFormatChanged(true)
        testDispatcher.scheduler.advanceUntilIdle()
        val settings = fakeRepository.settings.first()
        assertTrue(settings.is24HourFormat)
    }

    @Test
    fun onTempUnitChanged_updatesState() = runTest {
        viewModel.onTempUnitChanged(true) // Fahrenheit
        testDispatcher.scheduler.advanceUntilIdle()
        val settings = fakeRepository.settings.first()
        assertEquals("F", settings.tempUnit)
    }

    @Test
    fun openAndDismissColorPicker_updatesUiState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        viewModel.openColorPicker(ColorPickerType.BACKGROUND)
        testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value
        assertEquals(ColorPickerType.BACKGROUND, state.activeColorPickerType)

        viewModel.dismissColorPicker()
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertNull(state.activeColorPickerType)
    }

    @Test
    fun onColorSelected_updatesRepositoryColor() = runTest {
        val newColor = 0xFF998877.toInt()
        viewModel.onColorSelected(ColorPickerType.ACTIVE_SEGMENT, newColor)
        testDispatcher.scheduler.advanceUntilIdle()
        val settings = fakeRepository.settings.first()
        assertEquals(newColor, settings.colorActive)
    }
}
