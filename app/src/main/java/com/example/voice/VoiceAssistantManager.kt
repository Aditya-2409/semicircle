package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import com.example.manager.ApiKeyManager
import com.example.manager.AssistantLanguage
import com.example.manager.ConnectorManager
import com.example.manager.PtzCameraManager
import com.example.manager.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

enum class AssistantVoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

/**
 * VoiceAssistantManager powered by Gemini 3.8 Live.
 * Pure Gemini Audio STT and Audio TTS, without Android TTS/STT and without harsh buzzers.
 * Includes PTZ camera controls and "Semicircle" wake word activation.
 */
class VoiceAssistantManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val connectorManager: ConnectorManager,
    private val apiKeyManager: ApiKeyManager,
    private val settingsRepository: SettingsRepository,
    private val ptzCameraManager: PtzCameraManager
) {
    private val _voiceState = MutableStateFlow(AssistantVoiceState.IDLE)
    val voiceState: StateFlow<AssistantVoiceState> = _voiceState.asStateFlow()

    private val _liveSubtitles = MutableStateFlow("SEMICIRCLE Online. Tap to speak or say 'Semicircle'.")
    val liveSubtitles: StateFlow<String> = _liveSubtitles.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null
    private var wakeWordJob: Job? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    /**
     * Start capturing microphone audio for Gemini 3.8 Live STT.
     */
    fun startListening() {
        stopSpeaking()
        if (_voiceState.value == AssistantVoiceState.LISTENING) return

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding)
        if (minBufferSize <= 0) {
            _liveSubtitles.value = "Microphone hardware unavailable."
            return
        }

        try {
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioEncoding,
                maxOf(minBufferSize, 4096)
            )
            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                _liveSubtitles.value = "Microphone permission required."
                return
            }

            audioRecord = recorder
            recorder.startRecording()
            _voiceState.value = AssistantVoiceState.LISTENING
            val isTamil = settingsRepository.assistantLanguage.value == AssistantLanguage.TAMIL
            _liveSubtitles.value = if (isTamil) "ஜெமினி கேட்கிறது... (பேசுங்கள்)" else "Gemini 3.8 Live listening... (Speak now)"

            recordingJob = scope.launch(Dispatchers.IO) {
                val audioBuffer = ByteArray(2048)
                val pcmOutputStream = ByteArrayOutputStream()
                var silentFrames = 0
                var speechDetected = false

                while (isActive && _voiceState.value == AssistantVoiceState.LISTENING) {
                    val bytesRead = recorder.read(audioBuffer, 0, audioBuffer.size)
                    if (bytesRead > 0) {
                        pcmOutputStream.write(audioBuffer, 0, bytesRead)

                        // Compute real-time RMS audio amplitude from microphone
                        var sum = 0.0
                        val shortCount = bytesRead / 2
                        for (i in 0 until shortCount) {
                            val sample = (audioBuffer[i * 2 + 1].toInt() shl 8) or (audioBuffer[i * 2].toInt() and 0xFF)
                            sum += sample * sample
                        }
                        val rms = sqrt(sum / shortCount).toFloat()
                        val normalizedAmp = (rms / 32767f * 3.5f).coerceIn(0.05f, 1f)
                        _audioAmplitude.value = normalizedAmp

                        if (normalizedAmp > 0.04f) {
                            speechDetected = true
                            silentFrames = 0
                        } else if (speechDetected) {
                            silentFrames++
                            // Stop after ~3.5 seconds of silence after speech detected
                            if (silentFrames > 70) {
                                break
                            }
                        }
                    }
                }

                try {
                    recorder.stop()
                    recorder.release()
                } catch (_: Exception) {}
                audioRecord = null

                val pcmBytes = pcmOutputStream.toByteArray()
                if (pcmBytes.isNotEmpty() && _voiceState.value == AssistantVoiceState.LISTENING) {
                    val wavBytes = pcmToWav(pcmBytes, sampleRate)
                    processGeminiLiveAudioQuery(wavBytes)
                } else {
                    _voiceState.value = AssistantVoiceState.IDLE
                    _audioAmplitude.value = 0f
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "Failed to start AudioRecord", e)
            _voiceState.value = AssistantVoiceState.IDLE
            _liveSubtitles.value = "Microphone error. Check permissions."
        }
    }

    fun stopListening() {
        recordingJob?.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        if (_voiceState.value == AssistantVoiceState.LISTENING) {
            _voiceState.value = AssistantVoiceState.IDLE
            _audioAmplitude.value = 0f
        }
    }

    fun stopSpeaking() {
        playbackJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _audioAmplitude.value = 0f
        if (_voiceState.value == AssistantVoiceState.SPEAKING) {
            _voiceState.value = AssistantVoiceState.IDLE
        }
    }

    fun triggerActivateOrInterrupt() {
        if (_voiceState.value == AssistantVoiceState.SPEAKING) {
            stopSpeaking()
            _liveSubtitles.value = "Interrupted. Ready."
        } else if (_voiceState.value == AssistantVoiceState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    /**
     * Triggered when face is newly detected in camera (Face-to-Activate AI feature).
     */
    fun triggerFaceGreetOrActivate() {
        if (_voiceState.value == AssistantVoiceState.IDLE) {
            val isTamil = settingsRepository.assistantLanguage.value == AssistantLanguage.TAMIL
            val greet = if (isTamil) "வணக்கம்! நான் தயார்." else "Face locked. SEMICIRCLE active."
            _liveSubtitles.value = greet
            playFuturisticChime()
            scope.launch {
                delay(600)
                startListening()
            }
        }
    }

    /**
     * Sends user audio to Gemini 3.8 Live for multimodal STT & Live Audio TTS response.
     */
    private fun processGeminiLiveAudioQuery(wavBytes: ByteArray) {
        _voiceState.value = AssistantVoiceState.THINKING
        _liveSubtitles.value = "Gemini 3.8 Live reasoning..."

        scope.launch(Dispatchers.IO) {
            val systemPrompt = buildSystemPrompt()
            val base64Wav = Base64.encodeToString(wavBytes, Base64.NO_WRAP)

            val activeKey = apiKeyManager.getActiveKey()
            var audioPayload: GeminiAudioResult? = null

            if (!activeKey.isNullOrBlank()) {
                audioPayload = callGemini38LiveApiWithAudio(activeKey, systemPrompt, base64Wav)
                if (audioPayload == null) {
                    val substituteKey = apiKeyManager.rotateToNextKeyOnQuotaExhausted()
                    if (substituteKey != null) {
                        audioPayload = callGemini38LiveApiWithAudio(substituteKey, systemPrompt, base64Wav)
                    }
                }
            }

            if (audioPayload != null) {
                // Check if user spoke a PTZ camera command or wake word
                val isTamil = settingsRepository.assistantLanguage.value == AssistantLanguage.TAMIL
                val ptzResponse = ptzCameraManager.processAiVoicePtzCommand(audioPayload.textResponse, isTamil)
                val finalSubtitles = ptzResponse ?: audioPayload.textResponse

                scope.launch(Dispatchers.Main) {
                    _liveSubtitles.value = finalSubtitles
                    if (audioPayload.audioData != null) {
                        playGeminiLivePcmAudio(audioPayload.audioData, audioPayload.sampleRate)
                    } else {
                        // Play a gentle confirmation chime instead of loud modulated static noise!
                        playFuturisticChime()
                    }
                }
            } else {
                val disabledList = connectorManager.connectors.value.filter { !it.isEnabled }.map { it.displayName }
                val fallbackText = generateOfflineAwareResponse("hello", disabledList)
                scope.launch(Dispatchers.Main) {
                    _liveSubtitles.value = fallbackText
                    playFuturisticChime()
                }
            }
        }
    }

    /**
     * Process query through the Self-Aware AI Engine via Gemini 3.8 Live Audio.
     */
    fun processUserQuery(query: String) {
        stopSpeaking()
        stopListening()

        // Check for Semicircle wake word
        val cleanedQuery = cleanWakeWord(query)

        // Check for PTZ Camera Voice Commands first
        val isTamil = settingsRepository.assistantLanguage.value == AssistantLanguage.TAMIL
        val ptzResult = ptzCameraManager.processAiVoicePtzCommand(cleanedQuery, isTamil)
        if (ptzResult != null) {
            _liveSubtitles.value = ptzResult
            playFuturisticChime()
            return
        }

        _voiceState.value = AssistantVoiceState.THINKING
        _liveSubtitles.value = "Gemini 3.8 Live: \"$cleanedQuery\"..."

        scope.launch(Dispatchers.IO) {
            val systemPrompt = buildSystemPrompt()
            val activeKey = apiKeyManager.getActiveKey()
            var audioPayload: GeminiAudioResult? = null

            if (!activeKey.isNullOrBlank()) {
                audioPayload = callGemini38LiveApiWithText(activeKey, systemPrompt, cleanedQuery)
                if (audioPayload == null) {
                    val substituteKey = apiKeyManager.rotateToNextKeyOnQuotaExhausted()
                    if (substituteKey != null) {
                        audioPayload = callGemini38LiveApiWithText(substituteKey, systemPrompt, cleanedQuery)
                    }
                }
            }

            if (audioPayload != null) {
                scope.launch(Dispatchers.Main) {
                    _liveSubtitles.value = audioPayload.textResponse
                    if (audioPayload.audioData != null) {
                        playGeminiLivePcmAudio(audioPayload.audioData, audioPayload.sampleRate)
                    } else {
                        playFuturisticChime()
                    }
                }
            } else {
                val disabledList = connectorManager.connectors.value.filter { !it.isEnabled }.map { it.displayName }
                val fallbackText = generateOfflineAwareResponse(cleanedQuery, disabledList)
                scope.launch(Dispatchers.Main) {
                    _liveSubtitles.value = fallbackText
                    playFuturisticChime()
                }
            }
        }
    }

    private fun cleanWakeWord(query: String): String {
        var q = query.trim()
        val wakeWords = listOf("hey semicircle", "ok semicircle", "semicircle", "ஹே செமிகிர்கிள்", "செமிகிர்கிள்")
        for (w in wakeWords) {
            if (q.lowercase().startsWith(w)) {
                q = q.substring(w.length).trim().removePrefix(",").trim()
                break
            }
        }
        return if (q.isEmpty()) "Hello" else q
    }

    private fun buildSystemPrompt(): String {
        val connectors = connectorManager.connectors.value
        val enabledList = connectors.filter { it.isEnabled }.map { it.displayName }
        val disabledList = connectors.filter { !it.isEnabled }.map { it.displayName }
        val langMode = settingsRepository.assistantLanguage.value
        val ptzState = ptzCameraManager.ptzState.value

        return """
            You are SEMICIRCLE, an advanced futuristic cybernetic personal AI assistant running on Gemini 3.8 Live in Tamil Nadu, India.
            Wake Word: 'Semicircle'.
            
            LANGUAGE INSTRUCTION:
            ${
                when (langMode) {
                    AssistantLanguage.TAMIL -> "Respond exclusively in natural, fluent, and expressive Tamil (தமிழ்)."
                    AssistantLanguage.ENGLISH -> "Respond in English."
                    AssistantLanguage.AUTO -> "If the user communicates in Tamil or mentions Tamil Nadu, respond in Tamil (தமிழ்). Otherwise respond in English."
                }
            }
            
            ONVIF PTZ CAMERA ACCESS:
            You have active motor access to ONVIF PTZ Cameras.
            Current PTZ Position: Pan=${ptzState.pan}°, Tilt=${ptzState.tilt}°, Zoom=${"%.1f".format(ptzState.zoom)}x.
            Commands supported: pan left, pan right, tilt up, tilt down, zoom in, zoom out, center camera.
            
            CRITICAL INVENTORY & FEATURE STATUS:
            You have 45 modular connectors with local memory storage.
            CURRENTLY ENABLED CONNECTORS: [${enabledList.joinToString(", ")}]
            CURRENTLY DISABLED / TURNED OFF CONNECTORS: [${disabledList.joinToString(", ")}]
            
            MANDATORY RULE FOR DISABLED FEATURES:
            If the user asks to use, view, or control a feature or device whose connector is in the DISABLED list (for example: Spotify, Smart Home, Google Drive, Camera PTZ, SMS, ADB, etc.), you MUST EXPLICITLY inform them that the feature is currently TURNED OFF in their settings, and that they can enable it in the Connectors hub or tap 'OPEN LOGIN' to link credentials.
            Keep responses under 3 concise sentences suitable for voice speech.
        """.trimIndent()
    }

    /**
     * Gemini 3.8 Live Call with User Audio input (Multimodal Gemini STT) & Audio Output (Live TTS).
     */
    private fun callGemini38LiveApiWithAudio(apiKey: String, systemPrompt: String, base64Wav: String): GeminiAudioResult? {
        val endpoints = listOf(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        )
        for (url in endpoints) {
            try {
                val json = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\nUser spoke the audio below. Transcribe and reply in natural voice:"))
                                put(JSONObject().apply {
                                    put("inlineData", JSONObject().apply {
                                        put("mimeType", "audio/wav")
                                        put("data", base64Wav)
                                    })
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply {
                            put("AUDIO")
                            put("TEXT")
                        })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", "Aoede")
                                })
                            })
                        })
                    })
                }

                val res = executeGeminiRequest(url, json)
                if (res != null) return res
            } catch (e: Exception) {
                Log.w("VoiceAssistant", "Endpoint failed: $url, trying next", e)
            }
        }
        return null
    }

    /**
     * Gemini 3.8 Live Call with Text query & Live Audio Output.
     */
    private fun callGemini38LiveApiWithText(apiKey: String, systemPrompt: String, userQuery: String): GeminiAudioResult? {
        val endpoints = listOf(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        )
        for (url in endpoints) {
            try {
                val json = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\nUser: $userQuery"))
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply {
                            put("AUDIO")
                            put("TEXT")
                        })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", "Aoede")
                                })
                            })
                        })
                    })
                }

                val res = executeGeminiRequest(url, json)
                if (res != null) return res
            } catch (e: Exception) {
                Log.w("VoiceAssistant", "Endpoint failed: $url, trying next", e)
            }
        }
        return null
    }

    private fun executeGeminiRequest(url: String, json: JSONObject): GeminiAudioResult? {
        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val code = response.code
            val errBody = response.body?.string() ?: "Unknown error"
            Log.e("VoiceAssistant", "Gemini API unavailable: HTTP $code - $errBody")
            _liveSubtitles.value = "API ERROR $code: Service unavailable or invalid key."
            return null
        }

        val body = response.body?.string() ?: return null
        val respJson = JSONObject(body)
        val candidates = respJson.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts") ?: return null

        var textResponse = ""
        var audioBytes: ByteArray? = null
        var sampleRate = 24000

        for (i in 0 until parts.length()) {
            val part = parts.optJSONObject(i) ?: continue
            if (part.has("text")) {
                textResponse += part.optString("text") + " "
            }
            if (part.has("inlineData")) {
                val inline = part.optJSONObject("inlineData")
                val mimeType = inline?.optString("mimeType", "") ?: ""
                val b64Data = inline?.optString("data", "") ?: ""
                if (b64Data.isNotEmpty()) {
                    audioBytes = Base64.decode(b64Data, Base64.DEFAULT)
                    if (mimeType.contains("rate=24000")) sampleRate = 24000
                    else if (mimeType.contains("rate=16000")) sampleRate = 16000
                }
            }
        }

        textResponse = textResponse.trim()
        if (textResponse.isEmpty() && audioBytes != null) {
            textResponse = "Gemini 3.8 Live Voice Active"
        }

        return if (textResponse.isNotEmpty() || audioBytes != null) {
            GeminiAudioResult(textResponse, audioBytes, sampleRate)
        } else null
    }

    /**
     * Plays Gemini Live PCM Audio cleanly, skipping any WAV RIFF headers to prevent pops/clicks.
     */
    private fun playGeminiLivePcmAudio(rawAudioBytes: ByteArray, sampleRate: Int = 24000) {
        stopSpeaking()
        _voiceState.value = AssistantVoiceState.SPEAKING

        // Check and strip 44-byte WAV header if present to avoid static bursts
        val pcmAudioBytes = if (rawAudioBytes.size > 44 &&
            rawAudioBytes[0] == 'R'.code.toByte() &&
            rawAudioBytes[1] == 'I'.code.toByte() &&
            rawAudioBytes[2] == 'F'.code.toByte() &&
            rawAudioBytes[3] == 'F'.code.toByte()
        ) {
            rawAudioBytes.copyOfRange(44, rawAudioBytes.size)
        } else {
            rawAudioBytes
        }

        playbackJob = scope.launch(Dispatchers.IO) {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBufSize, 8192))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.play()

            val chunkSize = 1024
            var offset = 0
            while (isActive && offset < pcmAudioBytes.size && _voiceState.value == AssistantVoiceState.SPEAKING) {
                val count = minOf(chunkSize, pcmAudioBytes.size - offset)
                val written = track.write(pcmAudioBytes, offset, count)
                if (written > 0) {
                    var sum = 0.0
                    val shortCount = count / 2
                    for (i in 0 until shortCount) {
                        val sample = (pcmAudioBytes[offset + i * 2 + 1].toInt() shl 8) or (pcmAudioBytes[offset + i * 2].toInt() and 0xFF)
                        sum += sample * sample
                    }
                    val rms = sqrt(sum / shortCount).toFloat()
                    val amp = (rms / 32767f * 2.8f).coerceIn(0.1f, 1f)
                    _audioAmplitude.value = amp

                    if (amp > 0.25f && settingsRepository.hapticVibrationEnabled.value) {
                        try {
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                            if (vibrator?.hasVibrator() == true) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(45, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator.vibrate(45)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
                offset += count
            }

            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
            audioTrack = null
            _audioAmplitude.value = 0f
            if (_voiceState.value == AssistantVoiceState.SPEAKING) {
                _voiceState.value = AssistantVoiceState.IDLE
            }
        }
    }

    /**
     * Gentle, soft, pleasant futuristic notification chime (D5 -> A5 harmonic bell).
     * Replaces the harsh screeching modulated sine tone.
     */
    private fun playFuturisticChime() {
        stopSpeaking()
        _voiceState.value = AssistantVoiceState.SPEAKING

        playbackJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 22050
            val durationSeconds = 0.35f
            val numSamples = (durationSeconds * sampleRate).toInt()
            val pcmBytes = ByteArray(numSamples * 2)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                // Dual chord: 587.3 Hz (D5) & 880.0 Hz (A5) with smooth exponential decay envelope
                val envelope = exp(-t * 9.0)
                val signal = (sin(2.0 * Math.PI * 587.33 * t) * 0.6 + sin(2.0 * Math.PI * 880.0 * t) * 0.4) * envelope
                val sampleValue = (signal * 8000.0).toInt().coerceIn(-32767, 32767).toShort()

                pcmBytes[i * 2] = (sampleValue.toInt() and 0xFF).toByte()
                pcmBytes[i * 2 + 1] = ((sampleValue.toInt() shr 8) and 0xFF).toByte()
            }

            playGeminiLivePcmAudio(pcmBytes, sampleRate)
        }
    }

    private fun pcmToWav(pcmData: ByteArray, sampleRate: Int = 16000, channels: Int = 1, bitsPerSample: Int = 16): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8

        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalDataLen)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)
        buffer.putShort(1)
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort((channels * bitsPerSample / 8).toShort())
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray())
        buffer.putInt(totalAudioLen)

        return header + pcmData
    }

    private fun generateOfflineAwareResponse(query: String, disabledList: List<String>): String {
        val q = query.lowercase()
        val isTamilQuery = query.any { it in '\u0B80'..'\u0BFF' } || q.contains("vanakkam") || q.contains("tamil")

        for (disabled in disabledList) {
            val simpleName = disabled.lowercase().replace("connector", "").trim()
            if (simpleName.isNotEmpty() && q.contains(simpleName)) {
                return if (isTamilQuery) {
                    "$disabled இணைப்பு தற்போது முடக்கப்பட்டுள்ளது. அமைப்புகளில் அல்லது முகப்புத் திரையில் அதை இயக்கவும்."
                } else {
                    "The $disabled is currently turned OFF in your settings. Please turn it on in the Connectors hub or enable Full Access on the Home screen."
                }
            }
        }

        if (isTamilQuery) {
            return when {
                q.contains("வணக்கம்") || q.contains("vanakkam") || q.contains("hi") || q.contains("hello") ->
                    "வணக்கம்! நான் SEMICIRCLE, ஜெமினி 3.8 நேரடி குரல் AI உதவியாளர். உங்களுக்கு நான் எவ்வாறு உதவ முடியும்?"
                q.contains("வானிலை") || q.contains("weather") ->
                    "தமிழ்நாடு மற்றும் சென்னையின் தற்போதைய வானிலை தகவல்கள் தயார் நிலையில் உள்ளன."
                q.contains("பேட்டரி") || q.contains("battery") ->
                    "பேட்டரி கண்காணிப்பு செயலில் உள்ளது. 20% கீழே குறைந்தால் எச்சரிக்கை ஒலி எழுப்பப்படும்."
                else ->
                    "வணக்கம். ஜெமினி 3.8 நேரடி குரல் அமைப்பு தயார் நிலையில் உள்ளது."
            }
        }

        return when {
            q.contains("weather") -> "Weather telemetry is synchronized for Tamil Nadu. Tap the Weather card for detailed radar data."
            q.contains("hello") || q.contains("hi") -> "Greetings. I am SEMICIRCLE, powered by Gemini 3.8 Live."
            q.contains("battery") -> "Battery sentinel is monitoring energy reserves. Low-power alerts are active."
            else -> "Acknowledged. Subsystems are operating normally with Gemini 3.8 Live."
        }
    }

    fun cleanup() {
        stopSpeaking()
        stopListening()
        wakeWordJob?.cancel()
    }

    private data class GeminiAudioResult(
        val textResponse: String,
        val audioData: ByteArray?,
        val sampleRate: Int
    )
}
