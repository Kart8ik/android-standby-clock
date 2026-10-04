package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class BedsideDreamService : DreamService() {

    private lateinit var clockView: LcdClockView
    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            clockView.updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    private val weatherRepository by lazy { WeatherRepository(this) }
    private val weatherScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val weatherRunnable = object : Runnable {
        override fun run() {
            fetchWeather()
            handler.postDelayed(this, 5 * 60 * 1000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = false
        isFullscreen = true
        isScreenBright = false

        clockView = LcdClockView(this)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setContentView(clockView, params)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        handler.post(tickRunnable)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            handler.post(weatherRunnable)
        }
    }

    private fun fetchWeather() {
        weatherScope.launch {
            val state = weatherRepository.fetchWeather()
            clockView.setWeather(state)
        }
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        handler.removeCallbacks(tickRunnable)
        handler.removeCallbacks(weatherRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        weatherScope.cancel()
    }
}
