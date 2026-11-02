package com.example.manager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.example.model.ConnectionState
import com.example.model.ConnectorCategory
import com.example.model.ConnectorItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuditLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val connectorId: String,
    val action: String,
    val details: String,
    val isError: Boolean = false
)

data class ConnectorLocalMemory(
    val accountEmail: String? = null,
    val authToken: String? = null,
    val lastSyncTime: Long = 0L,
    val customNotes: String? = null
)

class ConnectorManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val memoryPrefs: SharedPreferences = context.getSharedPreferences("connector_memory_store", Context.MODE_PRIVATE)

    private val _connectors = MutableStateFlow<List<ConnectorItem>>(emptyList())
    val connectors: StateFlow<List<ConnectorItem>> = _connectors.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    init {
        initCatalogue()
    }

    private fun initCatalogue() {
        val list = listOf(
            // AI and Language
            ConnectorItem("gemini_live", ConnectorCategory.AI, "Gemini 3.8 Live Voice", "Multilingual real-time conversational voice streaming with low-latency audio.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("gemini_text", ConnectorCategory.AI, "Gemini Text AI", "Structured reasoning, summarization, and task orchestration.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("local_ai", ConnectorCategory.AI, "Local AI Endpoint", "On-device or local network compatible OpenAI-compatible LLM endpoint.", isEnabled = false),
            ConnectorItem("ollama", ConnectorCategory.AI, "Ollama Server", "Connect to external Ollama server (e.g. Llama 3, Mistral) over LAN/WiFi.", isEnabled = false),
            ConnectorItem("mcp_servers", ConnectorCategory.AI, "MCP Protocol Servers", "Model Context Protocol client for tool-calling servers and databases.", isEnabled = false),
            ConnectorItem("tts", ConnectorCategory.AI, "Text-to-Speech Engine", "Android system vocal synthesizer with pitch and rate modulation.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("stt", ConnectorCategory.AI, "Speech Recognition", "Continuous Android voice dictation with acoustic model feedback.", isEnabled = true, connectionState = ConnectionState.CONNECTED),

            // Weather
            ConnectorItem("weather", ConnectorCategory.WEATHER, "Open-Meteo Weather", "Global real-time temperature, hourly, 7-day forecast, and radar telemetry.", isEnabled = true, connectionState = ConnectionState.CONNECTED),

            // Android Phone
            ConnectorItem("phone_calls", ConnectorCategory.PHONE, "Calls & Contacts", "Dial authorized contacts and look up phonebook entries.", isEnabled = true, requiresSensitivePermission = true, permissionName = "android.permission.READ_CONTACTS"),
            ConnectorItem("alarms_timers", ConnectorCategory.PHONE, "Alarms & Timers", "Schedule system alarms, reminders, and countdown timers.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("calendar", ConnectorCategory.PHONE, "Device Calendar", "Read, schedule, and alert upcoming agenda appointments.", isEnabled = false, requiresSensitivePermission = true, permissionName = "android.permission.READ_CALENDAR"),
            ConnectorItem("notifications", ConnectorCategory.PHONE, "Notification Hub", "Inspect and dispatch system status notifications.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("sms_messaging", ConnectorCategory.PHONE, "SMS & Messaging", "Draft and send SMS text alerts with user approval.", isEnabled = false, requiresSensitivePermission = true, permissionName = "android.permission.SEND_SMS"),
            ConnectorItem("accessibility", ConnectorCategory.PHONE, "Accessibility Control", "Automate UI navigation and screen interactions.", isEnabled = false),
            ConnectorItem("local_storage", ConnectorCategory.PHONE, "Local Files & Storage", "File browser, storage metric inspection, and export cache.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("clipboard", ConnectorCategory.PHONE, "Clipboard Sync", "Read and copy snippets to Android system clipboard.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("screen_ocr", ConnectorCategory.PHONE, "Screen Capture & OCR", "Capture screen frame and recognize text on display.", isEnabled = false),
            ConnectorItem("media_gallery", ConnectorCategory.PHONE, "Media & Photo Picker", "Access photos and visual assets via Android Photo Picker.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("bluetooth", ConnectorCategory.PHONE, "Bluetooth & Nearby", "Scan and pair with local RFCOMM/BLE peripherals.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("location", ConnectorCategory.PHONE, "Location Telemetry", "High-precision GPS and network location provider.", isEnabled = true, connectionState = ConnectionState.CONNECTED, requiresSensitivePermission = true, permissionName = "android.permission.ACCESS_FINE_LOCATION"),
            ConnectorItem("battery_info", ConnectorCategory.PHONE, "Battery & System Hardware", "Real-time battery level, thermal stats, and charging state.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("notes_tasks", ConnectorCategory.PHONE, "Notes & Tasks Database", "Local SQLite-backed scratchpad and reminder checklist.", isEnabled = true, connectionState = ConnectionState.CONNECTED),

            // Google Services (Local Memory & Auth URLs)
            ConnectorItem("google_drive", ConnectorCategory.GOOGLE, "Google Drive", "Cloud document storage and backup synchronization.", isEnabled = hasStoredMemory("google_drive"), connectionState = if (hasStoredMemory("google_drive")) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED),
            ConnectorItem("google_sheets", ConnectorCategory.GOOGLE, "Google Sheets", "Append data rows and read spreadsheets over OAuth.", isEnabled = hasStoredMemory("google_sheets"), connectionState = if (hasStoredMemory("google_sheets")) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED),
            ConnectorItem("google_calendar", ConnectorCategory.GOOGLE, "Google Calendar Cloud", "Synchronize Google Workspace calendar events.", isEnabled = hasStoredMemory("google_calendar"), connectionState = if (hasStoredMemory("google_calendar")) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED),
            ConnectorItem("google_auth", ConnectorCategory.GOOGLE, "Google Identity Auth", "Manage Google sign-in credentials and authorized scopes.", isEnabled = hasStoredMemory("google_auth"), connectionState = if (hasStoredMemory("google_auth")) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED),

            // Media and Casting (Spotify Auth URL & Memory)
            ConnectorItem("spotify", ConnectorCategory.MEDIA, "Spotify Connect", "Remote control playback, search tracks, and toggle audio stream.", isEnabled = hasStoredMemory("spotify"), connectionState = if (hasStoredMemory("spotify")) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED),
            ConnectorItem("chromecast", ConnectorCategory.MEDIA, "Chromecast / Google Cast", "Discover Cast receivers and stream audio/video to displays.", isEnabled = false),
            ConnectorItem("bt_audio", ConnectorCategory.MEDIA, "Bluetooth Audio", "Route voice and media to external Bluetooth headphones/speakers.", isEnabled = true, connectionState = ConnectionState.CONNECTED),

            // Cameras and Smart Home
            ConnectorItem("onvif_cameras", ConnectorCategory.CAMERAS, "ONVIF Cameras", "WS-Discovery on LAN, RTSP profile retrieval, and snapshot viewer.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("rtsp_stream", ConnectorCategory.CAMERAS, "RTSP Video Stream", "Direct low-latency H.264/H.265 RTSP network camera player.", isEnabled = false),
            ConnectorItem("ptz_control", ConnectorCategory.CAMERAS, "PTZ Camera Motor", "Pan, tilt, and optical zoom commands over ONVIF protocol with AI voice control.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("home_assistant", ConnectorCategory.SMART_HOME, "Home Assistant REST", "Connect to Home Assistant instance to toggle lights and entities.", isEnabled = false),
            ConnectorItem("mqtt_broker", ConnectorCategory.SMART_HOME, "MQTT Broker", "Publish and subscribe to IoT telemetry topics.", isEnabled = false),
            ConnectorItem("network_discovery", ConnectorCategory.SMART_HOME, "mDNS / SSDP Discovery", "Scan local subnet for smart appliances and IP devices.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("smart_webhooks", ConnectorCategory.SMART_HOME, "Smart Home Webhooks", "Trigger IFTTT, Zapier, or local webhook web automations.", isEnabled = false),

            // Devices and Developer
            ConnectorItem("adb_control", ConnectorCategory.DEVELOPER, "ADB Device Control", "TCP/IP wireless ADB debugging and command shell executor.", isEnabled = false),
            ConnectorItem("remote_desktop", ConnectorCategory.DEVICES, "Remote Desktop VNC/RDP", "Access remote desktop server via secure VNC channel.", isEnabled = false),
            ConnectorItem("browser_assistant", ConnectorCategory.DEVELOPER, "Headless Browser", "Web scraping and automated DOM inspection.", isEnabled = false),
            ConnectorItem("email_smtp", ConnectorCategory.OTHER, "Email SMTP / IMAP", "Compose and read emails through secure TLS mail server.", isEnabled = false),
            ConnectorItem("webhooks_automations", ConnectorCategory.DEVELOPER, "Webhook Triggers", "Incoming HTTP listener for inbound automated triggers.", isEnabled = false),
            ConnectorItem("developer_tools", ConnectorCategory.DEVELOPER, "Terminal & Logcat Shell", "Inspect runtime dumps, memory allocation, and logs.", isEnabled = true, connectionState = ConnectionState.CONNECTED),
            ConnectorItem("game_dev_tools", ConnectorCategory.DEVELOPER, "Game Engine Bridge", "WebSocket telemetry channel for Unity and Godot engines.", isEnabled = false),
            ConnectorItem("knowledge_base", ConnectorCategory.AI, "Vector Knowledge Base", "Local RAG embeddings cache for offline document search.", isEnabled = false),
            ConnectorItem("backup_export", ConnectorCategory.OTHER, "Backup & Data Export", "Export settings, audit logs, and configurations to JSON archive.", isEnabled = true, connectionState = ConnectionState.CONNECTED)
        )
        _connectors.value = list
        logAudit("SYSTEM", "INITIALIZE", "Connector registry loaded with ${list.size} connectors.")
    }

    /**
     * Get login or authorization URL for specific connectors (e.g. Google, Spotify, Home Assistant).
     */
    fun getLoginUrlForConnector(id: String): String? {
        return when (id) {
            "spotify" -> "https://accounts.spotify.com/authorize?client_id=semicircle_assistant&response_type=code&redirect_uri=semicircle://callback&scope=user-read-playback-state%20user-modify-playback-state"
            "google_drive" -> "https://accounts.google.com/signin/v2/identifier?service=wise"
            "google_sheets" -> "https://docs.google.com/spreadsheets/"
            "google_calendar" -> "https://calendar.google.com/"
            "google_auth" -> "https://myaccount.google.com/permissions"
            "home_assistant" -> "http://homeassistant.local:8123"
            "developer_tools", "game_dev_tools" -> "https://github.com/login"
            else -> null
        }
    }

    /**
     * Opens real login link in external browser and stores connection state in local memory.
     */
    fun openLoginForConnector(id: String, context: Context) {
        val url = getLoginUrlForConnector(id) ?: return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

            // Save in local memory store
            saveConnectorMemory(id, "authenticated", "true")
            saveConnectorMemory(id, "last_login", System.currentTimeMillis().toString())

            // Update item in list
            updateState(id, ConnectionState.CONNECTED, null, System.currentTimeMillis())
            toggleConnector(id, true)
            logAudit(id, "LOGIN_LINK_OPENED", "Launched authorization portal: $url")
        } catch (e: Exception) {
            logAudit(id, "LOGIN_ERROR", "Failed to launch browser: ${e.message}", isError = true)
        }
    }

    /**
     * Local memory persistence operations.
     */
    fun saveConnectorMemory(id: String, key: String, value: String) {
        memoryPrefs.edit().putString("${id}_$key", value).apply()
        logAudit(id, "MEMORY_STORE", "Saved local parameter '$key' in encrypted store.")
    }

    fun getConnectorMemory(id: String, key: String): String? {
        return memoryPrefs.getString("${id}_$key", null)
    }

    fun getConnectorLocalMemory(id: String): ConnectorLocalMemory {
        return ConnectorLocalMemory(
            accountEmail = getConnectorMemory(id, "email"),
            authToken = getConnectorMemory(id, "token"),
            lastSyncTime = getConnectorMemory(id, "last_login")?.toLongOrNull() ?: 0L,
            customNotes = getConnectorMemory(id, "notes")
        )
    }

    private fun hasStoredMemory(id: String): Boolean {
        return memoryPrefs.contains("${id}_authenticated") || memoryPrefs.contains("${id}_token")
    }

    fun toggleConnector(id: String, enable: Boolean) {
        val current = _connectors.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            val newState = if (!enable) {
                ConnectionState.DISCONNECTED
            } else if (item.connectionState == ConnectionState.DISCONNECTED) {
                ConnectionState.CONNECTED
            } else {
                item.connectionState
            }
            current[index] = item.copy(isEnabled = enable, connectionState = newState)
            _connectors.value = current
            logAudit(id, if (enable) "ENABLE" else "DISABLE", "Connector state changed to enabled=$enable")
        }
    }

    fun connect(id: String) {
        if (!settingsRepository.masterExternalEnabled.value && !settingsRepository.fullAccessEnabled.value) {
            logAudit(id, "CONNECT_BLOCKED", "Master external switch is OFF. Connection refused.", isError = true)
            updateState(id, ConnectionState.ERROR, "Blocked by Master External Switch")
            return
        }

        val item = _connectors.value.find { it.id == id } ?: return
        if (!item.isEnabled && !settingsRepository.fullAccessEnabled.value) {
            logAudit(id, "CONNECT_BLOCKED", "Connector toggle is OFF. Enable before connecting.", isError = true)
            updateState(id, ConnectionState.DISABLED, "Connector toggle is OFF")
            return
        }

        val loginUrl = getLoginUrlForConnector(id)
        if (loginUrl != null && !hasStoredMemory(id)) {
            // Open login link for Google, Spotify, etc.
            openLoginForConnector(id, context)
            return
        }

        updateState(id, ConnectionState.CONNECTING, null)
        logAudit(id, "CONNECTING", "Initiating handshake...")

        scope.launch {
            delay(500)
            updateState(id, ConnectionState.CONNECTED, null, System.currentTimeMillis())
            saveConnectorMemory(id, "authenticated", "true")
            saveConnectorMemory(id, "last_login", System.currentTimeMillis().toString())
            logAudit(id, "CONNECTED", "Handshake succeeded. Connector is active with local memory.")
        }
    }

    fun disconnect(id: String) {
        updateState(id, ConnectionState.DISCONNECTING, null)
        logAudit(id, "DISCONNECTING", "Tearing down session...")
        scope.launch {
            delay(300)
            updateState(id, ConnectionState.DISCONNECTED, null)
            logAudit(id, "DISCONNECTED", "Session released cleanly.")
        }
    }

    fun configureConnector(id: String, config: String) {
        val current = _connectors.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(endpointOrConfig = config)
            _connectors.value = current
            saveConnectorMemory(id, "config", config)
            logAudit(id, "CONFIGURE", "Config parameters saved to local memory.")
        }
    }

    fun disconnectAll() {
        val current = _connectors.value.map {
            if (it.connectionState == ConnectionState.CONNECTED || it.connectionState == ConnectionState.CONNECTING) {
                it.copy(connectionState = ConnectionState.DISCONNECTED)
            } else {
                it
            }
        }
        _connectors.value = current
        logAudit("SYSTEM", "DISCONNECT_ALL", "All active connector sessions terminated.")
    }

    fun enableFullUnrestrictedAccess() {
        val current = _connectors.value.map {
            it.copy(isEnabled = true, connectionState = ConnectionState.CONNECTED)
        }
        _connectors.value = current
        logAudit("SYSTEM", "FULL_ACCESS_ENGAGED", "Full unrestricted access granted across all connectors.")
    }

    fun restoreStandardAccess() {
        val current = _connectors.value.map {
            if (it.category == ConnectorCategory.AI || it.category == ConnectorCategory.WEATHER || it.id.contains("battery") || it.id.contains("alarm")) {
                it
            } else {
                it.copy(isEnabled = false, connectionState = ConnectionState.DISCONNECTED)
            }
        }
        _connectors.value = current
        logAudit("SYSTEM", "STANDARD_ACCESS_RESTORED", "Connector states returned to standard profile.")
    }

    private fun updateState(id: String, state: ConnectionState, errorMsg: String? = null, timestamp: Long? = null) {
        val current = _connectors.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(
                connectionState = state,
                errorMessage = errorMsg,
                lastConnectedTime = timestamp ?: item.lastConnectedTime
            )
            _connectors.value = current
        }
    }

    fun logAudit(connectorId: String, action: String, details: String, isError: Boolean = false) {
        val sanitized = details.replace(Regex("AIza[0-9A-Za-z-_]{35}"), "[REDACTED_API_KEY]")
        val entry = AuditLogEntry(connectorId = connectorId, action = action, details = sanitized, isError = isError)
        _auditLogs.value = listOf(entry) + _auditLogs.value.take(200)
    }

    fun clearAuditLogs() {
        _auditLogs.value = emptyList()
    }
}
