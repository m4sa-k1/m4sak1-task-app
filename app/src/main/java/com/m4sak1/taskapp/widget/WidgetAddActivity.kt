package com.m4sak1.taskapp.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.m4sak1.taskapp.ui.components.CustomAddDialog
import com.m4sak1.taskapp.ui.theme.TaskAppTheme
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WidgetAddActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        
        // Ensure transparent background for the Activity window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            TaskAppTheme {
                val scope = rememberCoroutineScope()
                
                val addDialogStyle by taskViewModel.addDialogStyle.collectAsState()
                val disableAnimations by taskViewModel.disableAnimations.collectAsState()
                val enterToAdd by taskViewModel.enterToAdd.collectAsState()
                
                var showDialog by remember { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    delay(50)
                    showDialog = true
                }

                CustomAddDialog(
                    visible = showDialog,
                    onDismissRequest = {
                        showDialog = false
                        scope.launch {
                            delay(200) // allow exit animation to play
                            finish()
                            @Suppress("DEPRECATION")
                            overridePendingTransition(0, 0)
                        }
                    },
                    onAddTask = { title, starred ->
                        scope.launch {
                            taskViewModel.addTask(title, starred)?.join()
                            showDialog = false
                            delay(200)
                            finish()
                            @Suppress("DEPRECATION")
                            overridePendingTransition(0, 0)
                        }
                    },
                    enterToAdd = enterToAdd,
                    style = addDialogStyle,
                    disableAnimations = disableAnimations
                )
            }
        }
    }
}
