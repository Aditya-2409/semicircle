package com.example.weather.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.example.weather.api.OpenMeteoApi
import com.example.weather.api.OpenMeteoGeocodingApi
import com.example.weather.model.GeocodingResult
import com.example.weather.model.WeatherConditionType
import com.example.weather.model.WeatherUIModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WeatherRepository(private val context: Context) {
    private val openMeteoApi = OpenMeteoApi.create()
    private val geocodingApi = OpenMeteoGeocodingApi.create()
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val _weatherState = MutableStateFlow<WeatherUIModel?>(null)
    val weatherState: StateFlow<WeatherUIModel?> = _weatherState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Default coordinates: Chennai, Tamil Nadu, India (13.0827, 80.2707)
    private var lastLat = 13.0827
    private var lastLon = 80.2707
    private var lastCityName = "Chennai, Tamil Nadu"

    val tamilNaduPopularCities = listOf(
        GeocodingResult(1L, "Chennai", 13.0827, 80.2707, "India", "Tamil Nadu"),
        GeocodingResult(2L, "Coimbatore", 11.0168, 76.9558, "India", "Tamil Nadu"),
        GeocodingResult(3L, "Madurai", 9.9252, 78.1198, "India", "Tamil Nadu"),
        GeocodingResult(4L, "Tiruchirappalli", 10.7905, 78.7047, "India", "Tamil Nadu"),
        GeocodingResult(5L, "Salem", 11.6643, 78.1460, "India", "Tamil Nadu"),
        GeocodingResult(6L, "Tirunelveli", 8.7139, 77.7567, "India", "Tamil Nadu"),
        GeocodingResult(7L, "Vellore", 12.9165, 79.1325, "India", "Tamil Nadu"),
        GeocodingResult(8L, "Kanyakumari", 8.0883, 77.5385, "India", "Tamil Nadu")
    )

    @SuppressLint("MissingPermission")
    suspend fun refreshWithExactLocation(): Boolean = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            var loc: Location? = null
            try {
                loc = fusedLocationClient.lastLocation.await()
            } catch (e: Exception) {
                Log.w("WeatherRepo", "Location services unavailable, using last known coordinates: ${e.message}")
            }

            val lat = loc?.latitude ?: lastLat
            val lon = loc?.longitude ?: lastLon
            lastLat = lat
            lastLon = lon
            lastCityName = if (loc != null) "GPS: ${String.format("%.2f", lat)}, ${String.format("%.2f", lon)}" else "Default Station"

            val response = openMeteoApi.getForecast(lat, lon)
            val current = response.current
            val code = current?.weatherCode ?: 0
            val (desc, type) = parseWeatherCode(code)

            val model = WeatherUIModel(
                cityName = lastCityName,
                latitude = lat,
                longitude = lon,
                temperatureCelsius = current?.temperature ?: 22.0,
                apparentTemperatureCelsius = current?.apparentTemperature ?: 23.0,
                humidityPercent = current?.humidity?.toInt() ?: 50,
                windSpeedKmh = current?.windSpeed ?: 10.0,
                precipitationMm = current?.precipitation ?: 0.0,
                uvIndex = current?.uvIndex ?: 3.5,
                weatherCode = code,
                conditionDescription = desc,
                conditionType = type,
                hourlyTimes = response.hourly?.time?.take(24) ?: emptyList(),
                hourlyTemps = response.hourly?.temperature?.take(24) ?: emptyList(),
                dailyTimes = response.daily?.time?.take(7) ?: emptyList(),
                dailyMaxTemps = response.daily?.temperatureMax?.take(7) ?: emptyList(),
                dailyMinTemps = response.daily?.temperatureMin?.take(7) ?: emptyList(),
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            _weatherState.value = model
            _isLoading.value = false
            true
        } catch (e: Exception) {
            Log.e("WeatherRepo", "Failed to fetch Open-Meteo weather", e)
            _errorMessage.value = "Weather telemetry error: ${e.localizedMessage ?: "Network unreachable"}"
            _isLoading.value = false
            false
        }
    }

    suspend fun fetchWeatherForCity(result: GeocodingResult): Boolean = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            lastLat = result.latitude
            lastLon = result.longitude
            lastCityName = "${result.name}, ${result.country ?: ""}"

            val response = openMeteoApi.getForecast(result.latitude, result.longitude)
            val current = response.current
            val code = current?.weatherCode ?: 0
            val (desc, type) = parseWeatherCode(code)

            val model = WeatherUIModel(
                cityName = lastCityName,
                latitude = result.latitude,
                longitude = result.longitude,
                temperatureCelsius = current?.temperature ?: 22.0,
                apparentTemperatureCelsius = current?.apparentTemperature ?: 23.0,
                humidityPercent = current?.humidity?.toInt() ?: 50,
                windSpeedKmh = current?.windSpeed ?: 10.0,
                precipitationMm = current?.precipitation ?: 0.0,
                uvIndex = current?.uvIndex ?: 3.5,
                weatherCode = code,
                conditionDescription = desc,
                conditionType = type,
                hourlyTimes = response.hourly?.time?.take(24) ?: emptyList(),
                hourlyTemps = response.hourly?.temperature?.take(24) ?: emptyList(),
                dailyTimes = response.daily?.time?.take(7) ?: emptyList(),
                dailyMaxTemps = response.daily?.temperatureMax?.take(7) ?: emptyList(),
                dailyMinTemps = response.daily?.temperatureMin?.take(7) ?: emptyList(),
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            _weatherState.value = model
            _isLoading.value = false
            true
        } catch (e: Exception) {
            Log.e("WeatherRepo", "Failed to fetch city weather", e)
            _errorMessage.value = "City weather error: ${e.localizedMessage ?: "Unknown error"}"
            _isLoading.value = false
            false
        }
    }

    suspend fun searchCities(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        try {
            val response = geocodingApi.searchCity(query.trim())
            response.results ?: emptyList()
        } catch (e: Exception) {
            Log.e("WeatherRepo", "City search failed", e)
            emptyList()
        }
    }

    private fun parseWeatherCode(code: Int): Pair<String, WeatherConditionType> {
        return when (code) {
            0 -> "Clear Sky" to WeatherConditionType.CLEAR
            1 -> "Mainly Clear" to WeatherConditionType.CLEAR
            2 -> "Partly Cloudy" to WeatherConditionType.CLOUDY
            3 -> "Overcast" to WeatherConditionType.CLOUDY
            45, 48 -> "Dense Fog" to WeatherConditionType.FOG
            51, 53, 55 -> "Light Drizzle" to WeatherConditionType.RAIN
            61, 63, 65 -> "Continuous Rain" to WeatherConditionType.RAIN
            66, 67 -> "Freezing Rain" to WeatherConditionType.RAIN
            71, 73, 75 -> "Snow Fall" to WeatherConditionType.SNOW
            77 -> "Snow Grains" to WeatherConditionType.SNOW
            80, 81, 82 -> "Rain Showers" to WeatherConditionType.RAIN
            85, 86 -> "Snow Showers" to WeatherConditionType.SNOW
            95 -> "Thunderstorm" to WeatherConditionType.STORM
            96, 99 -> "Heavy Thunderstorm with Hail" to WeatherConditionType.STORM
            else -> "Atmospheric Conditions" to WeatherConditionType.CLEAR
        }
    }
}
