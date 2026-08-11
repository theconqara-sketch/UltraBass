package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = UtlaPrimary,
    onPrimary = UtlaOnPrimary,
    primaryContainer = UtlaPrimaryContainer,
    onPrimaryContainer = UtlaOnPrimaryContainer,
    secondary = UtlaSecondary,
    onSecondary = UtlaOnSecondary,
    secondaryContainer = UtlaSecondaryContainer,
    onSecondaryContainer = UtlaOnSecondaryContainer,
    tertiary = UtlaTertiary,
    onTertiary = UtlaOnTertiary,
    tertiaryContainer = UtlaTertiaryContainer,
    onTertiaryContainer = UtlaOnTertiaryContainer,
    background = UtlaBackground,
    onBackground = UtlaOnBackground,
    surface = UtlaSurface,
    onSurface = UtlaOnSurface,
    surfaceContainer = UtlaSurfaceContainer,
    surfaceContainerHigh = UtlaSurfaceContainerHigh
)

private val LightColorScheme = lightColorScheme(
    primary = UtlaOnPrimaryContainer,
    onPrimary = UtlaPrimaryContainer,
    secondary = UtlaSecondary,
    onSecondary = UtlaOnSecondary,
    background = UtlaOnBackground,
    onBackground = UtlaBackground,
    surface = UtlaOnBackground,
    onSurface = UtlaSurface
)

@Composable
fun UtlaBassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
