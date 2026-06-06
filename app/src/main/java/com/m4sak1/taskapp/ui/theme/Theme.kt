package com.m4sak1.taskapp.ui.theme

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

private val DarkColorSchemeBase = darkColorScheme(
    primary = PureWhite,
    secondary = LightGray,
    tertiary = AccentColor,
    background = DarkGray,
    surface = DarkSurface,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    onTertiary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
)

private val LightColorSchemeBase = lightColorScheme(
    primary = PureBlack,
    secondary = DarkGray,
    tertiary = AccentColor,
    background = LightGray,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureBlack,
    onSurface = PureBlack,
)

@Composable
fun TaskAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = Color.Unspecified,
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> {
            if (accentColor != Color.Unspecified) {
                DarkColorSchemeBase.copy(primary = accentColor, onBackground = accentColor)
            } else {
                DarkColorSchemeBase
            }
        }
        else -> {
            if (accentColor != Color.Unspecified) {
                LightColorSchemeBase.copy(primary = accentColor, onBackground = accentColor)
            } else {
                LightColorSchemeBase
            }
        }
    }
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