package com.m4sak1.taskapp

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.ui.components.FloatingBottomNav
import com.m4sak1.taskapp.ui.screens.HomeScreen
import com.m4sak1.taskapp.ui.screens.LicensesScreen
import com.m4sak1.taskapp.ui.screens.SettingsScreen
import com.m4sak1.taskapp.ui.screens.StatsScreen
import com.m4sak1.taskapp.viewmodel.TaskViewModel

enum class ScreenTab { Home, Stats, Settings }

@Composable
fun MainScreen(taskViewModel: TaskViewModel) {
    var currentTab by remember { mutableStateOf(ScreenTab.Home) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    if (showLicenses) {
        LicensesScreen(onBack = { showLicenses = false })
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FloatingBottomNav(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        floatingActionButton = {
            if (currentTab == ScreenTab.Home) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task))
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val duration = 300
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(animationSpec = tween(duration)) { width -> width } + fadeIn(animationSpec = tween(duration))).togetherWith(
                            slideOutHorizontally(animationSpec = tween(duration)) { width -> -width } + fadeOut(animationSpec = tween(duration))
                        )
                    } else {
                        (slideInHorizontally(animationSpec = tween(duration)) { width -> -width } + fadeIn(animationSpec = tween(duration))).togetherWith(
                            slideOutHorizontally(animationSpec = tween(duration)) { width -> width } + fadeOut(animationSpec = tween(duration))
                        )
                    }.using(SizeTransform(clip = false))
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    ScreenTab.Home -> HomeScreen(taskViewModel)
                    ScreenTab.Stats -> StatsScreen()
                    ScreenTab.Settings -> SettingsScreen(onShowLicenses = { showLicenses = true })
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                taskViewModel.addTask(newTaskTitle)
                                newTaskTitle = ""
                                showAddDialog = false
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Text(stringResource(R.string.add_task), color = MaterialTheme.colorScheme.background)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = { Text(stringResource(R.string.new_task)) },
                text = {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.task_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}