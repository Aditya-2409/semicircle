package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalCardSecondary
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.ui.theme.TerminalSuccess
import com.example.ui.theme.TerminalWarning
import com.example.ui.theme.TerminalWhite

@Composable
fun BatteryHealthHudBadge(
    batteryLevel: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val (statusColor, iconVector) = when {
        isCharging -> TerminalSuccess to Icons.Default.BatteryChargingFull
        batteryLevel <= 20 -> TerminalError to Icons.Default.Warning
        batteryLevel <= 50 -> TerminalWarning to Icons.Default.BatteryStd
        else -> TerminalOrangeBright to Icons.Default.BatteryFull
    }

    val healthLabel = when {
        isCharging -> "CHR"
        batteryLevel <= 20 -> "LOW"
        batteryLevel <= 50 -> "MED"
        else -> "OPT"
    }

    Surface(
        color = TerminalCardSecondary,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = "Battery Status",
                tint = statusColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$batteryLevel% [$healthLabel]",
                color = statusColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
