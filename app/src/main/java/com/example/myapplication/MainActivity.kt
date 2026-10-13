package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myapplication.ui.settings.SettingsScreenRoute
import com.example.myapplication.ui.settings.SettingsViewModel
import com.example.myapplication.ui.theme.AppTheme
import kotlinx.coroutines.*

/**
 * MainActivity serves as the primary entry point for the Bedside Clock.
 * It operates in two modes: STANDBY (full-screen clock) and SETTINGS (configuration).
 */
class MainActivity : AppCompatActivity() {

    private enum class Mode { STANDBY, SETTINGS }
    private var currentMode = Mode.STANDBY

    private lateinit var rootLayout: FrameLayout
    private lateinit var fullScreenClock: LcdClockView
    private lateinit var settingsComposeView: ComposeView

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(this)
    }

    // --- Clock Tick Logic ---
    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (currentMode == Mode.STANDBY) {
                fullScreenClock.updateTime()
            }
            handler.postDelayed(this, 1000)
        }
    }

    // --- Weather Integration ---
    private val weatherRepository by lazy { WeatherRepository(this) }
    private val weatherScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val weatherRunnable = object : Runnable {
        override fun run() {
            fetchWeather()
            handler.postDelayed(this, 5 * 60 * 1000) // Refresh every 5 minutes
        }
    }

    // --- Navigation Logic ---
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (currentMode == Mode.STANDBY) {
                showSettingsMode()
            } else {
                // Exit app by disabling callback and re-triggering back dispatcher
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bedside Clock: Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        // 1. Initialize View Hierarchy
        fullScreenClock = LcdClockView(this)
        
        settingsComposeView = ComposeView(this).apply {
            visibility = View.GONE
            setContent {
                AppTheme {
                    SettingsScreenRoute(
                        viewModel = settingsViewModel,
                        onEnterStandby = { showStandbyMode() },
                        onOpenScreenSaverSettings = { openScreenSaverSettings() }
                    )
                }
            }
        }

        rootLayout = FrameLayout(this)
        rootLayout.addView(fullScreenClock)
        rootLayout.addView(settingsComposeView)
        setContentView(rootLayout)

        // 2. Setup Back Handling
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        // 3. Enter Initial State (StandBy Mode by default)
        showStandbyMode()
        
        // 4. Start Services
        checkLocationPermissions()
    }

    private fun showStandbyMode() {
        currentMode = Mode.STANDBY
        fullScreenClock.visibility = View.VISIBLE
        settingsComposeView.visibility = View.GONE
        
        // Force Landscape and Keep Screen On
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Apply immersive system UI safely
        applySystemUiVisibility()
        
        fullScreenClock.refreshColors()
        fullScreenClock.updateTime()
    }

    private fun showSettingsMode() {
        currentMode = Mode.SETTINGS
        fullScreenClock.visibility = View.GONE
        settingsComposeView.visibility = View.VISIBLE
        
        // Restore portrait/sensor orientation and let screen sleep
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Restore system UI safely
        applySystemUiVisibility()
    }

    private fun openScreenSaverSettings() {
        try {
            startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
        } catch (e: Exception) {
            try { startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) } catch (ex: Exception) {}
        }
    }

    private fun applySystemUiVisibility() {
        val decorView = window.decorView
        if (decorView.isAttachedToWindow) {
            configureSystemUi(decorView)
        } else {
            decorView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    configureSystemUi(v)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }

    private fun configureSystemUi(decorView: View) {
        val window = window ?: return
        val controller = WindowInsetsControllerCompat(window, decorView)
        
        if (currentMode == Mode.STANDBY) {
            // Immersive Fullscreen using AndroidX Compat APIs
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Restore System Bars
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1001)
        } else {
            startWeatherUpdates()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startWeatherUpdates()
        }
    }

    private fun startWeatherUpdates() {
        handler.removeCallbacks(weatherRunnable)
        handler.post(weatherRunnable)
    }

    private fun fetchWeather() {
        weatherScope.launch {
            val state = weatherRepository.fetchWeather()
            fullScreenClock.setWeather(state)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tickRunnable)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startWeatherUpdates()
        }
        applySystemUiVisibility()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tickRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(weatherRunnable)
        weatherScope.cancel()
    }
}
