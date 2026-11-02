package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SemicircleDarkColorScheme = darkColorScheme(
    primary = TerminalOrange,
    onPrimary = TerminalBackground,
    primaryContainer = TerminalCardSecondary,
    onPrimaryContainer = TerminalOrangeBright,
    secondary = TerminalOrangeBright,
    onSecondary = TerminalBackground,
    secondaryContainer = TerminalCard,
    onSecondaryContainer = TerminalWhite,
    tertiary = TerminalCyan,
    onTertiary = TerminalBackground,
    background = TerminalBackground,
    onBackground = TerminalWhite,
    surface = TerminalCard,
    onSurface = TerminalWhite,
    surfaceVariant = TerminalCardSecondary,
    onSurfaceVariant = TerminalTextSecondary,
    error = TerminalError,
    onError = TerminalWhite
)

@Composable
fun SemicircleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SemicircleDarkColorScheme,
        typography = Typography,
        content = content
    )
}
