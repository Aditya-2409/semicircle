package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BatteryHealthHudBadge
import com.example.ui.components.LiveWeatherBackground
import com.example.ui.components.SemicircleCyberFace
import com.example.ui.components.UniversalQrDialog
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.ui.theme.TerminalSuccess
import com.example.ui.theme.TerminalTextSecondary
import com.example.ui.theme.TerminalWhite
import com.example.voice.AssistantVoiceState
import com.example.weather.model.WeatherConditionType

@Composable
fun FullScreenFaceScreen(
    targetX: Float,
    targetY: Float,
    isFaceDetected: Boolean,
    audioAmplitude: Float,
    voiceState: AssistantVoiceState,
    isLowBatterySad: Boolean,
    batteryLevel: Int = 100,
    isCharging: Boolean = false,
    subtitles: String,
    weatherCondition: WeatherConditionType,
    onActivateOrInterrupt: () -> Unit,
    onQueryVoice: (String) -> Unit = {},
    onExitFullScreen: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Lock strictly to landscape only for fullscreen mode
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    BackHandler {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onExitFullScreen()
    }

    var showQrDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Live Weather Dynamic Background
        LiveWeatherBackground(conditionType = weatherCondition)

        // Landscape Layout: Controls stacked down directly below the face
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isFaceDetected) TerminalSuccess else TerminalTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFaceDetected) "FACE LOCKED" else "SEARCHING FOR FACE",
                        color = if (isFaceDetected) TerminalSuccess else TerminalTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "// GEMINI 3.8 LIVE HUD",
                        color = TerminalOrangeBright,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // AI ACTIVE STATUS BADGE & BATTERY HUD
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AiActiveStatusBadge(voiceState = voiceState)
                    Spacer(modifier = Modifier.width(6.dp))
                    BatteryHealthHudBadge(batteryLevel = batteryLevel, isCharging = isCharging)
                }

                // Exit Button
                Button(
                    onClick = {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        onExitFullScreen()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerminalCard,
                        contentColor = TerminalOrangeBright
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .border(1.dp, TerminalOrange, RoundedCornerShape(20.dp))
                        .testTag("exit_fullscreen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloseFullscreen,
                        contentDescription = "Exit Full Screen",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXIT", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 1. Central Face: Positioned on Top/Center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                SemicircleCyberFace(
                    targetX = targetX,
                    targetY = targetY,
                    isFaceDetected = isFaceDetected,
                    audioAmplitude = audioAmplitude,
                    voiceState = voiceState,
                    isLowBatterySad = isLowBatterySad,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. DOWN BELOW THE FACE: Textbox (Subtitles HUD Card)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard.copy(alpha = 0.95f)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
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
                    IconButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("fullscreen_qr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Show QR Code",
                            tint = TerminalOrangeBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. DOWN BELOW THE TEXTBOX: Big Listening & Activation Button
            val isSpeaking = voiceState == AssistantVoiceState.SPEAKING
            val isListening = voiceState == AssistantVoiceState.LISTENING

            Button(
                onClick = onActivateOrInterrupt,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpeaking) TerminalError else TerminalOrange,
                    contentColor = TerminalBackground
                ),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(48.dp)
                    .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = TerminalOrangeBright)
                    .border(2.dp, if (isSpeaking) Color.White else TerminalOrangeBright, RoundedCornerShape(26.dp))
                    .testTag("activate_interrupt_button")
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isSpeaking -> "INTERRUPT SPEECH"
                        isListening -> "STOP LISTENING"
                        else -> "SPEAK (GEMINI 3.8 LIVE)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Quick Action Chips (Tamil, Weather, PTZ Camera)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                val fullScreenChips = listOf(
                    "வணக்கம்" to "வணக்கம்! (Vanakkam)",
                    "வானிலை எப்படி?" to "வானிலை (Weather)",
                    "Pan camera left" to "PTZ: Pan Left",
                    "Zoom in" to "PTZ: Zoom In",
                    "Can you speak Tamil?" to "Tamil Check"
                )
                items(fullScreenChips) { (query, label) ->
                    AssistChip(
                        onClick = { onQueryVoice(query) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = TerminalOrangeBright,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = TerminalCard),
                        border = BorderStroke(1.dp, TerminalOrange.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Downloadable QR Code Modal
        if (showQrDialog) {
            UniversalQrDialog(
                title = "HUD DATA QR CODE",
                content = subtitles,
                onDismiss = { showQrDialog = false }
            )
        }
    }
}

@Composable
fun AiActiveStatusBadge(
    voiceState: AssistantVoiceState,
    modifier: Modifier = Modifier
) {
    val (label, targetColor) = when (voiceState) {
        AssistantVoiceState.LISTENING -> "● AI ACTIVE: LISTENING" to TerminalOrangeBright
        AssistantVoiceState.THINKING -> "● AI ACTIVE: PROCESSING" to TerminalCyan
        AssistantVoiceState.SPEAKING -> "● AI ACTIVE: RESPONDING" to TerminalSuccess
        AssistantVoiceState.IDLE -> "● AI IDLE: READY" to TerminalTextSecondary
    }

    val animatedColor by animateColorAsState(targetValue = targetColor, label = "badge_color")

    Surface(
        color = TerminalCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, animatedColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = animatedColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
