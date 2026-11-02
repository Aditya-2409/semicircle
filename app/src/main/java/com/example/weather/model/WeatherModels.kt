package com.example.weather.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val current: CurrentWeatherUnits? = null,
    val hourly: HourlyWeatherData? = null,
    val daily: DailyWeatherData? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherUnits(
    val time: String? = null,
    @Json(name = "temperature_2m") val temperature: Double = 0.0,
    @Json(name = "relative_humidity_2m") val humidity: Double = 0.0,
    @Json(name = "apparent_temperature") val apparentTemperature: Double = 0.0,
    @Json(name = "precipitation") val precipitation: Double = 0.0,
    @Json(name = "weather_code") val weatherCode: Int = 0,
    @Json(name = "wind_speed_10m") val windSpeed: Double = 0.0,
    @Json(name = "uv_index") val uvIndex: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherData(
    val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature: List<Double> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyWeatherData(
    val time: List<String> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double> = emptyList(),
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double> = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbability: List<Int>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)

enum class WeatherConditionType {
    CLEAR,
    CLOUDY,
    RAIN,
    STORM,
    SNOW,
    FOG
}

data class WeatherUIModel(
    val cityName: String = "Detecting Location...",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val temperatureCelsius: Double = 24.0,
    val apparentTemperatureCelsius: Double = 25.0,
    val humidityPercent: Int = 55,
    val windSpeedKmh: Double = 12.0,
    val precipitationMm: Double = 0.0,
    val uvIndex: Double = 4.0,
    val weatherCode: Int = 0,
    val conditionDescription: String = "Clear Sky",
    val conditionType: WeatherConditionType = WeatherConditionType.CLEAR,
    val hourlyTimes: List<String> = emptyList(),
    val hourlyTemps: List<Double> = emptyList(),
    val dailyTimes: List<String> = emptyList(),
    val dailyMaxTemps: List<Double> = emptyList(),
    val dailyMinTemps: List<Double> = emptyList(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
