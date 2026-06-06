package com.m4sak1.taskapp

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.m4sak1.taskapp.ui.theme.*
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var themeMode by remember { mutableStateOf(AppThemeMode.System) }
            var appLanguage by remember { mutableStateOf(AppLanguage.System) }
            var accentColor by remember { mutableStateOf(AppAccentColor.Default) }
            var customAccentColor by remember { mutableStateOf(Color.Unspecified) }
            val backgroundPath by taskViewModel.backgroundPath.collectAsState()
            var backgroundBlur by remember { mutableStateOf(0f) }

            val isDarkTheme = when (themeMode) {
                AppThemeMode.System -> isSystemInDarkTheme()
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
            }

            val themeController = remember(themeMode, appLanguage, isDarkTheme, accentColor, customAccentColor, backgroundPath, backgroundBlur) {
                ThemeController(
                    themeMode = themeMode,
                    setThemeMode = { themeMode = it },
                    appLanguage = appLanguage,
                    setAppLanguage = { appLanguage = it },
                    accentColor = accentColor,
                    setAccentColor = { accentColor = it },
                    customAccentColor = customAccentColor,
                    setCustomAccentColor = { customAccentColor = it },
                    backgroundPath = backgroundPath,
                    setBackgroundPath = { taskViewModel.updateBackgroundPath(it) },
                    backgroundBlur = backgroundBlur,
                    setBackgroundBlur = { backgroundBlur = it },
                    isDarkTheme = isDarkTheme
                )
            }

            val activityContext = this
            val configuration = LocalConfiguration.current
            
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

            val localizedContext = remember(localizedConfiguration) {
                activityContext.createConfigurationContext(localizedConfiguration)
            }

            CompositionLocalProvider(
                LocalThemeController provides themeController,
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides activityContext,
                LocalOnBackPressedDispatcherOwner provides activityContext
            ) {
                val effectiveColor = if (accentColor == AppAccentColor.Custom) customAccentColor else accentColor.color
                TaskAppTheme(darkTheme = isDarkTheme, accentColor = effectiveColor) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(
                            taskViewModel = taskViewModel
                        )
                    }
                }
            }
        }
    }

    fun saveBackgroundImage(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File(filesDir, "background.jpg")
                if (file.exists()) file.delete()
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}