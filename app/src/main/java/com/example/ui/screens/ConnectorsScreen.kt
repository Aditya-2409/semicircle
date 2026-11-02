package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manager.PtzCameraManager
import com.example.model.ConnectionState
import com.example.model.ConnectorCategory
import com.example.model.ConnectorItem
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalCardSecondary
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.ui.theme.TerminalSuccess
import com.example.ui.theme.TerminalTextSecondary
import com.example.ui.theme.TerminalWarning
import com.example.ui.theme.TerminalWhite

@Composable
fun ConnectorsScreen(
    connectors: List<ConnectorItem>,
    masterSwitchEnabled: Boolean,
    onToggleMasterSwitch: (Boolean) -> Unit,
    onToggleConnector: (id: String, enabled: Boolean) -> Unit,
    onConnect: (id: String) -> Unit,
    onOpenLogin: (id: String) -> Unit = {},
    onDisconnect: (id: String) -> Unit,
    onConfigure: (id: String, config: String) -> Unit,
    onDisconnectAll: () -> Unit,
    ptzCameraManager: PtzCameraManager? = null,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf(ConnectorCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var configuringConnectorId by remember { mutableStateOf<String?>(null) }
    var configText by remember { mutableStateOf("") }
    var showPtzModal by remember { mutableStateOf(false) }

    val filteredList = connectors.filter { item ->
        val matchesCategory = selectedCategory == ConnectorCategory.ALL || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                item.displayName.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("connectors_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TerminalOrangeBright
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "CONNECTORS HUB",
                            color = TerminalOrangeBright,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${connectors.count { it.isEnabled }} OF ${connectors.size} ARMED · LOCAL MEMORY ACTIVE",
                            color = TerminalTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // PTZ D-Pad Quick Trigger Button
                Button(
                    onClick = { showPtzModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalCardSecondary, contentColor = TerminalOrangeBright),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PTZ MOTOR", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Master External Controls Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (masterSwitchEnabled) TerminalSuccess else TerminalError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MASTER EXTERNAL DATA SWITCH",
                                color = TerminalWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (masterSwitchEnabled) "Outbound requests permitted" else "All outbound socket connections blocked",
                                color = if (masterSwitchEnabled) TerminalSuccess else TerminalError,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = masterSwitchEnabled,
                        onCheckedChange = onToggleMasterSwitch,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalOrangeBright,
                            checkedTrackColor = TerminalOrange,
                            uncheckedThumbColor = TerminalTextSecondary,
                            uncheckedTrackColor = TerminalCardSecondary
                        ),
                        modifier = Modifier.testTag("master_external_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter connectors, protocols, services...", color = TerminalTextSecondary, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TerminalOrange) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TerminalOrange,
                    unfocusedBorderColor = TerminalCardSecondary,
                    focusedTextColor = TerminalWhite,
                    unfocusedTextColor = TerminalWhite
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("connector_search_field"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Ribbon
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ConnectorCategory.entries.toTypedArray()) { cat ->
                    val isSelected = selectedCategory == cat
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TerminalOrange else TerminalCardSecondary
                        ),
                        modifier = Modifier
                            .clickable { selectedCategory = cat }
                            .border(1.dp, if (isSelected) TerminalOrangeBright else TerminalCardSecondary, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = cat.title,
                            color = if (isSelected) TerminalBackground else TerminalWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Connectors List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    ConnectorCardItem(
                        item = item,
                        onToggle = { enabled -> onToggleConnector(item.id, enabled) },
                        onConnect = { onConnect(item.id) },
                        onOpenLogin = { onOpenLogin(item.id) },
                        onDisconnect = { onDisconnect(item.id) },
                        onConfigureClick = {
                            configuringConnectorId = item.id
                            configText = item.endpointOrConfig
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Disconnect All Emergency Button
            Button(
                onClick = onDisconnectAll,
                colors = ButtonDefaults.buttonColors(containerColor = TerminalCardSecondary, contentColor = TerminalError),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalError.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .testTag("disconnect_all_button")
            ) {
                Icon(Icons.Default.Cable, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("DISCONNECT ALL SESSIONS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        // Configuration Modal Dialog
        if (configuringConnectorId != null) {
            val item = connectors.find { it.id == configuringConnectorId }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TerminalBackground.copy(alpha = 0.85f))
                    .clickable { configuringConnectorId = null },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TerminalCard),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = false) {}
                        .border(1.dp, TerminalOrange, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "CONFIGURE: ${item?.displayName?.uppercase()}",
                            color = TerminalOrangeBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Set custom IP endpoints, API access tokens, or local memory parameters.",
                            color = TerminalTextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = configText,
                            onValueChange = { configText = it },
                            label = { Text("Config String / IP / Token", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerminalOrange,
                                unfocusedBorderColor = TerminalCardSecondary,
                                focusedTextColor = TerminalWhite,
                                unfocusedTextColor = TerminalWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = { configuringConnectorId = null }) {
                                Text("Cancel", color = TerminalTextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    item?.let { onConfigure(it.id, configText) }
                                    configuringConnectorId = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalOrange, contentColor = TerminalBackground)
                            ) {
                                Text("Save Settings", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // PTZ Camera D-Pad Interactive Controller Modal
        if (showPtzModal && ptzCameraManager != null) {
            val ptzState by ptzCameraManager.ptzState.collectAsStateWithLifecycle()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TerminalBackground.copy(alpha = 0.85f))
                    .clickable { showPtzModal = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TerminalCard),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable(enabled = false) {}
                        .border(1.dp, TerminalOrangeBright, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ONVIF PTZ CAMERA MOTOR CONTROLS",
                            color = TerminalOrangeBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI VOICE CONTROL ARMED ('Pan left', 'Zoom in', 'Tilt up')",
                            color = TerminalCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Current Position Display
                        Text(
                            text = "POSITION: Pan=${ptzState.pan}° · Tilt=${ptzState.tilt}° · Zoom=${"%.1f".format(ptzState.zoom)}x [${ptzState.lastAction}]",
                            color = TerminalWhite,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // D-Pad Motor Layout
                        // Up
                        IconButton(
                            onClick = { ptzCameraManager.tiltUp() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(TerminalCardSecondary, CircleShape)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Tilt Up", tint = TerminalOrangeBright)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left
                            IconButton(
                                onClick = { ptzCameraManager.panLeft() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(TerminalCardSecondary, CircleShape)
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Pan Left", tint = TerminalOrangeBright)
                            }

                            // Center Reset
                            Button(
                                onClick = { ptzCameraManager.resetCenter() },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalOrange, contentColor = TerminalBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("CENTER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Right
                            IconButton(
                                onClick = { ptzCameraManager.panRight() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(TerminalCardSecondary, CircleShape)
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Pan Right", tint = TerminalOrangeBright)
                            }
                        }

                        // Down
                        IconButton(
                            onClick = { ptzCameraManager.tiltDown() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(TerminalCardSecondary, CircleShape)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Tilt Down", tint = TerminalOrangeBright)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Optical Zoom Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = { ptzCameraManager.zoomOut() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalOrangeBright),
                                border = BorderStroke(1.dp, TerminalOrange)
                            ) {
                                Icon(Icons.Default.ZoomOut, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ZOOM OUT", fontSize = 10.sp)
                            }

                            Button(
                                onClick = { ptzCameraManager.zoomIn() },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalOrange, contentColor = TerminalBackground)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ZOOM IN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showPtzModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCardSecondary, contentColor = TerminalWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CLOSE PTZ CONTROLLER", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectorCardItem(
    item: ConnectorItem,
    onToggle: (Boolean) -> Unit,
    onConnect: () -> Unit,
    onOpenLogin: () -> Unit,
    onDisconnect: () -> Unit,
    onConfigureClick: () -> Unit
) {
    val isLoginConnector = item.id.startsWith("google") || item.id == "spotify" || item.id == "home_assistant"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.connectionState == ConnectionState.CONNECTED) TerminalOrange else TerminalCardSecondary,
                RoundedCornerShape(14.dp)
            )
            .testTag("connector_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Category + Name + Enable/Disable Switch (Control A)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "[${item.category.title.uppercase()}]",
                        color = TerminalOrange,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.displayName,
                        color = TerminalWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Control A: ON/OFF Toggle
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TerminalOrangeBright,
                        checkedTrackColor = TerminalOrange,
                        uncheckedThumbColor = TerminalTextSecondary,
                        uncheckedTrackColor = TerminalCardSecondary
                    ),
                    modifier = Modifier.testTag("toggle_${item.id}")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.description,
                color = TerminalTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Indicator Badge & Local Memory Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (badgeColor, label) = when (item.connectionState) {
                        ConnectionState.CONNECTED -> TerminalSuccess to "CONNECTED"
                        ConnectionState.CONNECTING -> TerminalWarning to "CONNECTING..."
                        ConnectionState.DISCONNECTING -> TerminalWarning to "DISCONNECTING..."
                        ConnectionState.ERROR -> TerminalError to "ERROR: ${item.errorMessage ?: "Failed"}"
                        ConnectionState.NEEDS_PERMISSION -> TerminalWarning to "NEEDS PERMISSION"
                        ConnectionState.NEEDS_AUTHENTICATION -> TerminalWarning to "NEEDS AUTH"
                        ConnectionState.DISABLED -> TerminalTextSecondary to "DISABLED"
                        ConnectionState.DISCONNECTED -> TerminalTextSecondary to "DISCONNECTED"
                    }

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isLoginConnector) {
                    Text(
                        text = "LOCAL MEMORY STORED",
                        color = TerminalOrangeBright,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: Connect, Open Login, Configure
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isLoginConnector) {
                    // Open Login Link Button (Requested by User)
                    Button(
                        onClick = onOpenLogin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerminalCardSecondary,
                            contentColor = TerminalOrangeBright
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOGIN LINK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Control B: Connect or Disconnect Button
                if (item.connectionState == ConnectionState.CONNECTING) {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalCardSecondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = TerminalOrange, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CONNECTING", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                } else if (item.connectionState == ConnectionState.CONNECTED) {
                    OutlinedButton(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("disconnect_${item.id}")
                    ) {
                        Text("DISCONNECT", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onConnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerminalOrange,
                            contentColor = TerminalBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("connect_${item.id}")
                    ) {
                        Text("CONNECT", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // Control C: Configure Button
                OutlinedButton(
                    onClick = onConfigureClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("configure_${item.id}")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("CONFIG", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
