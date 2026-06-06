package com.m4sak1.taskapp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.m4sak1.taskapp.ui.components.FloatingBottomNav
import com.m4sak1.taskapp.ui.screens.*
import com.m4sak1.taskapp.ui.theme.LocalThemeController
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import kotlin.math.roundToInt

enum class ScreenTab { Home, Stats, Settings }

@Composable
fun MainScreen(
    taskViewModel: TaskViewModel,
    onExportBackup: () -> Unit // Note: We'll use our own launcher here but keeping signature for now
) {
    var currentTab by remember { mutableStateOf(ScreenTab.Home) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }
    var showMITLicense by remember { mutableStateOf(false) }
    var showPastTasks by remember { mutableStateOf(false) }
    var showEditHome by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    val fabOffsetX by taskViewModel.fabOffsetX.collectAsState()
    val fabOffsetY by taskViewModel.fabOffsetY.collectAsState()
    
    val context = LocalContext.current
    val themeController = LocalThemeController.current
    
    // Launcher MUST be registered at the top level of the Composable
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { taskViewModel.importBackup(context, it, themeController) }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { taskViewModel.exportBackup(context, it) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Screen Content Layer
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                showLicenses -> LicensesScreen(onBack = { showLicenses = false })
                showMITLicense -> MITLicenseScreen(onBack = { showMITLicense = false })
                showPastTasks -> PastTasksScreen(viewModel = taskViewModel, onBack = { showPastTasks = false })
                showEditHome -> EditHomeScreen(viewModel = taskViewModel, onBack = { showEditHome = false })
                else -> {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            FloatingBottomNav(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it }
                            )
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
                                    ScreenTab.Stats -> StatsScreen(viewModel = taskViewModel, onShowPastTasks = { showPastTasks = true })
                                    ScreenTab.Settings -> SettingsScreen(
                                        viewModel = taskViewModel, 
                                        onShowLicenses = { showLicenses = true },
                                        onShowMITLicense = { showMITLicense = true },
                                        onShowEditHome = { showEditHome = true },
                                        onBackup = { exportLauncher.launch("m4task_backup.json") },
                                        onRestore = { importLauncher.launch(arrayOf("application/json")) }
                                    )
                                }
                            }
                        }
                    }

                    // Floating Action Button Layer (Home Only)
                    if (currentTab == ScreenTab.Home) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            FloatingActionButton(
                                onClick = { showAddDialog = true },
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = MaterialTheme.colorScheme.background,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset { IntOffset(fabOffsetX.roundToInt(), fabOffsetY.roundToInt()) }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task))
                            }
                        }
                    }
                }
            }

            // Dialogs Layer
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
}