package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiveWeatherBackground
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalCardSecondary
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.ui.theme.TerminalTextSecondary
import com.example.ui.theme.TerminalWhite
import com.example.weather.model.GeocodingResult
import com.example.weather.model.WeatherConditionType
import com.example.weather.model.WeatherUIModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeatherScreen(
    weather: WeatherUIModel?,
    isLoading: Boolean,
    errorMessage: String?,
    temperatureUnit: String,
    tamilNaduCities: List<GeocodingResult>,
    onRefreshExactGps: () -> Unit,
    onSelectCity: (GeocodingResult) -> Unit,
    onSearchQuery: (String) -> Unit,
    searchResults: List<GeocodingResult>,
    onToggleUnit: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Dynamic Live Weather Background based on telemetry condition
        LiveWeatherBackground(conditionType = weather?.conditionType ?: WeatherConditionType.CLEAR)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("weather_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TerminalOrangeBright
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "WEATHER RADAR",
                            color = TerminalOrangeBright,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "OPEN-METEO · TAMIL NADU & GLOBAL",
                            color = TerminalTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // °C / °F Selector Button
                Button(
                    onClick = { onToggleUnit(if (temperatureUnit == "C") "F" else "C") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerminalCard,
                        contentColor = TerminalOrangeBright
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, TerminalOrange, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = if (temperatureUnit == "C") "°C (CELSIUS)" else "°F (FAHRENHEIT)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tamil Nadu Quick Selector Chips
            Text(
                text = "TAMIL NADU REGIONAL STATIONS",
                color = TerminalTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tamilNaduCities) { city ->
                    val isSelected = weather?.cityName?.contains(city.name, ignoreCase = true) == true
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TerminalOrange else TerminalCard
                        ),
                        modifier = Modifier
                            .clickable { onSelectCity(city) }
                            .border(1.dp, TerminalOrange, RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = city.name,
                            color = if (isSelected) TerminalBackground else TerminalWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Exact GPS Refresh & Search Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQuery(it)
                    },
                    label = { Text("Search city (e.g. Madurai, Paris, Tokyo)", fontSize = 11.sp) },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TerminalOrange)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TerminalOrange,
                        unfocusedBorderColor = TerminalCardSecondary,
                        focusedTextColor = TerminalWhite,
                        unfocusedTextColor = TerminalWhite,
                        cursorColor = TerminalOrange
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onRefreshExactGps,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerminalCard,
                        contentColor = TerminalOrangeBright
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .border(1.dp, TerminalOrange, RoundedCornerShape(8.dp))
                        .testTag("exact_gps_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TerminalOrangeBright, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = "Exact GPS")
                    }
                }
            }

            // Search Results Dropdown List
            if (searchResults.isNotEmpty() && searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TerminalCardSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        searchResults.take(4).forEach { res ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectCity(res)
                                        searchQuery = ""
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${res.name}, ${res.admin1 ?: ""} (${res.country ?: ""})",
                                    color = TerminalWhite,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "SELECT",
                                    color = TerminalOrangeBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Current Weather Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard.copy(alpha = 0.9f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, TerminalOrange, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = weather?.cityName ?: "Tamil Nadu, India",
                                    color = TerminalWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "LAT: ${String.format("%.2f", weather?.latitude ?: 13.08)}  LON: ${String.format("%.2f", weather?.longitude ?: 80.27)}",
                                color = TerminalTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        val formattedTemp = if (weather != null) {
                            if (temperatureUnit == "F") {
                                String.format("%.1f°F", (weather.temperatureCelsius * 9 / 5) + 32)
                            } else {
                                String.format("%.1f°C", weather.temperatureCelsius)
                            }
                        } else "28.5°C"

                        Text(
                            text = formattedTemp,
                            color = TerminalOrangeBright,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = weather?.conditionDescription?.uppercase() ?: "CLEAR SKY",
                        color = TerminalWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Grid (Humidity, Wind, Rain, UV)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeatherMetricItem(icon = Icons.Default.WaterDrop, label = "HUMIDITY", value = "${weather?.humidityPercent ?: 55}%")
                        WeatherMetricItem(icon = Icons.Default.Air, label = "WIND", value = "${weather?.windSpeedKmh ?: 12.0} km/h")
                        WeatherMetricItem(icon = Icons.Default.WaterDrop, label = "PRECIP", value = "${weather?.precipitationMm ?: 0.0} mm")
                        WeatherMetricItem(icon = Icons.Default.WbSunny, label = "UV INDEX", value = "${weather?.uvIndex ?: 4.0}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(weather?.lastUpdatedTimestamp ?: System.currentTimeMillis()))
                    Text(
                        text = "TELEMETRY SYNCHRONIZED: $timeStr · ZERO API KEY REQUIRED",
                        color = TerminalTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 24-Hour Hourly Forecast
            Text(
                text = "24-HOUR HOURLY RADAR",
                color = TerminalOrangeBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val times = weather?.hourlyTimes ?: emptyList()
                    val temps = weather?.hourlyTemps ?: emptyList()

                    for (i in 0 until minOf(times.size, temps.size, 24)) {
                        val hourStr = times[i].split("T").getOrNull(1)?.take(5) ?: "${i}:00"
                        val tempVal = if (temperatureUnit == "F") (temps[i] * 9 / 5) + 32 else temps[i]

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = hourStr, color = TerminalTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = String.format("%.0f°", tempVal), color = TerminalOrangeBright, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7-Day Forecast
            Text(
                text = "7-DAY EXTENDED FORECAST",
                color = TerminalOrangeBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val days = weather?.dailyTimes ?: emptyList()
                    val maxTemps = weather?.dailyMaxTemps ?: emptyList()
                    val minTemps = weather?.dailyMinTemps ?: emptyList()

                    for (i in 0 until minOf(days.size, maxTemps.size, minTemps.size, 7)) {
                        val maxDisplay = if (temperatureUnit == "F") (maxTemps[i] * 9 / 5) + 32 else maxTemps[i]
                        val minDisplay = if (temperatureUnit == "F") (minTemps[i] * 9 / 5) + 32 else minTemps[i]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DAY ${i + 1} (${days[i]})",
                                color = TerminalWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Row {
                                Text(
                                    text = String.format("MIN: %.0f°", minDisplay),
                                    color = TerminalCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = String.format("MAX: %.0f°", maxDisplay),
                                    color = TerminalOrangeBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WeatherMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = TerminalOrange, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = TerminalTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = TerminalWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
