package com.example.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLayoutStyle(val title: String, val description: String) {
    CYBER_TERMINAL("Cyber Terminal", "Full futuristic orange HUD with central face and telemetry"),
    TACTICAL_HUD("Tactical HUD", "Angular combat telemetry layout with dual diagnostics"),
    MINIMAL_FACE("Minimal Face", "Clean high-focus robot presence with floating controls"),
    DOCK_STATION("Dock Station", "Split-pane workstation layout for desk stands")
}

enum class AssistantLanguage(val code: String, val label: String) {
    AUTO("auto", "Auto (தமிழ் & English)"),
    TAMIL("ta-IN", "தமிழ் (Tamil)"),
    ENGLISH("en-IN", "English")
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("semicircle_settings", Context.MODE_PRIVATE)

    private val _layoutStyle = MutableStateFlow(
        AppLayoutStyle.valueOf(prefs.getString("layout_style", AppLayoutStyle.CYBER_TERMINAL.name) ?: AppLayoutStyle.CYBER_TERMINAL.name)
    )
    val layoutStyle: StateFlow<AppLayoutStyle> = _layoutStyle.asStateFlow()

    private val _masterExternalEnabled = MutableStateFlow(prefs.getBoolean("master_external_enabled", true))
    val masterExternalEnabled: StateFlow<Boolean> = _masterExternalEnabled.asStateFlow()

    private val _fullAccessEnabled = MutableStateFlow(prefs.getBoolean("full_access_enabled", false))
    val fullAccessEnabled: StateFlow<Boolean> = _fullAccessEnabled.asStateFlow()

    private val _startOnBoot = MutableStateFlow(prefs.getBoolean("start_on_boot", true))
    val startOnBoot: StateFlow<Boolean> = _startOnBoot.asStateFlow()

    private val _permanentWakeLock = MutableStateFlow(prefs.getBoolean("permanent_wake_lock", true))
    val permanentWakeLock: StateFlow<Boolean> = _permanentWakeLock.asStateFlow()

    private val _faceTrackingEnabled = MutableStateFlow(prefs.getBoolean("face_tracking_enabled", true))
    val faceTrackingEnabled: StateFlow<Boolean> = _faceTrackingEnabled.asStateFlow()

    private val _temperatureUnit = MutableStateFlow(prefs.getString("temperature_unit", "C") ?: "C")
    val temperatureUnit: StateFlow<String> = _temperatureUnit.asStateFlow()

    private val _useExactLocation = MutableStateFlow(prefs.getBoolean("use_exact_location", true))
    val useExactLocation: StateFlow<Boolean> = _useExactLocation.asStateFlow()

    private val _assistantLanguage = MutableStateFlow(
        AssistantLanguage.valueOf(prefs.getString("assistant_language", AssistantLanguage.AUTO.name) ?: AssistantLanguage.AUTO.name)
    )
    val assistantLanguage: StateFlow<AssistantLanguage> = _assistantLanguage.asStateFlow()

    fun setAssistantLanguage(lang: AssistantLanguage) {
        _assistantLanguage.value = lang
        prefs.edit().putString("assistant_language", lang.name).apply()
    }

    fun setLayoutStyle(style: AppLayoutStyle) {
        _layoutStyle.value = style
        prefs.edit().putString("layout_style", style.name).apply()
    }

    fun setMasterExternalEnabled(enabled: Boolean) {
        _masterExternalEnabled.value = enabled
        prefs.edit().putBoolean("master_external_enabled", enabled).apply()
    }

    fun setFullAccessEnabled(enabled: Boolean) {
        _fullAccessEnabled.value = enabled
        prefs.edit().putBoolean("full_access_enabled", enabled).apply()
    }

    fun setStartOnBoot(enabled: Boolean) {
        _startOnBoot.value = enabled
        prefs.edit().putBoolean("start_on_boot", enabled).apply()
    }

    fun setPermanentWakeLock(enabled: Boolean) {
        _permanentWakeLock.value = enabled
        prefs.edit().putBoolean("permanent_wake_lock", enabled).apply()
    }

    fun setFaceTrackingEnabled(enabled: Boolean) {
        _faceTrackingEnabled.value = enabled
        prefs.edit().putBoolean("face_tracking_enabled", enabled).apply()
    }

    fun setTemperatureUnit(unit: String) {
        _temperatureUnit.value = unit
        prefs.edit().putString("temperature_unit", unit).apply()
    }

    private val _cameraEnabled = MutableStateFlow(prefs.getBoolean("camera_enabled", true))
    val cameraEnabled: StateFlow<Boolean> = _cameraEnabled.asStateFlow()

    private val _faceAutoActivateAiEnabled = MutableStateFlow(prefs.getBoolean("face_auto_activate_ai_enabled", false))
    val faceAutoActivateAiEnabled: StateFlow<Boolean> = _faceAutoActivateAiEnabled.asStateFlow()

    private val _wakeWordEnabled = MutableStateFlow(prefs.getBoolean("wake_word_enabled", true))
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    fun setCameraEnabled(enabled: Boolean) {
        _cameraEnabled.value = enabled
        prefs.edit().putBoolean("camera_enabled", enabled).apply()
    }

    fun setFaceAutoActivateAiEnabled(enabled: Boolean) {
        _faceAutoActivateAiEnabled.value = enabled
        prefs.edit().putBoolean("face_auto_activate_ai_enabled", enabled).apply()
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        _wakeWordEnabled.value = enabled
        prefs.edit().putBoolean("wake_word_enabled", enabled).apply()
    }

    private val _hapticVibrationEnabled = MutableStateFlow(prefs.getBoolean("haptic_vibration_enabled", true))
    val hapticVibrationEnabled: StateFlow<Boolean> = _hapticVibrationEnabled.asStateFlow()

    fun setHapticVibrationEnabled(enabled: Boolean) {
        _hapticVibrationEnabled.value = enabled
        prefs.edit().putBoolean("haptic_vibration_enabled", enabled).apply()
    }

    fun setUseExactLocation(enabled: Boolean) {
        _useExactLocation.value = enabled
        prefs.edit().putBoolean("use_exact_location", enabled).apply()
    }
}
