=== ABOUT: Fixed the colour order so the gold and ink colours are ready before the light/dark themes are built ===
package com.aibuilt.jaiho777alwayswin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val ColorWhite = Color(0xFFFFFFFF)
private val ColorTransparent = Color(0x00000000)

private val LightColors = lightColorScheme(
    primary = GoldLight,
    onPrimary = ColorWhite,
    secondary = InkLight,
    onSecondary = ColorWhite,
    background = SurfaceLight,
    onBackground = InkLight,
    surface = ColorWhite,
    onSurface = InkLight,
    error = RedDark,
    onError = ColorWhite
)

private val DarkColors = darkColorScheme(
    primary = GoldDark,
    onPrimary = InkDark,
    secondary = GoldDark,
    onSecondary = InkDark,
    background = InkDark,
    onBackground = ColorWhite,
    surface = SurfaceDark,
    onSurface = ColorWhite,
    error = RedDark,
    onError = InkDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = ColorWhite,
    outline = GoldDark,
    scrim = ColorTransparent
)

@Composable
fun JaihoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colors, content = content)
}
