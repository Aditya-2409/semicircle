package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGridLine
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.weather.model.WeatherConditionType
import kotlin.random.Random

@Composable
fun LiveWeatherBackground(
    conditionType: WeatherConditionType = WeatherConditionType.CLEAR,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weatherAnim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    // Pre-calculate deterministic random particle positions
    val particles = remember(conditionType) {
        val rand = Random(42)
        List(40) {
            Triple(rand.nextFloat(), rand.nextFloat(), rand.nextFloat() * 3f + 1f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Base sci-fi gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    TerminalBackground,
                    Color(0xFF0F0B07),
                    TerminalBackground
                )
            )
        )

        // 2. Subtle sci-fi orange grid lines
        val gridSize = 72f
        var x = 0f
        while (x < w) {
            drawLine(
                color = TerminalGridLine,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
            x += gridSize
        }
        var y = 0f
        while (y < h) {
            drawLine(
                color = TerminalGridLine,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += gridSize
        }

        // 3. Dynamic Weather Layer
        when (conditionType) {
            WeatherConditionType.CLEAR -> {
                // Warm radiant ambient pulse and floating solar embers
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(TerminalOrange.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.25f),
                        radius = w * 0.7f
                    ),
                    radius = w * 0.7f,
                    center = Offset(w * 0.5f, h * 0.25f)
                )
                particles.take(25).forEach { (px, py, speed) ->
                    val curY = (py - (animProgress * speed * 0.2f)) % 1f
                    val actualY = if (curY < 0f) curY + 1f else curY
                    drawCircle(
                        color = TerminalOrangeBright.copy(alpha = 0.4f * (1f - actualY)),
                        radius = speed * 1.5f,
                        center = Offset(px * w, actualY * h)
                    )
                }
            }

            WeatherConditionType.RAIN -> {
                // Sci-fi orange rain streaks descending rapidly
                particles.forEach { (px, py, speed) ->
                    val curY = (py + (animProgress * speed * 1.4f)) % 1f
                    val startX = px * w
                    val startY = curY * h
                    val streakLen = 30f * speed
                    drawLine(
                        color = TerminalOrange.copy(alpha = 0.5f),
                        start = Offset(startX, startY),
                        end = Offset(startX + (streakLen * 0.15f), startY + streakLen),
                        strokeWidth = 1.8f
                    )
                }
            }

            WeatherConditionType.STORM -> {
                // Electric storm pulses & sharp rainfall
                val flashAlpha = if (animProgress in 0.45f..0.52f || animProgress in 0.85f..0.91f) 0.18f else 0.03f
                drawRect(color = TerminalOrangeBright.copy(alpha = flashAlpha))

                particles.forEach { (px, py, speed) ->
                    val curY = (py + (animProgress * speed * 1.8f)) % 1f
                    val startX = px * w
                    val startY = curY * h
                    val streakLen = 45f * speed
                    drawLine(
                        color = TerminalOrangeBright.copy(alpha = 0.65f),
                        start = Offset(startX, startY),
                        end = Offset(startX + (streakLen * 0.2f), startY + streakLen),
                        strokeWidth = 2.2f
                    )
                }
            }

            WeatherConditionType.CLOUDY, WeatherConditionType.FOG -> {
                // Atmospheric mist layers
                val mistX = (animProgress * w * 0.3f) % w
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(TerminalOrange.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(mistX, h * 0.35f),
                        radius = w * 0.8f
                    ),
                    radius = w * 0.8f,
                    center = Offset(mistX, h * 0.35f)
                )
            }

            WeatherConditionType.SNOW -> {
                // Drifting crystals
                particles.forEach { (px, py, speed) ->
                    val curY = (py + (animProgress * speed * 0.3f)) % 1f
                    val sway = kotlin.math.sin((animProgress + py) * kotlin.math.PI * 2).toFloat() * 12f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = speed * 1.4f,
                        center = Offset((px * w) + sway, curY * h)
                    )
                }
            }
        }
    }
}
