package com.example.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.BatteryManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class BatterySentinel(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _isLowBatterySadActive = MutableStateFlow(false)
    val isLowBatterySadActive: StateFlow<Boolean> = _isLowBatterySadActive.asStateFlow()

    private var lastSadSoundTime = 0L

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                if (level >= 0 && scale > 0) {
                    val pct = (level * 100) / scale
                    _batteryLevel.value = pct
                    _isCharging.value = charging
                }
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }

    /**
     * Evaluates conditions:
     * Battery <= 20% AND not charging AND no face detected AND no conversation active.
     */
    fun checkAndTriggerSadReminder(
        isFaceDetected: Boolean,
        isConversationActive: Boolean,
        onTriggered: (message: String) -> Unit
    ) {
        val level = _batteryLevel.value
        val charging = _isCharging.value

        if (level <= 20 && !charging) {
            if (!isFaceDetected && !isConversationActive) {
                _isLowBatterySadActive.value = true
                val now = System.currentTimeMillis()
                // Throttle sound to once every 2 minutes
                if (now - lastSadSoundTime > 120_000L) {
                    lastSadSoundTime = now
                    scope.launch(Dispatchers.Default) {
                        playSynthesizedSadSound()
                    }
                    onTriggered("Low battery alert: Power level at $level%. Energy critical.")
                }
            } else {
                _isLowBatterySadActive.value = false
            }
        } else {
            _isLowBatterySadActive.value = false
        }
    }

    /**
     * Synthesizes a melancholy descending minor arpeggio (F4 -> D4 -> Bb3 -> G3)
     * using AudioTrack to create an authentic robotic sad sound without external assets.
     */
    private fun playSynthesizedSadSound() {
        try {
            val sampleRate = 44100
            val frequencies = doubleArrayOf(349.23, 293.66, 233.08, 196.00) // F4, D4, Bb3, G3
            val noteDurationMs = 280
            val numSamplesPerNote = (sampleRate * noteDurationMs) / 1000
            val totalSamples = numSamplesPerNote * frequencies.size
            val buffer = ShortArray(totalSamples)

            var sampleIdx = 0
            for (freq in frequencies) {
                for (i in 0 until numSamplesPerNote) {
                    val t = i.toDouble() / sampleRate
                    // Apply smooth envelope fade-in and fade-out to prevent audio pops
                    val envelope = sin(PI * (i.toDouble() / numSamplesPerNote)).toFloat()
                    val wave = sin(2.0 * PI * freq * t).toFloat()
                    val sample = (wave * envelope * Short.MAX_VALUE * 0.45f).toInt().toShort()
                    buffer[sampleIdx++] = sample
                }
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(totalSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, totalSamples)
            audioTrack.play()
        } catch (e: Exception) {
            Log.e("BatterySentinel", "Could not play synthesized sad sound", e)
        }
    }

    fun cleanup() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }
}
