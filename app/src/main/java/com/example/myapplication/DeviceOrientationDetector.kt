package com.example.myapplication

import android.content.Context
import android.hardware.SensorManager
import android.view.OrientationEventListener

class DeviceOrientationDetector(context: Context, private val onOrientationChanged: (Boolean) -> Unit) {

    private val orientationEventListener = object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return
            
            // Landscape is roughly around 90 or 270 degrees
            val isLandscape = (orientation in 60..120) || (orientation in 240..300)
            onOrientationChanged(isLandscape)
        }
    }

    fun start() {
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
    }

    fun stop() {
        orientationEventListener.disable()
    }
}
