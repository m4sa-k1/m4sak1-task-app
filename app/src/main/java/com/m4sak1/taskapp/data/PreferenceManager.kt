package com.m4sak1.taskapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.m4sak1.taskapp.ui.theme.AppAccentColor
import com.m4sak1.taskapp.ui.theme.AppLanguage
import com.m4sak1.taskapp.ui.theme.AppThemeMode

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var themeMode: AppThemeMode
        get() = AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.System.name) ?: AppThemeMode.System.name)
        set(value) = prefs.edit().putString("theme_mode", value.name).apply()

    var appLanguage: AppLanguage
        get() = AppLanguage.valueOf(prefs.getString("app_language", AppLanguage.System.name) ?: AppLanguage.System.name)
        set(value) = prefs.edit().putString("app_language", value.name).apply()

    var accentColor: AppAccentColor
        get() = AppAccentColor.valueOf(prefs.getString("accent_color", AppAccentColor.Default.name) ?: AppAccentColor.Default.name)
        set(value) = prefs.edit().putString("accent_color", value.name).apply()

    var customAccentColor: Int
        get() = prefs.getInt("custom_accent_color", Color.Unspecified.toArgb())
        set(value) = prefs.edit().putInt("custom_accent_color", value).apply()

    var backgroundPath: String?
        get() = prefs.getString("background_path", null)
        set(value) = prefs.edit().putString("background_path", value).apply()

    var backgroundBlur: Float
        get() = prefs.getFloat("background_blur", 0f)
        set(value) = prefs.edit().putFloat("background_blur", value).apply()

    var fabOffsetX: Float
        get() = prefs.getFloat("fab_offset_x", 0f)
        set(value) = prefs.edit().putFloat("fab_offset_x", value).apply()

    var fabOffsetY: Float
        get() = prefs.getFloat("fab_offset_y", -240f)
        set(value) = prefs.edit().putFloat("fab_offset_y", value).apply()

    var hideImmediately: Boolean
        get() = prefs.getBoolean("hide_immediately", false)
        set(value) = prefs.edit().putBoolean("hide_immediately", value).apply()
}