package com.stadiolinks.kitabu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KitabuColorScheme = lightColorScheme(
    primary = KitabuTeal,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = KitabuNavy,
    onSecondary = Color.White,
    secondaryContainer = NavyContainer,
    onSecondaryContainer = KitabuNavy,
    tertiary = KitabuAmber,
    onTertiary = KitabuNavy,
    background = KitabuSurface,
    onBackground = KitabuNavy,
    surface = Color.White,
    onSurface = KitabuNavy,
    surfaceVariant = NavyContainer,
    onSurfaceVariant = OnSurfaceMuted,
    outline = OutlineMuted,
    outlineVariant = OutlineSoft,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun KitabuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KitabuColorScheme,
        content = content
    )
}
