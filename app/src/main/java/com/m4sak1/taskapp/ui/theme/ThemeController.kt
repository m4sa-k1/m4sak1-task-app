package com.m4sak1.taskapp.ui.theme

import androidx.compose.runtime.compositionLocalOf

data class ThemeController(
    val isDarkTheme: Boolean,
    val toggleTheme: (Boolean) -> Unit
)

val LocalThemeController = compositionLocalOf<ThemeController> {
    error("No ThemeController provided")
}