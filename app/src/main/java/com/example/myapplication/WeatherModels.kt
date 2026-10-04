package com.example.myapplication

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOGGY,
    RAIN,
    HEAVY_RAIN,
    THUNDERSTORM,
    SNOW,
    WINDY,
    UNKNOWN
}

data class WeatherState(
    val temperature: Float,
    val condition: WeatherCondition,
    val isDay: Boolean = true
)

object WeatherMapper {
    fun fromWmoCode(code: Int): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition.CLEAR
            1, 2 -> WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.FOGGY // Fog
            51, 53, 55, 61, 63, 65 -> WeatherCondition.RAIN
            80, 81, 82 -> WeatherCondition.HEAVY_RAIN
            95, 96, 99 -> WeatherCondition.THUNDERSTORM
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
            else -> WeatherCondition.UNKNOWN
        }
    }
}
