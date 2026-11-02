package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.battery.BatterySentinel
import com.example.manager.ApiKeyManager
import com.example.manager.ConnectorManager
import com.example.manager.SettingsRepository
import com.example.ui.components.PermissionGatewayDialog
import com.example.ui.screens.ActivityLogsScreen
import com.example.ui.screens.ConnectorsScreen
import com.example.ui.screens.DeviceManagerScreen
import com.example.ui.screens.FullScreenFaceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageGeneratorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.SemicircleTheme
import com.example.ui.theme.TerminalBackground
import com.example.vision.FaceTrackingAnalyzer
import com.example.voice.AssistantVoiceState
import com.example.voice.VoiceAssistantManager
import com.example.weather.model.GeocodingResult
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

import com.example.manager.PtzCameraManager
import kotlinx.coroutines.flow.collectLatest

enum class AppNavScreen {
    HOME,
    FULL_SCREEN_FACE,
    CONNECTORS,
    WEATHER,
    IMAGE_GEN,
    DEVICES,
    SETTINGS,
    LOGS
}

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var apiKeyManager: ApiKeyManager
    private lateinit var connectorManager: ConnectorManager
    private lateinit var ptzCameraManager: PtzCameraManager
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var voiceAssistantManager: VoiceAssistantManager
    private lateinit var batterySentinel: BatterySentinel

    private val faceTrackingAnalyzer = FaceTrackingAnalyzer()
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepository = SettingsRepository(applicationContext)
        apiKeyManager = ApiKeyManager(applicationContext)
        connectorManager = ConnectorManager(applicationContext, settingsRepository)
        ptzCameraManager = PtzCameraManager(connectorManager)
        weatherRepository = WeatherRepository(applicationContext)
        voiceAssistantManager = VoiceAssistantManager(applicationContext, lifecycleScope, connectorManager, apiKeyManager, settingsRepository, ptzCameraManager)
        batterySentinel = BatterySentinel(applicationContext, lifecycleScope)

        // Initial weather load for Tamil Nadu / GPS location
        lifecycleScope.launch {
            weatherRepository.refreshWithExactLocation()
        }

        // Camera hardware toggle observer
        lifecycleScope.launch {
            settingsRepository.cameraEnabled.collectLatest { enabled ->
                if (enabled) {
                    bindFrontCameraAnalysis()
                } else {
                    try {
                        val provider = ProcessCameraProvider.getInstance(this@MainActivity).get()
                        provider.unbindAll()
                    } catch (_: Exception) {}
                }
            }
        }

        // Face Detection Auto-Activate AI observer ("force turn fact to activated ai")
        lifecycleScope.launch {
            var lastDetected = false
            faceTrackingAnalyzer.isFaceDetected.collectLatest { detected ->
                if (detected && !lastDetected) {
                    if (settingsRepository.faceAutoActivateAiEnabled.value) {
                        voiceAssistantManager.triggerFaceGreetOrActivate()
                    }
                }
                lastDetected = detected
            }
        }

        // Start background simulated face saccades / battery sentinel monitoring loop
        lifecycleScope.launch(Dispatchers.Default) {
            var phase = 0f
            while (isActive) {
                // If camera face is not detected, keep eyes centered perfectly
                if (!faceTrackingAnalyzer.isFaceDetected.value) {
                    faceTrackingAnalyzer.setSimulatedPosition(0f, 0f, detected = false)
                }

                // Check battery sentinel conditions
                val isSpeakingOrListening = voiceAssistantManager.voiceState.value == AssistantVoiceState.SPEAKING ||
                        voiceAssistantManager.voiceState.value == AssistantVoiceState.LISTENING

                batterySentinel.checkAndTriggerSadReminder(
                    isFaceDetected = faceTrackingAnalyzer.isFaceDetected.value,
                    isConversationActive = isSpeakingOrListening
                ) { alertMsg ->
                    connectorManager.logAudit("BATTERY_SENTINEL", "LOW_POWER_ALERT", alertMsg, isError = true)
                }

                delay(200)
            }
        }

        setContent {
            val permanentWakeLock by settingsRepository.permanentWakeLock.collectAsStateWithLifecycle()

            // Handle permanent wake lock flag
            DisposableEffect(permanentWakeLock) {
                if (permanentWakeLock) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            SemicircleTheme {
                MainAppScaffold()
            }
        }
    }

    @Composable
    private fun MainAppScaffold() {
        var currentScreen by remember { mutableStateOf(AppNavScreen.HOME) }
        val scope = rememberCoroutineScope()

        // State collection
        val targetX by faceTrackingAnalyzer.targetX.collectAsStateWithLifecycle()
        val targetY by faceTrackingAnalyzer.targetY.collectAsStateWithLifecycle()
        val isFaceDetected by faceTrackingAnalyzer.isFaceDetected.collectAsStateWithLifecycle()

        val voiceState by voiceAssistantManager.voiceState.collectAsStateWithLifecycle()
        val subtitles by voiceAssistantManager.liveSubtitles.collectAsStateWithLifecycle()
        val audioAmplitude by voiceAssistantManager.audioAmplitude.collectAsStateWithLifecycle()

        val weatherState by weatherRepository.weatherState.collectAsStateWithLifecycle()
        val isWeatherLoading by weatherRepository.isLoading.collectAsStateWithLifecycle()
        val weatherError by weatherRepository.errorMessage.collectAsStateWithLifecycle()

        val isLowBatterySad by batterySentinel.isLowBatterySadActive.collectAsStateWithLifecycle()
        val batteryLevel by batterySentinel.batteryLevel.collectAsStateWithLifecycle()
        val isCharging by batterySentinel.isCharging.collectAsStateWithLifecycle()

        val connectors by connectorManager.connectors.collectAsStateWithLifecycle()
        val auditLogs by connectorManager.auditLogs.collectAsStateWithLifecycle()

        val masterExternal by settingsRepository.masterExternalEnabled.collectAsStateWithLifecycle()
        val fullAccess by settingsRepository.fullAccessEnabled.collectAsStateWithLifecycle()
        val startOnBoot by settingsRepository.startOnBoot.collectAsStateWithLifecycle()
        val permanentWakeLock by settingsRepository.permanentWakeLock.collectAsStateWithLifecycle()
        val tempUnit by settingsRepository.temperatureUnit.collectAsStateWithLifecycle()
        val faceTrackingEnabled by settingsRepository.faceTrackingEnabled.collectAsStateWithLifecycle()
        val assistantLang by settingsRepository.assistantLanguage.collectAsStateWithLifecycle()
        val layoutStyle by settingsRepository.layoutStyle.collectAsStateWithLifecycle()

        var searchResults by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }

        // Sensitive Action Permission Gateway Modal State
        var pendingPermissionRequest by remember { mutableStateOf<Triple<String, String, String>?>(null) }

        // Request runtime permissions for Camera, Microphone, and Location
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val cameraGranted = perms[Manifest.permission.CAMERA] ?: false
            if (cameraGranted && faceTrackingEnabled) {
                bindFrontCameraAnalysis()
            }
        }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        LaunchedEffect(faceTrackingEnabled) {
            if (faceTrackingEnabled) {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    bindFrontCameraAnalysis()
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(TerminalBackground)
            ) {
                when (currentScreen) {
                    AppNavScreen.HOME -> {
                        HomeScreen(
                            targetX = targetX,
                            targetY = targetY,
                            isFaceDetected = isFaceDetected,
                            audioAmplitude = audioAmplitude,
                            voiceState = voiceState,
                            isLowBatterySad = isLowBatterySad,
                            batteryLevel = batteryLevel,
                            isCharging = isCharging,
                            subtitles = subtitles,
                            weather = weatherState,
                            temperatureUnit = tempUnit,
                            connectors = connectors,
                            fullAccessEnabled = fullAccess,
                            startOnBootEnabled = startOnBoot,
                            permanentWakeLock = permanentWakeLock,
                            currentLayoutStyle = layoutStyle,
                            assistantLanguage = assistantLang,
                            onSelectLanguage = { settingsRepository.setAssistantLanguage(it) },
                            onSelectLayoutStyle = { settingsRepository.setLayoutStyle(it) },
                            onToggleFullAccess = { enable ->
                                settingsRepository.setFullAccessEnabled(enable)
                                if (enable) connectorManager.enableFullUnrestrictedAccess()
                                else connectorManager.restoreStandardAccess()
                            },
                            onToggleStartOnBoot = { settingsRepository.setStartOnBoot(it) },
                            onActivateVoice = { voiceAssistantManager.triggerActivateOrInterrupt() },
                            onQueryVoice = { query -> voiceAssistantManager.processUserQuery(query) },
                            onNavigateToFullScreen = { currentScreen = AppNavScreen.FULL_SCREEN_FACE },
                            onNavigateToWeather = { currentScreen = AppNavScreen.WEATHER },
                            onNavigateToConnectors = { currentScreen = AppNavScreen.CONNECTORS },
                            onNavigateToImageGen = { currentScreen = AppNavScreen.IMAGE_GEN },
                            onNavigateToDevices = { currentScreen = AppNavScreen.DEVICES },
                            onNavigateToSettings = { currentScreen = AppNavScreen.SETTINGS }
                        )
                    }

                    AppNavScreen.FULL_SCREEN_FACE -> {
                        FullScreenFaceScreen(
                            targetX = targetX,
                            targetY = targetY,
                            isFaceDetected = isFaceDetected,
                            audioAmplitude = audioAmplitude,
                            voiceState = voiceState,
                            isLowBatterySad = isLowBatterySad,
                            batteryLevel = batteryLevel,
                            isCharging = isCharging,
                            subtitles = subtitles,
                            weatherCondition = weatherState?.conditionType ?: com.example.weather.model.WeatherConditionType.CLEAR,
                            onActivateOrInterrupt = { voiceAssistantManager.triggerActivateOrInterrupt() },
                            onQueryVoice = { query -> voiceAssistantManager.processUserQuery(query) },
                            onExitFullScreen = { currentScreen = AppNavScreen.HOME }
                        )
                    }

                    AppNavScreen.CONNECTORS -> {
                        ConnectorsScreen(
                            connectors = connectors,
                            masterSwitchEnabled = masterExternal,
                            onToggleMasterSwitch = { settingsRepository.setMasterExternalEnabled(it) },
                            onToggleConnector = { id, enabled -> connectorManager.toggleConnector(id, enabled) },
                            onConnect = { id ->
                                val item = connectors.find { it.id == id }
                                if (item?.requiresSensitivePermission == true && !fullAccess) {
                                    pendingPermissionRequest = Triple(item.displayName, "CONNECT_AND_AUTHORIZE", item.permissionName ?: "System Access")
                                } else {
                                    connectorManager.connect(id)
                                }
                            },
                            onOpenLogin = { id -> connectorManager.openLoginForConnector(id, applicationContext) },
                            onDisconnect = { id -> connectorManager.disconnect(id) },
                            onConfigure = { id, cfg -> connectorManager.configureConnector(id, cfg) },
                            onDisconnectAll = { connectorManager.disconnectAll() },
                            ptzCameraManager = ptzCameraManager,
                            onBack = { currentScreen = AppNavScreen.HOME }
                        )
                    }

                    AppNavScreen.WEATHER -> {
                        WeatherScreen(
                            weather = weatherState,
                            isLoading = isWeatherLoading,
                            errorMessage = weatherError,
                            temperatureUnit = tempUnit,
                            tamilNaduCities = weatherRepository.tamilNaduPopularCities,
                            onRefreshExactGps = {
                                scope.launch { weatherRepository.refreshWithExactLocation() }
                            },
                            onSelectCity = { city ->
                                scope.launch { weatherRepository.fetchWeatherForCity(city) }
                            },
                            onSearchQuery = { query ->
                                scope.launch {
                                    searchResults = weatherRepository.searchCities(query)
                                }
                            },
                            searchResults = searchResults,
                            onToggleUnit = { settingsRepository.setTemperatureUnit(it) },
                            onBack = { currentScreen = AppNavScreen.HOME }
                        )
                    }

                    AppNavScreen.IMAGE_GEN -> {
                        ImageGeneratorScreen(
                            onBack = { currentScreen = AppNavScreen.HOME }
                        )
                    }

                    AppNavScreen.DEVICES -> {
                        DeviceManagerScreen(
                            onBack = { currentScreen = AppNavScreen.HOME }
                        )
                    }

                    AppNavScreen.SETTINGS -> {
                        SettingsScreen(
                            settingsRepository = settingsRepository,
                            apiKeyManager = apiKeyManager,
                            voiceAssistantManager = voiceAssistantManager,
                            onNavigateToLogs = { currentScreen = AppNavScreen.LOGS },
                            onBack = { currentScreen = AppNavScreen.HOME }
                        )
                    }

                    AppNavScreen.LOGS -> {
                        ActivityLogsScreen(
                            logs = auditLogs,
                            onClearLogs = { connectorManager.clearAuditLogs() },
                            onBack = { currentScreen = AppNavScreen.SETTINGS }
                        )
                    }
                }

                // Safety Permission Gateway Dialog
                if (pendingPermissionRequest != null) {
                    val (conn, action, target) = pendingPermissionRequest!!
                    PermissionGatewayDialog(
                        connectorName = conn,
                        actionName = action,
                        targetResource = target,
                        onAllowOnce = {
                            val connItem = connectors.find { it.displayName == conn }
                            connItem?.let { connectorManager.connect(it.id) }
                            pendingPermissionRequest = null
                        },
                        onDeny = {
                            connectorManager.logAudit(conn, "PERMISSION_DENIED", "User denied sensitive action.", isError = true)
                            pendingPermissionRequest = null
                        },
                        onCancel = {
                            pendingPermissionRequest = null
                        }
                    )
                }
            }
        }
    }

    private fun bindFrontCameraAnalysis() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, faceTrackingAnalyzer)
                    }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
            } catch (e: Exception) {
                // If front camera is busy or unavailable, simulated saccades remain active smoothly
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceAssistantManager.cleanup()
        batterySentinel.cleanup()
        cameraExecutor.shutdown()
    }
}
