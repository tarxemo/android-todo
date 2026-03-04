package com.example.todoapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = ElectricBlue,
    background = DeepSpace,
    surface = SurfaceDark,
    onPrimary = TextPrimary,
    onSecondary = DeepSpace,
    onTertiary = DeepSpace,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderGray
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = ElectricBlue,
    background = TextPrimary,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = DeepSpace,
    onTertiary = DeepSpace,
    onBackground = DeepSpace,
    onSurface = DeepSpace,
    outline = Color.LightGray
)

@Composable
fun TodoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
