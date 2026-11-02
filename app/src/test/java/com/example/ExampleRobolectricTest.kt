package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.manager.ApiKeyManager
import com.example.manager.AssistantLanguage
import com.example.manager.ConnectorManager
import com.example.manager.PtzCameraManager
import com.example.manager.SettingsRepository
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SEMICIRCLE", appName)
    }

    @Test
    fun `test assistant language settings persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsRepo = SettingsRepository(context)

        assertEquals(AssistantLanguage.AUTO, settingsRepo.assistantLanguage.value)

        settingsRepo.setAssistantLanguage(AssistantLanguage.TAMIL)
        assertEquals(AssistantLanguage.TAMIL, settingsRepo.assistantLanguage.value)

        // Verify persistence in SharedPreferences
        val reloadedRepo = SettingsRepository(context)
        assertEquals(AssistantLanguage.TAMIL, reloadedRepo.assistantLanguage.value)
    }

    @Test
    fun `test ptz camera controls and ai command execution`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsRepo = SettingsRepository(context)
        val connectorManager = ConnectorManager(context, settingsRepo)
        val ptzManager = PtzCameraManager(connectorManager)

        assertEquals(0, ptzManager.ptzState.value.pan)
        assertEquals(0, ptzManager.ptzState.value.tilt)

        // Test Pan Left
        ptzManager.panLeft(20)
        assertEquals(-20, ptzManager.ptzState.value.pan)

        // Test Zoom In
        ptzManager.zoomIn(1.0f)
        assertEquals(2.0f, ptzManager.ptzState.value.zoom)

        // Test AI Natural Voice Command
        val response = ptzManager.processAiVoicePtzCommand("pan right", isTamil = false)
        assertNotNull(response)
        assertTrue(response!!.contains("panned right"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test voice assistant manager gemini 3_8 live initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsRepo = SettingsRepository(context)
        val apiKeyManager = ApiKeyManager(context)
        val connectorManager = ConnectorManager(context, settingsRepo)
        val ptzManager = PtzCameraManager(connectorManager)
        val testScope = TestScope(UnconfinedTestDispatcher())

        val voiceAssistant = VoiceAssistantManager(
            context,
            testScope,
            connectorManager,
            apiKeyManager,
            settingsRepo,
            ptzManager
        )

        assertNotNull(voiceAssistant)
        assertNotNull(voiceAssistant.voiceState.value)
        assertNotNull(voiceAssistant.liveSubtitles.value)

        // Verify Tamil query process trigger
        voiceAssistant.processUserQuery("வணக்கம் SEMICIRCLE")
        assertNotNull(voiceAssistant.liveSubtitles.value)
    }
}
