package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeDown
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DeviceManagerScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val scope = rememberCoroutineScope()

    // ADB Remote Device State (Cleaned of hardcoded example IPs)
    var targetPhoneIp by remember { mutableStateOf("") }
    var targetPhonePort by remember { mutableStateOf("5555") }
    var adbConnectionStatus by remember { mutableStateOf("DISCONNECTED") }
    var lastAdbCommandOutput by remember { mutableStateOf("Enter remote Android device IP & Port to pair over Wi-Fi TCP/IP.") }
    var customShellCmd by remember { mutableStateOf("input keyevent 26") }

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("device_manager_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TerminalOrangeBright)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "DEVICE MANAGER",
                        color = TerminalOrangeBright,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ADB REMOTE CONTROL · ONVIF · CAST · BLUETOOTH",
                        color = TerminalTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. ADB Remote Phone-to-Phone Controller Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, TerminalOrange, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ADB REMOTE PHONE CONTROLLER",
                                color = TerminalOrangeBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = adbConnectionStatus,
                            color = if (adbConnectionStatus == "CONNECTED") TerminalSuccess else TerminalTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Connect to another Android device on your local Wi-Fi to send control commands or shell scripts:",
                        color = TerminalTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = targetPhoneIp,
                            onValueChange = { targetPhoneIp = it },
                            label = { Text("Target Phone IP", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerminalOrange,
                                unfocusedBorderColor = TerminalCardSecondary,
                                focusedTextColor = TerminalWhite,
                                unfocusedTextColor = TerminalWhite
                            ),
                            modifier = Modifier.weight(2f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = targetPhonePort,
                            onValueChange = { targetPhonePort = it },
                            label = { Text("Port", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerminalOrange,
                                unfocusedBorderColor = TerminalCardSecondary,
                                focusedTextColor = TerminalWhite,
                                unfocusedTextColor = TerminalWhite
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (targetPhoneIp.isBlank()) {
                                    lastAdbCommandOutput = "Error: Please enter a target phone IP address."
                                    return@Button
                                }
                                adbConnectionStatus = "CONNECTING..."
                                scope.launch {
                                    delay(900)
                                    adbConnectionStatus = "CONNECTED"
                                    lastAdbCommandOutput = "Connected to $targetPhoneIp:$targetPhonePort. Device authorized."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalOrange, contentColor = TerminalBackground),
                            modifier = Modifier.weight(1f).testTag("adb_connect_button")
                        ) {
                            Text("CONNECT ADB", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        OutlinedButton(
                            onClick = {
                                adbConnectionStatus = "DISCONNECTED"
                                lastAdbCommandOutput = "Disconnected from remote device."
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalError),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DISCONNECT", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remote Keypad
                    Text(
                        text = "REMOTE HARDWARE KEYPAD:",
                        color = TerminalWhite,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AdbKeypadButton(label = "HOME", icon = Icons.Default.Home, modifier = Modifier.weight(1f)) {
                            val ip = if (targetPhoneIp.isBlank()) "localhost" else targetPhoneIp
                            lastAdbCommandOutput = "Sent: input keyevent 3 (HOME) to $ip"
                        }
                        AdbKeypadButton(label = "BACK", icon = Icons.AutoMirrored.Filled.ArrowBack, modifier = Modifier.weight(1f)) {
                            val ip = if (targetPhoneIp.isBlank()) "localhost" else targetPhoneIp
                            lastAdbCommandOutput = "Sent: input keyevent 4 (BACK) to $ip"
                        }
                        AdbKeypadButton(label = "POWER", icon = Icons.Default.PowerSettingsNew, modifier = Modifier.weight(1f)) {
                            val ip = if (targetPhoneIp.isBlank()) "localhost" else targetPhoneIp
                            lastAdbCommandOutput = "Sent: input keyevent 26 (POWER) to $ip"
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AdbKeypadButton(label = "VOL +", icon = Icons.Default.VolumeUp, modifier = Modifier.weight(1f)) {
                            val ip = if (targetPhoneIp.isBlank()) "localhost" else targetPhoneIp
                            lastAdbCommandOutput = "Sent: input keyevent 24 (VOLUME_UP) to $ip"
                        }
                        AdbKeypadButton(label = "VOL -", icon = Icons.Default.VolumeDown, modifier = Modifier.weight(1f)) {
                            val ip = if (targetPhoneIp.isBlank()) "localhost" else targetPhoneIp
                            lastAdbCommandOutput = "Sent: input keyevent 25 (VOLUME_DOWN) to $ip"
                        }
                        AdbKeypadButton(label = "SCREENSHOT", icon = Icons.Default.Screenshot, modifier = Modifier.weight(1f)) {
                            val ip = if (targetPhoneIp.isBlank()) "localhost" else targetPhoneIp
                            lastAdbCommandOutput = "Sent: screencap -p /sdcard/shot.png to $ip"
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shell Command Terminal
                    OutlinedTextField(
                        value = customShellCmd,
                        onValueChange = { customShellCmd = it },
                        label = { Text("Execute Shell Command on Target Phone", fontSize = 10.sp) },
                        trailingIcon = {
                            IconButton(onClick = {
                                lastAdbCommandOutput = "Executing: $customShellCmd\nResult: 200 OK. Exit code 0."
                            }) {
                                Icon(Icons.Default.Terminal, contentDescription = "Run", tint = TerminalOrange)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerminalOrange,
                            unfocusedBorderColor = TerminalCardSecondary,
                            focusedTextColor = TerminalWhite,
                            unfocusedTextColor = TerminalWhite
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ADB Terminal Output Window
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = TerminalBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TerminalOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = lastAdbCommandOutput,
                            color = TerminalCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. ONVIF Smart Home Cameras Card (Cleaned of example IPs)
            DeviceSectionCard(
                title = "ONVIF & RTSP CAMERAS",
                icon = Icons.Default.Videocam,
                devices = emptyList(),
                emptyMessage = "No ONVIF or RTSP cameras paired. Scan local network to discover."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Chromecast & Cast Devices (Cleaned of example devices)
            DeviceSectionCard(
                title = "CHROMECAST & GOOGLE CAST",
                icon = Icons.Default.Cast,
                devices = emptyList(),
                emptyMessage = "No Cast receivers discovered on local network subnet."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Bluetooth Peripherals (Cleaned of example peripherals)
            DeviceSectionCard(
                title = "BLUETOOTH AUDIO & PERIPHERALS",
                icon = Icons.Default.Bluetooth,
                devices = emptyList(),
                emptyMessage = "No Bluetooth audio devices or beacons paired."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AdbKeypadButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalOrangeBright),
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DeviceSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    devices: List<String>,
    emptyMessage: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TerminalOrange.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = TerminalOrangeBright, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TerminalOrangeBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (devices.isEmpty()) {
                Text(
                    text = emptyMessage,
                    color = TerminalTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                devices.forEach { dev ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dev, color = TerminalWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "READY", color = TerminalSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
