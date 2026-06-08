package com.m4sak1.taskapp.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.glance.appwidget.updateAll
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.ui.theme.TaskAppTheme
import com.m4sak1.taskapp.ui.components.CustomAddDialog
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class WidgetAddActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure transparent background for the Activity window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        setContent {
            TaskAppTheme {
                val scope = rememberCoroutineScope()
                var visible by remember { mutableStateOf(true) }
                val enterToAdd by taskViewModel.enterToAdd.collectAsState()

                CustomAddDialog(
                    visible = visible,
                    onDismissRequest = {
                        visible = false
                        scope.launch {
                            delay(200) // wait for exit animation
                            finish()
                        }
                    },
                    onAddTask = { title, starred ->
                        visible = false
                        scope.launch {
                            taskViewModel.addTask(title, starred)?.join()
                            delay(200) // wait for exit animation
                            finish()
                        }
                    },
                    enterToAdd = enterToAdd
                )
            }
        }
    }
}
