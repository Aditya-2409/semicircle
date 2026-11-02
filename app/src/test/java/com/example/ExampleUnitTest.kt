package com.example

import com.example.manager.AppLayoutStyle
import com.example.model.ConnectionState
import com.example.model.ConnectorCategory
import com.example.weather.model.WeatherConditionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAppLayoutStylesExist() {
        val styles = AppLayoutStyle.entries
        assertEquals(4, styles.size)
        assertTrue(styles.contains(AppLayoutStyle.CYBER_TERMINAL))
        assertTrue(styles.contains(AppLayoutStyle.TACTICAL_HUD))
        assertTrue(styles.contains(AppLayoutStyle.MINIMAL_FACE))
        assertTrue(styles.contains(AppLayoutStyle.DOCK_STATION))
    }

    @Test
    fun testConnectorCategories() {
        val categories = ConnectorCategory.entries
        assertTrue(categories.size >= 10)
        assertTrue(categories.contains(ConnectorCategory.AI))
        assertTrue(categories.contains(ConnectorCategory.WEATHER))
        assertTrue(categories.contains(ConnectorCategory.PHONE))
        assertTrue(categories.contains(ConnectorCategory.GOOGLE))
        assertTrue(categories.contains(ConnectorCategory.MEDIA))
        assertTrue(categories.contains(ConnectorCategory.CAMERAS))
        assertTrue(categories.contains(ConnectorCategory.SMART_HOME))
        assertTrue(categories.contains(ConnectorCategory.DEVICES))
        assertTrue(categories.contains(ConnectorCategory.DEVELOPER))
    }

    @Test
    fun testConnectionStateLabels() {
        assertEquals("Connected", ConnectionState.CONNECTED.label)
        assertEquals("Disconnected", ConnectionState.DISCONNECTED.label)
        assertEquals("Connecting...", ConnectionState.CONNECTING.label)
        assertEquals("Error", ConnectionState.ERROR.label)
    }

    @Test
    fun testWeatherConditionTypes() {
        assertNotNull(WeatherConditionType.CLEAR)
        assertNotNull(WeatherConditionType.RAIN)
        assertNotNull(WeatherConditionType.STORM)
        assertNotNull(WeatherConditionType.CLOUDY)
        assertNotNull(WeatherConditionType.SNOW)
    }
}
