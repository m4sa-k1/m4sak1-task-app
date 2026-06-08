package com.m4sak1.taskapp

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.m4sak1.taskapp.data.PreferenceManager
import com.m4sak1.taskapp.ui.theme.*
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import dev.chrisbanes.haze.HazeState
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        createNotificationChannel()
        
        val prefManager = PreferenceManager(this)

        setContent {
            var themeMode by remember { mutableStateOf(prefManager.themeMode) }
            var appLanguage by remember { mutableStateOf(prefManager.appLanguage) }
            var accentColor by remember { mutableStateOf(prefManager.accentColor) }
            var customAccentColor by remember { mutableStateOf(Color(prefManager.customAccentColor)) }
            val backgroundPath by taskViewModel.backgroundPath.collectAsState()
            var backgroundBlur by remember { mutableStateOf(prefManager.backgroundBlur) }
            val isGlassModeEnabled by taskViewModel.isGlassModeEnabled.collectAsState()

            val isDarkTheme = when (themeMode) {
                AppThemeMode.System -> isSystemInDarkTheme()
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
            }

            val themeController = remember(themeMode, appLanguage, isDarkTheme, accentColor, customAccentColor, backgroundPath, backgroundBlur, isGlassModeEnabled) {
                ThemeController(
                    themeMode = themeMode,
                    setThemeMode = { 
                        themeMode = it
                        prefManager.themeMode = it
                    },
                    appLanguage = appLanguage,
                    setAppLanguage = { 
                        appLanguage = it
                        prefManager.appLanguage = it
                    },
                    accentColor = accentColor,
                    setAccentColor = { 
                        accentColor = it
                        prefManager.accentColor = it
                    },
                    customAccentColor = customAccentColor,
                    setCustomAccentColor = { 
                        customAccentColor = it
                        prefManager.customAccentColor = it.toArgb()
                    },
                    backgroundPath = backgroundPath,
                    setBackgroundPath = { taskViewModel.updateBackgroundPath(it) },
                    backgroundBlur = backgroundBlur,
                    setBackgroundBlur = { 
                        backgroundBlur = it
                        prefManager.backgroundBlur = it
                    },
                    isGlassModeEnabled = isGlassModeEnabled,
                    setGlassModeEnabled = { taskViewModel.setGlassModeEnabled(it) },
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
                } else {
                    val systemLocale = Locale.getDefault()
                    config.setLocale(systemLocale)
                }
                config
            }

            val localizedContext = remember(localizedConfiguration) {
                activityContext.createConfigurationContext(localizedConfiguration)
            }

            // Apply locale to the Activity's resources as well so that Dialog windows use the correct language
            SideEffect {
                activityContext.resources.updateConfiguration(
                    localizedConfiguration,
                    activityContext.resources.displayMetrics
                )
            }

            val hazeState = remember { HazeState() }

            CompositionLocalProvider(
                LocalThemeController provides themeController,
                LocalHazeState provides hazeState,
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides activityContext,
                LocalOnBackPressedDispatcherOwner provides activityContext
            ) {
                val effectiveColor = if (accentColor == AppAccentColor.Custom) customAccentColor else accentColor.color
                TaskAppTheme(darkTheme = isDarkTheme, accentColor = effectiveColor) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        MainScreen(
                            taskViewModel = taskViewModel,
                            activity = activityContext
                        )
                    }
                }
            }
        }
    }

    fun saveBackgroundImage(uri: Uri, scale: Float, tx: Float, ty: Float): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val original = BitmapFactory.decodeStream(inputStream) ?: return null
                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels
                val result = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(result)
                val matrix = Matrix()
                val scaleFit = Math.max(screenWidth.toFloat() / original.width, screenHeight.toFloat() / original.height)
                matrix.postTranslate(-original.width / 2f, -original.height / 2f)
                matrix.postScale(scaleFit * scale, scaleFit * scale)
                matrix.postTranslate(screenWidth / 2f + tx, screenHeight / 2f + ty)
                canvas.drawBitmap(original, matrix, null)
                val file = File(filesDir, "background.jpg")
                if (file.exists()) file.delete()
                FileOutputStream(file).use { outputStream ->
                    result.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel = android.app.NotificationChannel("task_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
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