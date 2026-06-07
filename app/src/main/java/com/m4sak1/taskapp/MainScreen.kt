package com.m4sak1.taskapp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.m4sak1.taskapp.ui.components.FloatingBottomNav
import com.m4sak1.taskapp.ui.screens.*
import com.m4sak1.taskapp.ui.theme.LocalThemeController
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.io.File
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.roundToInt

enum class ScreenTab { Home, Stats, Settings, Licenses, MITLicense, PastTasks, EditHome }

@Composable
fun MainScreen(
    taskViewModel: TaskViewModel
) {
    var currentTab by remember { mutableStateOf(ScreenTab.Home) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var editingBgUri by remember { mutableStateOf<Uri?>(null) }

    val fabOffsetX by taskViewModel.fabOffsetX.collectAsState()
    val fabOffsetY by taskViewModel.fabOffsetY.collectAsState()
    
    val context = LocalContext.current
    val themeController = LocalThemeController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val backupSuccessMsg = stringResource(R.string.backup_success)
    val restoreSuccessMsg = stringResource(R.string.restore_success)
    val restoreFailedMsg = stringResource(R.string.restore_failed)

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { 
            taskViewModel.importBackupZip(
                context, it, themeController,
                onSuccess = { scope.launch { snackbarHostState.showSnackbar(restoreSuccessMsg) } },
                onError = { scope.launch { snackbarHostState.showSnackbar(restoreFailedMsg) } }
            )
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let { 
            taskViewModel.exportBackupZip(
                context, it, themeController,
                onSuccess = { scope.launch { snackbarHostState.showSnackbar(backupSuccessMsg) } }
            )
        }
    }
    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { editingBgUri = it }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // BACKGROUND IMAGE LAYER
            val bgPath by taskViewModel.backgroundPath.collectAsState()
            val bitmap = remember(bgPath) {
                if (bgPath != null) {
                    try {
                        val file = File(bgPath)
                        if (file.exists()) BitmapFactory.decodeFile(bgPath) else null
                    } catch (e: Exception) { null }
                } else null
            }
            
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(themeController.backgroundBlur.dp),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(
                    if (themeController.isDarkTheme) Color.Black.copy(alpha = 0.3f)
                    else Color.White.copy(alpha = 0.3f)
                ))
            }

            // NAVIGATION STRUCTURE
            val displayTabs = listOf(ScreenTab.Home, ScreenTab.Stats, ScreenTab.Settings)
            val isMainTab = currentTab in displayTabs

            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (isMainTab) {
                        FloatingBottomNav(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                }
            ) { paddingValues ->
                // To solve the FAB position mismatch, we wrap everything in an outer box 
                // and subtract padding manually only for the content, NOT the FAB.
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize().padding(if (isMainTab) paddingValues else PaddingValues(0.dp))) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                val duration = 300
                                val target = targetState
                                val initial = initialState
                                if (target.ordinal > initial.ordinal) {
                                    (slideInHorizontally(animationSpec = tween(duration)) { width -> width } + fadeIn(animationSpec = tween(duration))).togetherWith(
                                        slideOutHorizontally(animationSpec = tween(duration)) { width -> -width } + fadeOut(animationSpec = tween(duration))
                                    )
                                } else {
                                    (slideInHorizontally(animationSpec = tween(duration)) { width -> -width } + fadeIn(animationSpec = tween(duration))).togetherWith(
                                        slideOutHorizontally(animationSpec = tween(duration)) { width -> width } + fadeOut(animationSpec = tween(duration))
                                    )
                                }.using(SizeTransform(clip = false))
                            },
                            label = "screen_transition"
                        ) { targetTab ->
                            when (targetTab) {
                                ScreenTab.Home -> HomeScreen(taskViewModel)
                                ScreenTab.Stats -> StatsScreen(viewModel = taskViewModel, onShowPastTasks = { currentTab = ScreenTab.PastTasks })
                                ScreenTab.Settings -> SettingsScreen(
                                    viewModel = taskViewModel, 
                                    onShowLicenses = { currentTab = ScreenTab.Licenses },
                                    onShowMITLicense = { currentTab = ScreenTab.MITLicense },
                                    onShowEditHome = { currentTab = ScreenTab.EditHome },
                                    onBackup = { exportLauncher.launch("m4task_backup.zip") },
                                    onRestore = { importLauncher.launch(arrayOf("application/zip")) },
                                    onPickBackground = { bgLauncher.launch("image/*") }
                                )
                                ScreenTab.Licenses -> LicensesScreen(onBack = { currentTab = ScreenTab.Settings })
                                ScreenTab.MITLicense -> MITLicenseScreen(onBack = { currentTab = ScreenTab.Settings })
                                ScreenTab.PastTasks -> PastTasksScreen(viewModel = taskViewModel, onBack = { currentTab = ScreenTab.Stats })
                                ScreenTab.EditHome -> EditHomeScreen(viewModel = taskViewModel, onBack = { currentTab = ScreenTab.Settings })
                            }
                        }
                    }

                    // FAB positioned absolutely within the outer Box to ignore Scaffold padding
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

            if (editingBgUri != null) {
                BackgroundEditorScreen(
                    imageUri = editingBgUri!!,
                    onSave = { scale, tx, ty, blurEnabled ->
                        val activity = context.findActivity() as? MainActivity
                        val finalPath = activity?.saveBackgroundImage(editingBgUri!!, scale, tx, ty)
                        taskViewModel.updateBackgroundPath(finalPath)
                        themeController.setBackgroundBlur(if (blurEnabled) 15f else 0f)
                        editingBgUri = null
                    },
                    onCancel = { editingBgUri = null }
                )
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
}
