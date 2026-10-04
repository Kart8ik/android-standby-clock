package com.example.myapplication

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

class WeatherRepository(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var lastWeather: WeatherState? = null

    suspend fun fetchWeather(): WeatherState? = withContext(Dispatchers.IO) {
        try {
            val location = getLastKnownLocation() ?: return@withContext lastWeather
            
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current=temperature_2m,is_day,weather_code"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val current = json.getJSONObject("current")
            
            val temp = current.getDouble("temperature_2m").toFloat()
            val code = current.getInt("weather_code")
            val isDay = current.getInt("is_day") == 1
            
            val newState = WeatherState(
                temperature = temp,
                condition = WeatherMapper.fromWmoCode(code),
                isDay = isDay
            )
            lastWeather = newState
            newState
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching weather", e)
            lastWeather
        }
    }

    private fun getLastKnownLocation(): Location? {
        try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            return bestLocation
        } catch (e: SecurityException) {
            return null
        }
    }
}
