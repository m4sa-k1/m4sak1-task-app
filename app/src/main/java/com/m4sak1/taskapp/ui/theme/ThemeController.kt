package com.m4sak1.taskapp.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState

enum class AppThemeMode { System, Light, Dark }
enum class AppLanguage { System, English, Japanese, SimplifiedChinese, TraditionalChinese }

enum class AppAccentColor(val color: Color, val label: String) {
    Default(Color.Unspecified, "Default"),
    Red(Color(0xFFFF3B30), "Red"),
    Orange(Color(0xFFFF9500), "Orange"),
    Yellow(Color(0xFFFFCC00), "Yellow"),
    Green(Color(0xFF34C759), "Green"),
    Blue(Color(0xFF007AFF), "Blue"),
    Indigo(Color(0xFF5856D6), "Indigo"),
    Purple(Color(0xFFAF52DE), "Purple"),
    Pink(Color(0xFFFF2D55), "Pink"),
    Custom(Color.Unspecified, "Custom")
}

data class ThemeController(
    val themeMode: AppThemeMode,
    val setThemeMode: (AppThemeMode) -> Unit,
    val appLanguage: AppLanguage,
    val setAppLanguage: (AppLanguage) -> Unit,
    val accentColor: AppAccentColor,
    val setAccentColor: (AppAccentColor) -> Unit,
    val customAccentColor: Color,
    val setCustomAccentColor: (Color) -> Unit,
    val backgroundPath: String?,
    val setBackgroundPath: (String?) -> Unit,
    val backgroundBlur: Float,
    val setBackgroundBlur: (Float) -> Unit,
    val isGlassModeEnabled: Boolean,
    val setGlassModeEnabled: (Boolean) -> Unit,
    val isDarkTheme: Boolean
)

val LocalThemeController = compositionLocalOf<ThemeController> {
    error("No ThemeController provided")
}

val LocalHazeState = compositionLocalOf<HazeState?> { null }