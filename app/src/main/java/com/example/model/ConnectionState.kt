package com.example.model

enum class ConnectionState(val label: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
    DISCONNECTING("Disconnecting..."),
    ERROR("Error"),
    NEEDS_PERMISSION("Needs Permission"),
    NEEDS_AUTHENTICATION("Needs Auth"),
    DISABLED("Disabled")
}

enum class ConnectorCategory(val title: String) {
    ALL("All"),
    AI("AI & Lang"),
    WEATHER("Weather"),
    PHONE("Phone"),
    GOOGLE("Google"),
    MEDIA("Media"),
    CAMERAS("Cameras"),
    SMART_HOME("Smart Home"),
    DEVICES("Devices"),
    DEVELOPER("Developer"),
    OTHER("Other")
}

data class ConnectorItem(
    val id: String,
    val category: ConnectorCategory,
    val displayName: String,
    val description: String,
    val isEnabled: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val lastConnectedTime: Long? = null,
    val errorMessage: String? = null,
    val endpointOrConfig: String = "",
    val requiresSensitivePermission: Boolean = false,
    val permissionName: String? = null,
    val localMemory: String = "",
    val loginUrl: String? = null
)
