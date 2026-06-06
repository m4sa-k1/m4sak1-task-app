package com.m4sak1.taskapp

import android.graphics.Bitmap
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
                val config = android.content.res.Configuration(configuration)
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

    fun copyUriToTemp(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File(cacheDir, "temp_bg_edit.jpg")
            FileOutputStream(tempFile).use { inputStream?.copyTo(it) }
            tempFile.absolutePath
        } catch (e: Exception) { null }
    }

    fun processAndSaveBackground(
        tempPath: String,
        scale: Float,
        tx: Float,
        ty: Float
    ): String? {
        return try {
            val options = android.graphics.BitmapFactory.Options()
            val original = android.graphics.BitmapFactory.decodeFile(tempPath, options) ?: return null
            
            // Create a bitmap that matches screen size or some high res
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            
            val result = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            
            val matrix = Matrix()
            // Center crop logic based on scale and translation
            // This is a simplified version, ideally we match Compose's graphicsLayer
            matrix.postScale(scale, scale, (original.width / 2).toFloat(), (original.height / 2).toFloat())
            matrix.postTranslate(tx, ty)
            
            // Center the image on canvas first
            val dx = (screenWidth - original.width) / 2f
            val dy = (screenHeight - original.height) / 2f
            matrix.preTranslate(dx, dy)
            
            canvas.drawBitmap(original, matrix, null)
            
            val file = File(filesDir, "background.jpg")
            FileOutputStream(file).use { 
                result.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}