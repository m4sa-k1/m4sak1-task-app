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
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

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
                var newTaskTitle by remember { mutableStateOf("") }
                var isTaskStarred by remember { mutableStateOf(false) }
                val focusRequester = remember { FocusRequester() }
                
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(100)
                    try {
                        focusRequester.requestFocus()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                AlertDialog(
                    onDismissRequest = { finish() },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    scope.launch {
                                        taskViewModel.addTask(newTaskTitle, isTaskStarred)?.join()
                                        finish()
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                        ) {
                            Text(stringResource(R.string.add_task), color = MaterialTheme.colorScheme.background)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { finish() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    title = { Text(stringResource(R.string.new_task)) },
                    text = {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = { Text(stringResource(R.string.task_placeholder)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { isTaskStarred = !isTaskStarred }) {
                                    Text(
                                        text = if (isTaskStarred) "★" else "☆",
                                        fontSize = 24.sp,
                                        color = if (isTaskStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
