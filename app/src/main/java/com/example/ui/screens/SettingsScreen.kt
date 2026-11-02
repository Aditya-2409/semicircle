package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.ApiKeyManager
import com.example.manager.AssistantLanguage
import com.example.manager.SettingsRepository
import com.example.ui.components.UniversalQrDialog
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalCardSecondary
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.ui.theme.TerminalSuccess
import com.example.ui.theme.TerminalTextSecondary
import com.example.ui.theme.TerminalWhite
import com.example.voice.VoiceAssistantManager

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    apiKeyManager: ApiKeyManager,
    voiceAssistantManager: VoiceAssistantManager? = null,
    onNavigateToLogs: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val masterExternal by settingsRepository.masterExternalEnabled.collectAsStateWithLifecycle()
    val startOnBoot by settingsRepository.startOnBoot.collectAsStateWithLifecycle()
    val permanentWakeLock by settingsRepository.permanentWakeLock.collectAsStateWithLifecycle()
    val faceTracking by settingsRepository.faceTrackingEnabled.collectAsStateWithLifecycle()
    val cameraEnabled by settingsRepository.cameraEnabled.collectAsStateWithLifecycle()
    val faceAutoActivateAi by settingsRepository.faceAutoActivateAiEnabled.collectAsStateWithLifecycle()
    val wakeWordEnabled by settingsRepository.wakeWordEnabled.collectAsStateWithLifecycle()
    val hapticVibration by settingsRepository.hapticVibrationEnabled.collectAsStateWithLifecycle()
    val tempUnit by settingsRepository.temperatureUnit.collectAsStateWithLifecycle()
    val assistantLang by settingsRepository.assistantLanguage.collectAsStateWithLifecycle()

    val apiKeys by apiKeyManager.apiKeys.collectAsStateWithLifecycle()
    val activeKeyIndex by apiKeyManager.activeKeyIndex.collectAsStateWithLifecycle()
    val quotaMsg by apiKeyManager.lastQuotaExhaustedMessage.collectAsStateWithLifecycle()

    var newKeyInput by remember { mutableStateOf("") }
    var showSystemQrDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TerminalOrangeBright
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CORE CONFIGURATION",
                        color = TerminalOrangeBright,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "REALME X7 5G SYSTEM PARAMETERS & GEMINI 3.8 LIVE",
                        color = TerminalTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Gemini API Keys Management Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI 3.8 LIVE API KEYS",
                                color = TerminalWhite,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${apiKeys.size} REGISTERED",
                            color = TerminalOrange,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Automatic quota failover rotates to next registered key on HTTP 429.",
                        color = TerminalTextSecondary,
                        fontSize = 11.sp
                    )

                    if (quotaMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = quotaMsg ?: "",
                            color = TerminalError,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Registered Keys List
                    apiKeys.forEachIndexed { index, key ->
                        val isActive = index == activeKeyIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    if (isActive) TerminalOrange.copy(alpha = 0.15f) else TerminalCardSecondary,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isActive) TerminalOrangeBright else TerminalCardSecondary,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (index == 0) "PRIMARY KEY: ${key.take(8)}...${key.takeLast(4)}" else "SUBSTITUTE #${index}: ${key.take(8)}...${key.takeLast(4)}",
                                    color = if (isActive) TerminalOrangeBright else TerminalWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isActive) {
                                    Text(
                                        text = "ACTIVE INFERENCE CHANNEL",
                                        color = TerminalSuccess,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (index > 0) {
                                IconButton(
                                    onClick = {
                                        apiKeyManager.removeKey(index)
                                        Toast.makeText(context, "Key removed", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Key", tint = TerminalError, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Add Substitute Key Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newKeyInput,
                            onValueChange = { newKeyInput = it },
                            label = { Text("Paste substitute Gemini API key", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerminalOrange,
                                unfocusedBorderColor = TerminalCardSecondary,
                                focusedTextColor = TerminalWhite,
                                unfocusedTextColor = TerminalWhite
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (apiKeyManager.addSubstituteKey(newKeyInput)) {
                                    newKeyInput = ""
                                    Toast.makeText(context, "Substitute key registered", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Key is invalid or already registered", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalOrange, contentColor = TerminalBackground),
                            modifier = Modifier.testTag("add_key_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("ADD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Camera, Vision & Face Activation Toggles (Requested by User)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CAMERA & OPTICAL VISION SENSORS",
                        color = TerminalOrangeBright,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Camera Hardware Master Toggle
                    SettingToggleRow(
                        icon = Icons.Default.CameraAlt,
                        title = "CAMERA SENSOR HARDWARE",
                        subtitle = "Toggle camera sensor ON or completely OFF for privacy",
                        checked = cameraEnabled,
                        onCheckedChange = { settingsRepository.setCameraEnabled(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pupil Face Tracking
                    SettingToggleRow(
                        icon = Icons.Default.Videocam,
                        title = "PUPIL FACE TRACKING",
                        subtitle = "Real-time cybernetic gaze tracking via front camera",
                        checked = faceTracking,
                        onCheckedChange = { settingsRepository.setFaceTrackingEnabled(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Face-to-Activate AI Toggle (Requested by user)
                    SettingToggleRow(
                        icon = Icons.Default.Face,
                        title = "FACE DETECTION AUTO-ACTIVATE AI",
                        subtitle = "Automatically awaken Gemini AI when face enters camera field",
                        checked = faceAutoActivateAi,
                        onCheckedChange = { settingsRepository.setFaceAutoActivateAiEnabled(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wake Word Toggle
                    SettingToggleRow(
                        icon = Icons.Default.Hearing,
                        title = "SEMICIRCLE WAKE WORD",
                        subtitle = "Say 'Semicircle' or 'ஹே செமிகிர்கிள்' to activate voice listening",
                        checked = wakeWordEnabled,
                        onCheckedChange = { settingsRepository.setWakeWordEnabled(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // System Identity & Configuration QR Code Action
                    Button(
                        onClick = { showSystemQrDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerminalCardSecondary,
                            contentColor = TerminalOrangeBright
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DISPLAY SYSTEM IDENTITY & TELEMETRY QR CODE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Hardware & Power Toggles
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Permanent Wake Lock
                    SettingToggleRow(
                        icon = Icons.Default.LockOpen,
                        title = "PERMANENT WAKE LOCK",
                        subtitle = "Prevents device from sleeping during assistant operation",
                        checked = permanentWakeLock,
                        onCheckedChange = { settingsRepository.setPermanentWakeLock(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Start on Boot
                    SettingToggleRow(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "START ON BOOT",
                        subtitle = "Arm SEMICIRCLE readiness immediately when device boots",
                        checked = startOnBoot,
                        onCheckedChange = { settingsRepository.setStartOnBoot(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Haptic Speech Vibration Toggle
                    SettingToggleRow(
                        icon = Icons.Default.VolumeUp,
                        title = "HAPTIC SPEECH VIBRATION",
                        subtitle = "Vibrate device synchronously while assistant is speaking",
                        checked = hapticVibration,
                        onCheckedChange = { settingsRepository.setHapticVibrationEnabled(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Temperature Units
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Thermostat, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "WEATHER UNIT", color = TerminalWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(text = if (tempUnit == "C") "Celsius (°C)" else "Fahrenheit (°F)", color = TerminalTextSecondary, fontSize = 10.sp)
                            }
                        }
                        Button(
                            onClick = { settingsRepository.setTemperatureUnit(if (tempUnit == "C") "F" else "C") },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCardSecondary, contentColor = TerminalOrangeBright),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.border(1.dp, TerminalOrange, RoundedCornerShape(6.dp))
                        ) {
                            Text(text = if (tempUnit == "C") "SWITCH TO °F" else "SWITCH TO °C", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Gemini 3.8 Live Voice Engine Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI 3.8 LIVE VOICE ENGINE",
                                color = TerminalWhite,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = assistantLang.label,
                            color = TerminalOrangeBright,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Preference Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistantLanguage.entries.forEach { lang ->
                            val isSelected = assistantLang == lang
                            OutlinedButton(
                                onClick = { settingsRepository.setAssistantLanguage(lang) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) TerminalOrange else TerminalCardSecondary,
                                    contentColor = if (isSelected) TerminalBackground else TerminalWhite
                                ),
                                border = BorderStroke(1.dp, if (isSelected) TerminalOrangeBright else TerminalOrange.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = when (lang) {
                                        AssistantLanguage.AUTO -> "AUTO (தமிழ்+EN)"
                                        AssistantLanguage.TAMIL -> "தமிழ்"
                                        AssistantLanguage.ENGLISH -> "ENGLISH"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice Testing Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { voiceAssistantManager?.processUserQuery("வணக்கம்") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalOrangeBright),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TerminalOrange),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TEST தமிழ் VOICE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { voiceAssistantManager?.processUserQuery("Hello Semicircle") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalWhite),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TerminalOrange.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TEST ENG VOICE", fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Activity & Audit Logs Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLogs() }
                    .border(1.dp, TerminalOrange.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .testTag("settings_logs_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "ACTIVITY & DIAGNOSTIC LOGS", color = TerminalWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "Inspect connector execution events & safety audits", color = TerminalTextSecondary, fontSize = 10.sp)
                        }
                    }
                    Text(text = "VIEW >", color = TerminalOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // System Identity QR Modal
        if (showSystemQrDialog) {
            val qrTelemetry = """
                SEMICIRCLE AI TERMINAL
                Device: Realme X7 5G
                Engine: Gemini 3.8 Live
                Language: ${assistantLang.label}
                Camera: ${if (cameraEnabled) "ENABLED" else "DISABLED"}
                Face Tracking: ${if (faceTracking) "ON" else "OFF"}
                Face Auto-Activate: ${if (faceAutoActivateAi) "ARMED" else "OFF"}
                Wake Word: 'Semicircle'
                Keys Registered: ${apiKeys.size}
            """.trimIndent()

            UniversalQrDialog(
                title = "SYSTEM IDENTITY & TELEMETRY",
                content = qrTelemetry,
                onDismiss = { showSystemQrDialog = false }
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, color = TerminalWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(text = subtitle, color = TerminalTextSecondary, fontSize = 10.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TerminalOrangeBright,
                checkedTrackColor = TerminalOrange
            )
        )
    }
}
