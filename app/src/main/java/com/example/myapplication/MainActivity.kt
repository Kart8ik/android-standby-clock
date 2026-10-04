package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    private lateinit var settingsScrollView: ScrollView
    private lateinit var previewClock: LcdClockView
    
    private var bgIndicator: View? = null
    private var activeIndicator: View? = null
    private var inactiveIndicator: View? = null

    // --- Clock Tick Logic ---
    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (currentMode == Mode.STANDBY) {
                fullScreenClock.updateTime()
            } else {
                previewClock.updateTime()
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
        setupSettingsUI() // Initializes settingsScrollView and previewClock

        rootLayout = FrameLayout(this)
        rootLayout.addView(fullScreenClock)
        rootLayout.addView(settingsScrollView)
        setContentView(rootLayout)

        // 2. Setup Back Handling
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        // 3. Enter Initial State
        // The app always opens directly into StandBy
        showStandbyMode()
        
        // 4. Start Services
        checkLocationPermissions()
    }

    private fun setupSettingsUI() {
        settingsScrollView = ScrollView(this).apply {
            visibility = View.GONE
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        settingsScrollView.addView(layout)

        val title = TextView(this).apply {
            text = "Bedside Clock"
            textSize = 28f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }

        val previewTitle = TextView(this).apply {
            text = "Preview"
            textSize = 14f
            setPadding(0, 20, 0, 10)
        }

        previewClock = LcdClockView(this).apply {
            val density = resources.displayMetrics.density
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (180 * density).toInt()
            )
        }

        val toggle24Hr = Switch(this).apply {
            text = "Use 24-hour format"
            isChecked = AppSettings.is24HourFormat(this@MainActivity)
            setOnCheckedChangeListener { _, isChecked ->
                AppSettings.set24HourFormat(this@MainActivity, isChecked)
                fullScreenClock.updateTime()
                previewClock.updateTime()
            }
            setPadding(0, 20, 0, 20)
        }

        val toggleUnit = Switch(this).apply {
            val currentUnit = AppSettings.getTempUnit(this@MainActivity)
            text = "Temperature Unit: ${if (currentUnit == "C") "Celsius (°C)" else "Fahrenheit (°F)"}"
            isChecked = currentUnit == "F"
            setOnCheckedChangeListener { _, isChecked ->
                val newUnit = if (isChecked) "F" else "C"
                AppSettings.setTempUnit(this@MainActivity, newUnit)
                text = "Temperature Unit: ${if (newUnit == "C") "Celsius (°C)" else "Fahrenheit (°F)"}"
                fullScreenClock.updateTime()
                previewClock.updateTime()
            }
            setPadding(0, 0, 0, 20)
        }

        layout.addView(title)
        layout.addView(previewTitle)
        layout.addView(previewClock)
        layout.addView(toggle24Hr)
        layout.addView(toggleUnit)

        addColorPickerRow(layout, "Background Color") {
            showColorPickerDialog("Background Color", AppSettings.getColorBg(this)) { color ->
                AppSettings.setColorBg(this, color)
            }
        }

        addColorPickerRow(layout, "Active Segment Color") {
            showColorPickerDialog("Active Segment Color", AppSettings.getColorActive(this)) { color ->
                AppSettings.setColorActive(this, color)
            }
        }

        addColorPickerRow(layout, "Inactive Segment Color") {
            showColorPickerDialog("Inactive Segment Color", AppSettings.getColorInactive(this)) { color ->
                AppSettings.setColorInactive(this, color)
            }
        }

        val standbyButton = Button(this).apply {
            text = "Enter StandBy"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 40, 0, 0)
            }
            setOnClickListener { showStandbyMode() }
        }

        val dreamSettingsButton = Button(this).apply {
            text = "Open Screen Saver Settings"
            setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                } catch (e: Exception) {
                    try { startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) } catch (ex: Exception) {}
                }
            }
        }

        val dreamHint = TextView(this).apply {
            text = "\nTo use as an automatic screensaver:\n1. Open Screen Saver settings\n2. Select 'Bedside Clock'"
            textSize = 12f
            setPadding(0, 40, 0, 40)
            gravity = Gravity.CENTER
        }

        layout.addView(standbyButton)
        layout.addView(dreamSettingsButton)
        layout.addView(dreamHint)
    }

    private fun showStandbyMode() {
        currentMode = Mode.STANDBY
        fullScreenClock.visibility = View.VISIBLE
        settingsScrollView.visibility = View.GONE
        
        // Force Landscape and Keep Screen On
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Apply immersive system UI safely
        applySystemUiVisibility()
        
        fullScreenClock.updateTime()
        updateAllUIs()
    }

    private fun showSettingsMode() {
        currentMode = Mode.SETTINGS
        fullScreenClock.visibility = View.GONE
        settingsScrollView.visibility = View.VISIBLE
        
        // Restore portrait/sensor orientation and let screen sleep
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Restore system UI safely
        applySystemUiVisibility()
        
        previewClock.updateTime()
        updateAllUIs()
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
            previewClock.setWeather(state)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tickRunnable)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startWeatherUpdates()
        }
        // Re-apply system UI state in case it was lost while backgrounded
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

    private fun addColorPickerRow(parent: LinearLayout, label: String, onClick: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 20, 0, 20)
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
            setOnClickListener { onClick() }
        }

        val text = TextView(this).apply {
            text = label
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val colorIndicator = View(this).apply {
            val size = (30 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size)
            val color = when (label) {
                "Background Color" -> AppSettings.getColorBg(this@MainActivity)
                "Active Segment Color" -> AppSettings.getColorActive(this@MainActivity)
                else -> AppSettings.getColorInactive(this@MainActivity)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                setStroke(3, Color.LTGRAY)
            }
            when (label) {
                "Background Color" -> bgIndicator = this
                "Active Segment Color" -> activeIndicator = this
                else -> inactiveIndicator = this
            }
        }
        row.addView(text)
        row.addView(colorIndicator)
        parent.addView(row)
    }

    private fun updateAllUIs() {
        fullScreenClock.refreshColors()
        previewClock.refreshColors()
        (bgIndicator?.background as? GradientDrawable)?.setColor(AppSettings.getColorBg(this))
        (activeIndicator?.background as? GradientDrawable)?.setColor(AppSettings.getColorActive(this))
        (inactiveIndicator?.background as? GradientDrawable)?.setColor(AppSettings.getColorInactive(this))
    }

    private fun showColorPickerDialog(title: String, currentColor: Int, onColorSelected: (Int) -> Unit) {
        val density = resources.displayMetrics.density
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24 * density).toInt(), (24 * density).toInt(), (24 * density).toInt(), (24 * density).toInt())
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val dialogPreview = LcdClockView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (120 * density).toInt()).apply {
                setMargins(0, 0, 0, (24 * density).toInt())
            }
        }
        dialogPreview.updateTime()
        dialogLayout.addView(dialogPreview)
        val colorWheel = ColorWheelView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (360 * density).toInt())
            selectedColor = currentColor
        }
        dialogLayout.addView(colorWheel)
        val selectedColorPreview = View(this).apply {
            layoutParams = LinearLayout.LayoutParams((48 * density).toInt(), (48 * density).toInt()).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, (16 * density).toInt(), 0, (8 * density).toInt())
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(currentColor)
                setStroke((2 * density).toInt(), Color.LTGRAY)
            }
        }
        dialogLayout.addView(selectedColorPreview)

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogLayout)
            .setPositiveButton("Done") { _, _ ->
                onColorSelected(colorWheel.selectedColor)
                updateAllUIs()
            }
            .setNegativeButton("Cancel", null)
            .create()

        colorWheel.setOnTouchListener { _, _ ->
            colorWheel.post {
                val color = colorWheel.selectedColor
                (selectedColorPreview.background as? GradientDrawable)?.setColor(color)
                when (title) {
                    "Background Color" -> AppSettings.setColorBg(this, color)
                    "Active Segment Color" -> AppSettings.setColorActive(this, color)
                    "Inactive Segment Color" -> AppSettings.setColorInactive(this, color)
                }
                dialogPreview.refreshColors()
                updateAllUIs()
            }
            false
        }
        dialog.setOnShowListener { dialogPreview.refreshColors() }
        dialog.show()
    }
}
