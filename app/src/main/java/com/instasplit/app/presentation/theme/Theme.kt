package com.instasplit.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary         = AccentAmber,
    onPrimary       = DarkBackground,
    secondary       = AccentAmberDim,
    background      = DarkBackground,
    surface         = DarkSurface,
    surfaceVariant  = DarkSurfaceVariant,
    outline         = DarkOutline,
    error           = ErrorRed
)

@Composable
fun InstaSplitTheme(content: @Composable () -> Unit) {
    // InstaSplit is always dark â€” mirrors the web app's premium dark aesthetic
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = InstaSplitTypography,
        content     = content
    )
}

