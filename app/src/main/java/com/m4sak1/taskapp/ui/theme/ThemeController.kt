package com.m4sak1.taskapp.ui.theme

import androidx.compose.runtime.compositionLocalOf

enum class AppThemeMode { System, Light, Dark }
enum class AppLanguage { System, English, Japanese, SimplifiedChinese, TraditionalChinese }

data class ThemeController(
    val themeMode: AppThemeMode,
    val setThemeMode: (AppThemeMode) -> Unit,
    val appLanguage: AppLanguage,
    val setAppLanguage: (AppLanguage) -> Unit,
    val isDarkTheme: Boolean // Calculated effective theme
)

val LocalThemeController = compositionLocalOf<ThemeController> {
    error("No ThemeController provided")
}