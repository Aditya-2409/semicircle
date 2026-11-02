package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.AppLayoutStyle
import com.example.manager.AssistantLanguage
import com.example.model.ConnectionState
import com.example.model.ConnectorItem
import com.example.ui.components.BatteryHealthHudBadge
import com.example.ui.components.LiveWeatherBackground
import com.example.ui.components.SemicircleCyberFace
import com.example.ui.components.UniversalQrDialog
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalCardSecondary
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.ui.theme.TerminalSuccess
import com.example.ui.theme.TerminalTextSecondary
import com.example.ui.theme.TerminalWhite
import com.example.voice.AssistantVoiceState
import com.example.weather.model.WeatherUIModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    targetX: Float,
    targetY: Float,
    isFaceDetected: Boolean,
    audioAmplitude: Float,
    voiceState: AssistantVoiceState,
    isLowBatterySad: Boolean,
    batteryLevel: Int = 100,
    isCharging: Boolean = false,
    subtitles: String,
    weather: WeatherUIModel?,
    temperatureUnit: String,
    connectors: List<ConnectorItem>,
    fullAccessEnabled: Boolean,
    startOnBootEnabled: Boolean,
    permanentWakeLock: Boolean,
    currentLayoutStyle: AppLayoutStyle,
    assistantLanguage: AssistantLanguage = AssistantLanguage.AUTO,
    onSelectLanguage: (AssistantLanguage) -> Unit = {},
    onSelectLayoutStyle: (AppLayoutStyle) -> Unit,
    onToggleFullAccess: (Boolean) -> Unit,
    onToggleStartOnBoot: (Boolean) -> Unit,
    onActivateVoice: () -> Unit,
    onQueryVoice: (String) -> Unit = {},
    onNavigateToFullScreen: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToConnectors: () -> Unit,
    onNavigateToImageGen: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showQrDialog by remember { mutableStateOf(false) }
    val connectedCount = connectors.count { it.connectionState == ConnectionState.CONNECTED }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Dynamic animated weather shader background
        LiveWeatherBackground(conditionType = weather?.conditionType ?: com.example.weather.model.WeatherConditionType.CLEAR)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: Title + Language Switcher + Full Screen Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SEMICIRCLE",
                        color = TerminalOrangeBright,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "REALME X7 5G AI TERMINAL",
                        color = TerminalTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Language Toggle Chip (Auto / தமிழ் / ENG)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = TerminalCardSecondary),
                        modifier = Modifier
                            .clickable {
                                val next = when (assistantLanguage) {
                                    AssistantLanguage.AUTO -> AssistantLanguage.TAMIL
                                    AssistantLanguage.TAMIL -> AssistantLanguage.ENGLISH
                                    AssistantLanguage.ENGLISH -> AssistantLanguage.AUTO
                                }
                                onSelectLanguage(next)
                            }
                            .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = TerminalOrangeBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (assistantLanguage) {
                                    AssistantLanguage.AUTO -> "தமிழ்/EN"
                                    AssistantLanguage.TAMIL -> "தமிழ்"
                                    AssistantLanguage.ENGLISH -> "ENG"
                                },
                                color = TerminalWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    BatteryHealthHudBadge(batteryLevel = batteryLevel, isCharging = isCharging)

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onNavigateToFullScreen,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerminalCard,
                            contentColor = TerminalOrangeBright
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(1.dp, TerminalOrange, RoundedCornerShape(12.dp))
                            .testTag("enter_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Full Screen Face",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FULL SCREEN", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Layout Selector & AI Active Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AiActiveStatusBadge(voiceState = voiceState)

                Text(
                    text = "SELECT LAYOUT:",
                    color = TerminalTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentLayoutStyle.title.uppercase(),
                    color = TerminalOrangeBright,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(AppLayoutStyle.entries) { layout ->
                    val isSelected = currentLayoutStyle == layout
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TerminalOrange else TerminalCard
                        ),
                        modifier = Modifier
                            .clickable { onSelectLayoutStyle(layout) }
                            .border(1.dp, TerminalOrange, RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = layout.title,
                            color = if (isSelected) TerminalBackground else TerminalWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Distinct View Rendering for each Layout Mode
            when (currentLayoutStyle) {
                AppLayoutStyle.CYBER_TERMINAL -> {
                    CyberTerminalLayout(
                        targetX = targetX,
                        targetY = targetY,
                        isFaceDetected = isFaceDetected,
                        audioAmplitude = audioAmplitude,
                        voiceState = voiceState,
                        isLowBatterySad = isLowBatterySad,
                        subtitles = subtitles,
                        fullAccessEnabled = fullAccessEnabled,
                        startOnBootEnabled = startOnBootEnabled,
                        permanentWakeLock = permanentWakeLock,
                        weather = weather,
                        temperatureUnit = temperatureUnit,
                        connectedCount = connectedCount,
                        totalConnectors = connectors.size,
                        onToggleFullAccess = onToggleFullAccess,
                        onToggleStartOnBoot = onToggleStartOnBoot,
                        onActivateVoice = onActivateVoice,
                        onQueryVoice = onQueryVoice,
                        onRequestQr = { showQrDialog = true },
                        onNavigateToWeather = onNavigateToWeather,
                        onNavigateToConnectors = onNavigateToConnectors,
                        onNavigateToImageGen = onNavigateToImageGen,
                        onNavigateToDevices = onNavigateToDevices,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }

                AppLayoutStyle.TACTICAL_HUD -> {
                    TacticalHudLayout(
                        targetX = targetX,
                        targetY = targetY,
                        isFaceDetected = isFaceDetected,
                        audioAmplitude = audioAmplitude,
                        voiceState = voiceState,
                        isLowBatterySad = isLowBatterySad,
                        subtitles = subtitles,
                        weather = weather,
                        temperatureUnit = temperatureUnit,
                        connectedCount = connectedCount,
                        totalConnectors = connectors.size,
                        onActivateVoice = onActivateVoice,
                        onQueryVoice = onQueryVoice,
                        onRequestQr = { showQrDialog = true },
                        onNavigateToConnectors = onNavigateToConnectors,
                        onNavigateToWeather = onNavigateToWeather,
                        onNavigateToDevices = onNavigateToDevices
                    )
                }

                AppLayoutStyle.MINIMAL_FACE -> {
                    MinimalFaceLayout(
                        targetX = targetX,
                        targetY = targetY,
                        isFaceDetected = isFaceDetected,
                        audioAmplitude = audioAmplitude,
                        voiceState = voiceState,
                        isLowBatterySad = isLowBatterySad,
                        subtitles = subtitles,
                        onActivateVoice = onActivateVoice,
                        onQueryVoice = onQueryVoice,
                        onRequestQr = { showQrDialog = true }
                    )
                }

                AppLayoutStyle.DOCK_STATION -> {
                    DockStationLayout(
                        targetX = targetX,
                        targetY = targetY,
                        isFaceDetected = isFaceDetected,
                        audioAmplitude = audioAmplitude,
                        voiceState = voiceState,
                        isLowBatterySad = isLowBatterySad,
                        subtitles = subtitles,
                        weather = weather,
                        temperatureUnit = temperatureUnit,
                        connectedCount = connectedCount,
                        totalConnectors = connectors.size,
                        onActivateVoice = onActivateVoice,
                        onQueryVoice = onQueryVoice,
                        onRequestQr = { showQrDialog = true },
                        onNavigateToWeather = onNavigateToWeather,
                        onNavigateToConnectors = onNavigateToConnectors,
                        onNavigateToDevices = onNavigateToDevices,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Universal QR Dialog
        if (showQrDialog) {
            UniversalQrDialog(
                title = "ON-SCREEN TEXT QR CODE",
                content = subtitles,
                onDismiss = { showQrDialog = false }
            )
        }
    }
}

/**
 * 1. Cyber Terminal / Assistant Layout (Default Dashboard)
 */
@Composable
private fun CyberTerminalLayout(
    targetX: Float,
    targetY: Float,
    isFaceDetected: Boolean,
    audioAmplitude: Float,
    voiceState: AssistantVoiceState,
    isLowBatterySad: Boolean,
    subtitles: String,
    fullAccessEnabled: Boolean,
    startOnBootEnabled: Boolean,
    permanentWakeLock: Boolean,
    weather: WeatherUIModel?,
    temperatureUnit: String,
    connectedCount: Int,
    totalConnectors: Int,
    onToggleFullAccess: (Boolean) -> Unit,
    onToggleStartOnBoot: (Boolean) -> Unit,
    onActivateVoice: () -> Unit,
    onQueryVoice: (String) -> Unit,
    onRequestQr: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToConnectors: () -> Unit,
    onNavigateToImageGen: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Master Controls Card: Full Unrestricted Access & Boot Start
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (fullAccessEnabled) TerminalOrangeBright else TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FULL UNRESTRICTED ACCESS",
                        color = if (fullAccessEnabled) TerminalOrangeBright else TerminalWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Enables & commands all 45 connectors simultaneously",
                        color = TerminalTextSecondary,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = fullAccessEnabled,
                    onCheckedChange = onToggleFullAccess,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TerminalOrangeBright,
                        checkedTrackColor = TerminalOrange,
                        uncheckedThumbColor = TerminalTextSecondary,
                        uncheckedTrackColor = TerminalCardSecondary
                    ),
                    modifier = Modifier.testTag("full_access_toggle")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "START ON BOOT: ",
                        color = TerminalTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Switch(
                        checked = startOnBootEnabled,
                        onCheckedChange = onToggleStartOnBoot,
                        modifier = Modifier
                            .size(width = 44.dp, height = 28.dp)
                            .testTag("start_boot_toggle"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalOrangeBright,
                            checkedTrackColor = TerminalOrange
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (permanentWakeLock) TerminalSuccess else TerminalTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (permanentWakeLock) "WAKE LOCK: ON" else "WAKE LOCK: OFF",
                        color = TerminalTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Central AI Cyber Face (Eyes tracking + Mouth Visualizer)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard.copy(alpha = 0.85f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SemicircleCyberFace(
                targetX = targetX,
                targetY = targetY,
                isFaceDetected = isFaceDetected,
                audioAmplitude = audioAmplitude,
                voiceState = voiceState,
                isLowBatterySad = isLowBatterySad,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitles & One-Tap Universal QR Code Button
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = subtitles,
                        color = TerminalWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    IconButton(
                        onClick = onRequestQr,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("home_qr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Generate QR from text",
                            tint = TerminalOrangeBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Voice Action Button
            val isSpeaking = voiceState == AssistantVoiceState.SPEAKING
            val isListening = voiceState == AssistantVoiceState.LISTENING

            Button(
                onClick = onActivateVoice,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpeaking) TerminalError else TerminalOrange,
                    contentColor = TerminalBackground
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("voice_conversation_button")
            ) {
                Icon(
                    imageVector = when {
                        isSpeaking -> Icons.Default.Stop
                        isListening -> Icons.Default.MicOff
                        else -> Icons.Default.Mic
                    },
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when {
                        isSpeaking -> "INTERRUPT VOICE"
                        isListening -> "STOP LISTENING"
                        else -> "START VOICE CONVERSATION"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TamilEnglishQuickChips(onQueryVoice = onQueryVoice)
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Weather Snapshot Card
    WeatherSnapshotCard(
        weather = weather,
        temperatureUnit = temperatureUnit,
        onClick = onNavigateToWeather
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Subsystem Controls Grid
    SubsystemControlsGrid(
        connectedCount = connectedCount,
        totalConnectors = totalConnectors,
        onNavigateToConnectors = onNavigateToConnectors,
        onNavigateToImageGen = onNavigateToImageGen,
        onNavigateToDevices = onNavigateToDevices,
        onNavigateToSettings = onNavigateToSettings
    )
}

/**
 * 2. Tactical HUD Layout (High-Density Telemetry & Diagnostics)
 */
@Composable
private fun TacticalHudLayout(
    targetX: Float,
    targetY: Float,
    isFaceDetected: Boolean,
    audioAmplitude: Float,
    voiceState: AssistantVoiceState,
    isLowBatterySad: Boolean,
    subtitles: String,
    weather: WeatherUIModel?,
    temperatureUnit: String,
    connectedCount: Int,
    totalConnectors: Int,
    onActivateVoice: () -> Unit,
    onQueryVoice: (String) -> Unit,
    onRequestQr: () -> Unit,
    onNavigateToConnectors: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToDevices: () -> Unit
) {
    // Tactical Top Telemetry Gauges
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Optical Radar Panel
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard),
            modifier = Modifier
                .weight(1f)
                .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Radar, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OPTICAL RADAR", color = TerminalOrangeBright, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFaceDetected) "STATUS: TARGET LOCKED" else "STATUS: SEARCHING",
                    color = if (isFaceDetected) TerminalSuccess else TerminalTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("VEC: [%+.2f, %+.2f]", targetX, targetY),
                    color = TerminalWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Acoustic & Power Telemetry Panel
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard),
            modifier = Modifier
                .weight(1f)
                .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sensors, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TELEMETRY", color = TerminalOrangeBright, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                val bars = (audioAmplitude * 10).toInt().coerceIn(0, 10)
                val visualBars = "█".repeat(bars) + "░".repeat(10 - bars)
                Text(
                    text = "AUDIO: $visualBars",
                    color = TerminalOrangeBright,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isLowBatterySad) "POWER: LOW SADDENED" else "SENTINEL: ARMED",
                    color = if (isLowBatterySad) TerminalError else TerminalSuccess,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Tactical Radar Framed Face
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard.copy(alpha = 0.9f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, TerminalOrange, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("[ + COMBAT HUD + ]", color = TerminalOrange.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("REALME X7 5G", color = TerminalOrange.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }

            SemicircleCyberFace(
                targetX = targetX,
                targetY = targetY,
                isFaceDetected = isFaceDetected,
                audioAmplitude = audioAmplitude,
                voiceState = voiceState,
                isLowBatterySad = isLowBatterySad,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // Subtitle Stream Bar
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subtitles,
                        color = TerminalOrangeBright,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    IconButton(onClick = onRequestQr, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR", tint = TerminalOrangeBright)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tactical Engage Button
            val isSpeaking = voiceState == AssistantVoiceState.SPEAKING
            Button(
                onClick = onActivateVoice,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpeaking) TerminalError else TerminalOrange,
                    contentColor = TerminalBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (isSpeaking) "// INTERRUPT SPEECH STREAM" else "// ENGAGE TACTICAL COMM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Tactical Actions Grid
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard),
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToConnectors() }
                .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("CONNECTORS MATRIX", color = TerminalWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("$connectedCount / $totalConnectors ARMED", color = TerminalOrangeBright, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard),
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToDevices() }
                .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("REMOTE ADB LINK", color = TerminalWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("PHONE-TO-PHONE READY", color = TerminalSuccess, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    TamilEnglishQuickChips(onQueryVoice = onQueryVoice)
}

/**
 * 3. Minimal Face Layout (High-Focus Pure Robot Presence)
 */
@Composable
private fun MinimalFaceLayout(
    targetX: Float,
    targetY: Float,
    isFaceDetected: Boolean,
    audioAmplitude: Float,
    voiceState: AssistantVoiceState,
    isLowBatterySad: Boolean,
    subtitles: String,
    onActivateVoice: () -> Unit,
    onQueryVoice: (String) -> Unit,
    onRequestQr: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Status Pill
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard.copy(alpha = 0.7f)),
            modifier = Modifier.border(1.dp, TerminalOrange.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isFaceDetected) TerminalSuccess else TerminalTextSecondary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFaceDetected) "EYES TRACKING" else "EYES IDLE",
                    color = TerminalWhite,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Full-Bleed Cyber Face
        SemicircleCyberFace(
            targetX = targetX,
            targetY = targetY,
            isFaceDetected = isFaceDetected,
            audioAmplitude = audioAmplitude,
            voiceState = voiceState,
            isLowBatterySad = isLowBatterySad,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitles Ribbon with QR Button
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard.copy(alpha = 0.85f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitles,
                    color = TerminalWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                IconButton(onClick = onRequestQr, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR", tint = TerminalOrangeBright)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Floating Voice Button
        val isSpeaking = voiceState == AssistantVoiceState.SPEAKING
        Button(
            onClick = onActivateVoice,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSpeaking) TerminalError else TerminalOrange,
                contentColor = TerminalBackground
            ),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(58.dp)
        ) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isSpeaking) "INTERRUPT" else "TOUCH TO SPEAK (தமிழ் / ENG)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        TamilEnglishQuickChips(onQueryVoice = onQueryVoice)
    }
}

/**
 * 4. Dock Station Layout (Workstation / Desk Dock with Clock & Weather Station)
 */
@Composable
private fun DockStationLayout(
    targetX: Float,
    targetY: Float,
    isFaceDetected: Boolean,
    audioAmplitude: Float,
    voiceState: AssistantVoiceState,
    isLowBatterySad: Boolean,
    subtitles: String,
    weather: WeatherUIModel?,
    temperatureUnit: String,
    connectedCount: Int,
    totalConnectors: Int,
    onActivateVoice: () -> Unit,
    onQueryVoice: (String) -> Unit,
    onRequestQr: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToConnectors: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFmt.format(now)
            currentDateString = dateFmt.format(now)
            delay(1000)
        }
    }

    // Workstation Digital Clock Header
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DOCK CLOCK & CALENDAR", color = TerminalOrangeBright, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (currentDateString.isNotEmpty()) currentDateString else "Tamil Nadu, India",
                    color = TerminalTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = if (currentTimeString.isNotEmpty()) currentTimeString else "12:00:00",
                color = TerminalWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Split Workstation View: Face + Live Weather Station Telemetry
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left Box: Compact Cyber Face
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard),
            modifier = Modifier
                .weight(1f)
                .height(180.dp)
                .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
            SemicircleCyberFace(
                targetX = targetX,
                targetY = targetY,
                isFaceDetected = isFaceDetected,
                audioAmplitude = audioAmplitude,
                voiceState = voiceState,
                isLowBatterySad = isLowBatterySad,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }

        // Right Box: Weather Station Readout
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalCard),
            modifier = Modifier
                .weight(1f)
                .height(180.dp)
                .clickable { onNavigateToWeather() }
                .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(weather?.cityName ?: "Chennai", color = TerminalOrangeBright, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(16.dp))
                }

                val tempDisplay = if (weather != null) {
                    if (temperatureUnit == "F") String.format("%.1f°F", (weather.temperatureCelsius * 9 / 5) + 32)
                    else String.format("%.1f°C", weather.temperatureCelsius)
                } else "28.5°C"

                Text(tempDisplay, color = TerminalWhite, fontSize = 26.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)

                Column {
                    Text("HUMIDITY: ${weather?.humidityPercent ?: 58}%", color = TerminalTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("WIND: ${weather?.windSpeedKmh ?: 14.0} km/h", color = TerminalTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("DETAILS >", color = TerminalOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Subtitle & Speech Strip
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalOrange.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subtitles,
                color = TerminalWhite,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            IconButton(onClick = onRequestQr, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.QrCode, contentDescription = "QR", tint = TerminalOrangeBright)
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Voice Action Button
    val isSpeaking = voiceState == AssistantVoiceState.SPEAKING
    Button(
        onClick = onActivateVoice,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSpeaking) TerminalError else TerminalOrange,
            contentColor = TerminalBackground
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = if (isSpeaking) "INTERRUPT VOICE" else "ACTIVATE VOICE ASSISTANT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    TamilEnglishQuickChips(onQueryVoice = onQueryVoice)

    Spacer(modifier = Modifier.height(12.dp))

    // Workstation Quick Dock Navigation Strip
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickDockButton(
            title = "CONNECTORS",
            icon = Icons.Default.Cable,
            badge = "$connectedCount/$totalConnectors",
            onClick = onNavigateToConnectors,
            modifier = Modifier.weight(1f)
        )
        QuickDockButton(
            title = "REMOTE ADB",
            icon = Icons.Default.Devices,
            badge = "LINK",
            onClick = onNavigateToDevices,
            modifier = Modifier.weight(1f)
        )
        QuickDockButton(
            title = "SETTINGS",
            icon = Icons.Default.Settings,
            badge = "CONFIG",
            onClick = onNavigateToSettings,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickDockButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, TerminalOrange.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = TerminalWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(badge, color = TerminalTextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

/**
 * Shared Tamil & English Quick Chips
 */
@Composable
private fun TamilEnglishQuickChips(onQueryVoice: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "QUICK COMMANDS (தமிழ் / ENG):",
            color = TerminalTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val quickQueries = listOf(
                "வணக்கம்" to "வணக்கம்! (Vanakkam)",
                "சென்னை வானிலை எப்படி இருக்கிறது?" to "வானிலை (Weather)",
                "பேட்டரி நிலை என்ன?" to "பேட்டரி (Battery)",
                "தற்போதைய நேரம் என்ன?" to "நேரம் (Time)",
                "எந்தெந்த கனெக்டர்கள் இயங்குகின்றன?" to "இணைப்புகள் (Connectors)",
                "Can you speak Tamil?" to "தமிழ் ஆய்வு (Tamil Check)"
            )
            items(quickQueries) { (query, label) ->
                AssistChip(
                    onClick = { onQueryVoice(query) },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = TerminalOrangeBright,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = TerminalCardSecondary
                    ),
                    border = BorderStroke(1.dp, TerminalOrange.copy(alpha = 0.5f))
                )
            }
        }
    }
}

/**
 * Weather Snapshot Card
 */
@Composable
private fun WeatherSnapshotCard(
    weather: WeatherUIModel?,
    temperatureUnit: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .testTag("home_weather_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = TerminalOrangeBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = weather?.cityName ?: "Tamil Nadu, India",
                        color = TerminalOrangeBright,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${weather?.conditionDescription ?: "Clear Sky"} · Humidity ${weather?.humidityPercent ?: 55}% · Wind ${weather?.windSpeedKmh ?: 12.0} km/h",
                    color = TerminalTextSecondary,
                    fontSize = 11.sp
                )
            }

            val tempDisplay = if (weather != null) {
                if (temperatureUnit == "F") {
                    String.format("%.1f°F", (weather.temperatureCelsius * 9 / 5) + 32)
                } else {
                    String.format("%.1f°C", weather.temperatureCelsius)
                }
            } else "28.5°C"

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = tempDisplay,
                    color = TerminalWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "DETAILS >",
                    color = TerminalOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Subsystem Controls Grid
 */
@Composable
private fun SubsystemControlsGrid(
    connectedCount: Int,
    totalConnectors: Int,
    onNavigateToConnectors: () -> Unit,
    onNavigateToImageGen: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Text(
        text = "SUBSYSTEM CONTROLS",
        color = TerminalTextSecondary,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionCard(
            title = "CONNECTORS",
            subtitle = "$connectedCount / $totalConnectors Online",
            icon = Icons.Default.Cable,
            onClick = onNavigateToConnectors,
            modifier = Modifier.weight(1f),
            testTag = "quick_connectors_button"
        )
        QuickActionCard(
            title = "IMAGE GEN",
            subtitle = "AI Visual + QR",
            icon = Icons.Default.Image,
            onClick = onNavigateToImageGen,
            modifier = Modifier.weight(1f),
            testTag = "quick_image_button"
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionCard(
            title = "DEVICES",
            subtitle = "ADB & Cast",
            icon = Icons.Default.Devices,
            onClick = onNavigateToDevices,
            modifier = Modifier.weight(1f),
            testTag = "quick_devices_button"
        )
        QuickActionCard(
            title = "SETTINGS",
            subtitle = "API Keys & Config",
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings,
            modifier = Modifier.weight(1f),
            testTag = "quick_settings_button"
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, TerminalOrange.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TerminalOrangeBright,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = TerminalWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtitle,
                color = TerminalTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
