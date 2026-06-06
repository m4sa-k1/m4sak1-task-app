package com.m4sak1.taskapp

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
import com.m4sak1.taskapp.ui.theme.LocalThemeController
import com.m4sak1.taskapp.ui.theme.TaskAppTheme
import com.m4sak1.taskapp.ui.theme.ThemeController
import com.m4sak1.taskapp.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemTheme) }

            val themeController = remember(isDarkTheme) {
                ThemeController(
                    isDarkTheme = isDarkTheme,
                    toggleTheme = { isDarkTheme = it }
                )
            }

            CompositionLocalProvider(LocalThemeController provides themeController) {
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