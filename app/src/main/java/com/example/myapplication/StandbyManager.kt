package com.example.myapplication

import android.content.Context
import android.content.Intent

/**
 * Manages the StandBy mode state and activation logic.
 * This is a simple abstraction to prepare for future automatic activation features.
 */
class StandbyManager(private val context: Context) {

    fun startStandby() {
        val intent = Intent(context, StandbyActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun isStandbyEnabled(): Boolean {
        // Future: Check user preferences or system state
        return true
    }
}
