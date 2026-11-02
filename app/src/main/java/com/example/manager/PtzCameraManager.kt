package com.example.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PtzState(
    val pan: Int = 0,       // -100 (far left) to +100 (far right)
    val tilt: Int = 0,      // -100 (down) to +100 (up)
    val zoom: Float = 1.0f, // 1.0x to 5.0x
    val lastAction: String = "STANDBY"
)

class PtzCameraManager(
    private val connectorManager: ConnectorManager
) {
    private val _ptzState = MutableStateFlow(PtzState())
    val ptzState: StateFlow<PtzState> = _ptzState.asStateFlow()

    fun panLeft(step: Int = 15): PtzState {
        val newPan = (_ptzState.value.pan - step).coerceIn(-100, 100)
        val state = _ptzState.value.copy(pan = newPan, lastAction = "PAN LEFT -> $newPan°")
        _ptzState.value = state
        connectorManager.logAudit("PTZ_CAMERA", "PAN_LEFT", "Camera panned left to $newPan°")
        return state
    }

    fun panRight(step: Int = 15): PtzState {
        val newPan = (_ptzState.value.pan + step).coerceIn(-100, 100)
        val state = _ptzState.value.copy(pan = newPan, lastAction = "PAN RIGHT -> $newPan°")
        _ptzState.value = state
        connectorManager.logAudit("PTZ_CAMERA", "PAN_RIGHT", "Camera panned right to $newPan°")
        return state
    }

    fun tiltUp(step: Int = 15): PtzState {
        val newTilt = (_ptzState.value.tilt + step).coerceIn(-100, 100)
        val state = _ptzState.value.copy(tilt = newTilt, lastAction = "TILT UP -> $newTilt°")
        _ptzState.value = state
        connectorManager.logAudit("PTZ_CAMERA", "TILT_UP", "Camera tilted up to $newTilt°")
        return state
    }

    fun tiltDown(step: Int = 15): PtzState {
        val newTilt = (_ptzState.value.tilt - step).coerceIn(-100, 100)
        val state = _ptzState.value.copy(tilt = newTilt, lastAction = "TILT DOWN -> $newTilt°")
        _ptzState.value = state
        connectorManager.logAudit("PTZ_CAMERA", "TILT_DOWN", "Camera tilted down to $newTilt°")
        return state
    }

    fun zoomIn(step: Float = 0.5f): PtzState {
        val newZoom = (_ptzState.value.zoom + step).coerceIn(1.0f, 5.0f)
        val state = _ptzState.value.copy(zoom = newZoom, lastAction = "ZOOM IN -> ${"%.1f".format(newZoom)}x")
        _ptzState.value = state
        connectorManager.logAudit("PTZ_CAMERA", "ZOOM_IN", "Camera zoomed in to ${"%.1f".format(newZoom)}x")
        return state
    }

    fun zoomOut(step: Float = 0.5f): PtzState {
        val newZoom = (_ptzState.value.zoom - step).coerceIn(1.0f, 5.0f)
        val state = _ptzState.value.copy(zoom = newZoom, lastAction = "ZOOM OUT -> ${"%.1f".format(newZoom)}x")
        _ptzState.value = state
        connectorManager.logAudit("PTZ_CAMERA", "ZOOM_OUT", "Camera zoomed out to ${"%.1f".format(newZoom)}x")
        return state
    }

    fun resetCenter(): PtzState {
        val state = PtzState(pan = 0, tilt = 0, zoom = 1.0f, lastAction = "CENTER RESET")
        _ptzState.value = state
        connectorManager.logAudit("PTZ_CAMERA", "RESET_CENTER", "Camera returned to home origin 0°,0°,1.0x")
        return state
    }

    /**
     * Parse natural voice commands for PTZ and execute directly.
     */
    fun processAiVoicePtzCommand(query: String, isTamil: Boolean): String? {
        val q = query.lowercase()
        return when {
            q.contains("pan left") || q.contains("turn left") || q.contains("இடது") || q.contains("கேமரா இடது") -> {
                val s = panLeft()
                if (isTamil) "கேமரா இடதுபுறம் திருப்பப்பட்டது (${s.pan}°)." else "Camera panned left to ${s.pan} degrees."
            }
            q.contains("pan right") || q.contains("turn right") || q.contains("வலது") || q.contains("கேமரா வலது") -> {
                val s = panRight()
                if (isTamil) "கேமரா வலதுபுறம் திருப்பப்பட்டது (${s.pan}°)." else "Camera panned right to ${s.pan} degrees."
            }
            q.contains("tilt up") || q.contains("look up") || q.contains("மேலே") || q.contains("மேல்") -> {
                val s = tiltUp()
                if (isTamil) "கேமரா மேலே உயர்த்தப்பட்டது (${s.tilt}°)." else "Camera tilted up to ${s.tilt} degrees."
            }
            q.contains("tilt down") || q.contains("look down") || q.contains("கீழே") || q.contains("கீழ்") -> {
                val s = tiltDown()
                if (isTamil) "கேமரா கீழே இறக்கப்பட்டது (${s.tilt}°)." else "Camera tilted down to ${s.tilt} degrees."
            }
            q.contains("zoom in") || q.contains("ஜூம் இன்") || q.contains("பெரிதாக்கு") -> {
                val s = zoomIn()
                if (isTamil) "கேமரா ஜூம் செய்யப்பட்டது (${"%.1f".format(s.zoom)}x)." else "Camera zoomed in to ${"%.1f".format(s.zoom)}x."
            }
            q.contains("zoom out") || q.contains("ஜூம் அவுட்") || q.contains("சிறிதாக்கு") -> {
                val s = zoomOut()
                if (isTamil) "கேமரா ஜூம் அவுட் செய்யப்பட்டது (${"%.1f".format(s.zoom)}x)." else "Camera zoomed out to ${"%.1f".format(s.zoom)}x."
            }
            q.contains("center camera") || q.contains("reset camera") || q.contains("மத்தியில்") || q.contains("ரீசெட் கேமரா") -> {
                resetCenter()
                if (isTamil) "கேமரா ஆரம்ப நிலைக்கு திரும்பியது (0°, 0°)." else "Camera reset to center home position."
            }
            else -> null
        }
    }
}
