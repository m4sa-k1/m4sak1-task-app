package com.m4sak1.taskapp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.m4sak1.taskapp.ui.theme.*
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import java.util.*

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeMode by remember { mutableStateOf(AppThemeMode.System) }
            var appLanguage by remember { mutableStateOf(AppLanguage.System) }

            val isDarkTheme = when (themeMode) {
                AppThemeMode.System -> isSystemInDarkTheme()
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
            }

            val themeController = remember(themeMode, appLanguage, isDarkTheme) {
                ThemeController(
                    themeMode = themeMode,
                    setThemeMode = { themeMode = it },
                    appLanguage = appLanguage,
                    setAppLanguage = { appLanguage = it },
                    isDarkTheme = isDarkTheme
                )
            }

            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            
            // Create a custom configuration based on selected language
            val localizedConfiguration = remember(appLanguage, configuration) {
                val config = Configuration(configuration)
                if (appLanguage != AppLanguage.System) {
                    val localeCode = when (appLanguage) {
                        AppLanguage.English -> "en"
                        AppLanguage.Japanese -> "ja"
                        AppLanguage.SimplifiedChinese -> "zh-CN"
                        AppLanguage.TraditionalChinese -> "zh-TW"
                        else -> "en"
                    }
                    val locale = if (localeCode.contains("-")) {
                        val parts = localeCode.split("-")
                        Locale(parts[0], parts[1])
                    } else {
                        Locale(localeCode)
                    }
                    Locale.setDefault(locale)
                    config.setLocale(locale)
                }
                config
            }

            // Create a localized context to provide through LocalContext
            val localizedContext = remember(localizedConfiguration, context) {
                context.createConfigurationContext(localizedConfiguration)
            }

            CompositionLocalProvider(
                LocalThemeController provides themeController,
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext
            ) {
                TaskAppTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(taskViewModel = taskViewModel)
                    }
                }
            }
        }
    }
}