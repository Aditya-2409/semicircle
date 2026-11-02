package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.ui.theme.TerminalOrangeGlow
import com.example.voice.AssistantVoiceState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SemicircleCyberFace(
    targetX: Float, // -1f (left) to 1f (right)
    targetY: Float, // -1f (up) to 1f (down)
    isFaceDetected: Boolean,
    audioAmplitude: Float, // 0f to 1f
    voiceState: AssistantVoiceState,
    isLowBatterySad: Boolean,
    modifier: Modifier = Modifier.height(280.dp).fillMaxWidth()
) {
    // Idle breathing & tech pulse animations
    val infiniteTransition = rememberInfiniteTransition(label = "cyberFacePulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    // Searching sweeping motion when face is not detected
    val searchSweep = if (!isFaceDetected) {
        kotlin.math.sin(pulsePhase * 1.5f) * 0.85f
    } else {
        0f
    }
    val searchTilt = if (!isFaceDetected) {
        kotlin.math.cos(pulsePhase * 0.75f) * 0.3f
    } else {
        0f
    }

    val effectiveTargetX = if (isFaceDetected) targetX else searchSweep
    val effectiveTargetY = if (isFaceDetected) targetY else searchTilt

    // Smooth eye interpolation using spring physics
    val smoothEyeX by animateFloatAsState(
        targetValue = effectiveTargetX,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 120f),
        label = "eyeX"
    )
    val smoothEyeY by animateFloatAsState(
        targetValue = effectiveTargetY,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 120f),
        label = "eyeY"
    )
    val soundRingRadius by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "soundRing"
    )

    val eyeColor = when {
        isLowBatterySad -> TerminalError
        voiceState == AssistantVoiceState.THINKING -> TerminalCyan
        isFaceDetected -> TerminalOrangeBright
        else -> TerminalOrange
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h * 0.42f

        // 1. Concentric Sound-Wave Rings (Active when speaking)
        if (voiceState == AssistantVoiceState.SPEAKING) {
            val ringRadius = (w * 0.38f) * soundRingRadius
            val ringAlpha = (1f - (soundRingRadius - 0.7f) / 0.65f).coerceIn(0f, 0.4f)
            drawCircle(
                color = TerminalOrangeGlow.copy(alpha = ringAlpha),
                radius = ringRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 3. Dual Cybernetic Eyes (Left & Right)
        val eyeSpacing = w * 0.28f
        val leftEyeCenter = Offset(centerX - eyeSpacing, centerY)
        val rightEyeCenter = Offset(centerX + eyeSpacing, centerY)
        val eyeRadius = 38.dp.toPx()

        drawCyberEye(
            center = leftEyeCenter,
            radius = eyeRadius,
            targetX = smoothEyeX,
            targetY = smoothEyeY,
            color = eyeColor,
            isSad = isLowBatterySad,
            isFaceDetected = isFaceDetected,
            pulsePhase = pulsePhase
        )

        drawCyberEye(
            center = rightEyeCenter,
            radius = eyeRadius,
            targetX = smoothEyeX,
            targetY = smoothEyeY,
            color = eyeColor,
            isSad = isLowBatterySad,
            isFaceDetected = isFaceDetected,
            pulsePhase = pulsePhase
        )

    }
}

private fun DrawScope.drawCyberEye(
    center: Offset,
    radius: Float,
    targetX: Float,
    targetY: Float,
    color: Color,
    isSad: Boolean,
    isFaceDetected: Boolean,
    pulsePhase: Float
) {
    // Outer cyber socket ring
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Iris tracking offset: max offset is radius * 0.42
    val maxPupilShift = radius * 0.42f
    val pupilOffsetX = targetX * maxPupilShift
    val pupilOffsetY = if (isSad) {
        (targetY * maxPupilShift * 0.5f) + (radius * 0.3f)
    } else {
        targetY * maxPupilShift
    }
    val pupilCenter = Offset(center.x + pupilOffsetX, center.y + pupilOffsetY)

    // Glowing Iris
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0.2f)),
            center = pupilCenter,
            radius = radius * 0.45f
        ),
        radius = radius * 0.45f,
        center = pupilCenter
    )

    // Intense pupil core
    drawCircle(
        color = if (isSad) TerminalError else TerminalOrangeBright,
        radius = radius * 0.22f,
        center = pupilCenter
    )

    // Center laser pinpoint
    drawCircle(
        color = Color.White,
        radius = radius * 0.08f,
        center = pupilCenter
    )

    // Melancholy eyelid visor in low battery state
    if (isSad) {
        val lidPath = Path().apply {
            moveTo(center.x - radius, center.y - (radius * 0.5f))
            cubicTo(
                center.x - (radius * 0.4f), center.y + (radius * 0.2f),
                center.x + (radius * 0.4f), center.y + (radius * 0.2f),
                center.x + radius, center.y - (radius * 0.5f)
            )
            lineTo(center.x + radius, center.y - radius)
            lineTo(center.x - radius, center.y - radius)
            close()
        }
        drawPath(path = lidPath, color = Color(0xCC090909))
        drawPath(path = lidPath, color = TerminalError, style = Stroke(width = 2.dp.toPx()))
    }
}
