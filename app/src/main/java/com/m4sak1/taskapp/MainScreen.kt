package com.m4sak1.taskapp

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.tween
import kotlin.math.abs
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
import dev.chrisbanes.haze.haze
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m4sak1.taskapp.ui.components.FloatingBottomNav
import com.m4sak1.taskapp.ui.screens.*
import com.m4sak1.taskapp.ui.theme.LocalThemeController
import com.m4sak1.taskapp.ui.theme.LocalHazeState
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.io.File
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.roundToInt

enum class ScreenTab { Home, Stats, Settings, Licenses, MITLicense, PastTasks, EditHome }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    taskViewModel: TaskViewModel,
    activity: MainActivity
) {
    var currentTab by remember { mutableStateOf(ScreenTab.Home) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var editingBgUri by remember { mutableStateOf<Uri?>(null) }
    
    val disableAnimations by taskViewModel.disableAnimations.collectAsState()

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

    val hasRequestedNotificationPermission by taskViewModel.hasRequestedNotificationPermission.collectAsState()
    val initialPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        taskViewModel.setNotificationsEnabled(isGranted)
        taskViewModel.setHasRequestedNotificationPermission(true)
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedNotificationPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                initialPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                taskViewModel.setNotificationsEnabled(true)
                taskViewModel.setHasRequestedNotificationPermission(true)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    taskViewModel.setNotificationsEnabled(false)
                }
            }
        }
    }

    val hazeState = LocalHazeState.current
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        // BACKGROUND IMAGE LAYER
        val bgPath by taskViewModel.backgroundPath.collectAsState()
        val backgroundVersion by taskViewModel.backgroundVersion.collectAsState()
        val bitmap = remember(bgPath, backgroundVersion) {
            if (bgPath != null) {
                try {
                    val file = File(bgPath)
                    if (file.exists()) BitmapFactory.decodeFile(bgPath) else null
                } catch (e: Exception) { null }
            } else null
        }
        
        val isGlass = themeController.isGlassModeEnabled && bitmap != null && hazeState != null

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isGlass) Modifier.haze(state = hazeState!!) else Modifier)
        ) {
            // Base background color (for when no image is set)
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(themeController.backgroundBlur.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Uniform overlay across the entire screen (including behind nav bar)
            if (bitmap != null && !isGlass) {
                Box(modifier = Modifier.fillMaxSize().background(
                    if (themeController.isDarkTheme) Color.Black.copy(alpha = 0.45f)
                    else Color.White.copy(alpha = 0.45f)
                ))
            }

            // NAVIGATION STRUCTURE
            val displayTabs = listOf(ScreenTab.Home, ScreenTab.Stats, ScreenTab.Settings)
            val isMainTab = currentTab in displayTabs

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                // To solve the FAB position mismatch, we wrap everything in an outer box 
                // and subtract padding manually only for the content, NOT the FAB.
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        @OptIn(ExperimentalFoundationApi::class)
                        val pagerState = rememberPagerState { 3 }
                        
                        // Sync pager with currentTab
                        LaunchedEffect(currentTab) {
                            if (isMainTab) {
                                val targetPage = when (currentTab) {
                                    ScreenTab.Home -> 0
                                    ScreenTab.Stats -> 1
                                    ScreenTab.Settings -> 2
                                    else -> 0
                                }
                                if (pagerState.currentPage != targetPage) {
                                    if (disableAnimations) {
                                        pagerState.scrollToPage(targetPage)
                                    } else {
                                        pagerState.animateScrollToPage(targetPage)
                                    }
                                }
                            }
                        }

                        // Sync currentTab with pager when user swipes
                        LaunchedEffect(pagerState.targetPage, pagerState.isScrollInProgress) {
                            if (isMainTab && pagerState.isScrollInProgress) {
                                val newTab = when (pagerState.targetPage) {
                                    0 -> ScreenTab.Home
                                    1 -> ScreenTab.Stats
                                    2 -> ScreenTab.Settings
                                    else -> ScreenTab.Home
                                }
                                if (currentTab != newTab) {
                                    currentTab = newTab
                                }
                            }
                        }

                        val targetState: Any = if (isMainTab) "MainPager" else currentTab

                        AnimatedContent(
                            targetState = targetState,
                            transitionSpec = {
                                if (disableAnimations) {
                                    EnterTransition.None togetherWith ExitTransition.None
                                } else {
                                    val duration = 240
                                    val easing = FastOutSlowInEasing
                                    val target = targetState
                                    val initial = initialState
                                    
                                    // Use a safe ordinal extraction or default to 0 for "MainPager"
                                    val targetOrdinal = if (target is ScreenTab) target.ordinal else 0
                                    val initialOrdinal = if (initial is ScreenTab) initial.ordinal else 0

                                    if (targetOrdinal > initialOrdinal) {
                                        (slideInHorizontally(animationSpec = tween(duration, easing = easing)) { it } +
                                         fadeIn(animationSpec = tween(duration, easing = easing))).togetherWith(
                                            slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { -it } +
                                            fadeOut(animationSpec = tween(duration / 2))
                                        )
                                    } else {
                                        (slideInHorizontally(animationSpec = tween(duration, easing = easing)) { -it } +
                                         fadeIn(animationSpec = tween(duration, easing = easing))).togetherWith(
                                            slideOutHorizontally(animationSpec = tween(duration, easing = easing)) { it } +
                                            fadeOut(animationSpec = tween(duration / 2))
                                        )
                                    }.using(SizeTransform(clip = true))
                                }
                            },
                            label = "screen_transition"
                        ) { target ->
                            when (target) {
                                "MainPager" -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        @OptIn(ExperimentalFoundationApi::class)
                                        HorizontalPager(
                                            state = pagerState, 
                                            modifier = Modifier.fillMaxSize(),
                                            beyondBoundsPageCount = 2
                                        ) { page ->
                                            when (page) {
                                                0 -> HomeScreen(taskViewModel)
                                                1 -> StatsScreen(viewModel = taskViewModel, onShowPastTasks = { currentTab = ScreenTab.PastTasks })
                                                2 -> SettingsScreen(
                                                    viewModel = taskViewModel, 
                                                    onShowLicenses = { currentTab = ScreenTab.Licenses },
                                                    onShowMITLicense = { currentTab = ScreenTab.MITLicense },
                                                    onShowEditHome = { currentTab = ScreenTab.EditHome },
                                                    onBackup = { exportLauncher.launch("m4task_backup.zip") },
                                                    onRestore = { importLauncher.launch(arrayOf("application/zip")) },
                                                    onPickBackground = { bgLauncher.launch("image/*") }
                                                )
                                            }
                                        }
                                        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                                            FloatingBottomNav(
                                                currentTab = currentTab,
                                                onTabSelected = { currentTab = it }
                                            )
                                        }
                                    }
                                }
                                ScreenTab.Licenses -> {
                                    BackHandler { currentTab = ScreenTab.Settings }
                                    LicensesScreen(onBack = { currentTab = ScreenTab.Settings })
                                }
                                ScreenTab.MITLicense -> {
                                    BackHandler { currentTab = ScreenTab.Settings }
                                    MITLicenseScreen(onBack = { currentTab = ScreenTab.Settings })
                                }
                                ScreenTab.PastTasks -> {
                                    BackHandler { currentTab = ScreenTab.Stats }
                                    PastTasksScreen(viewModel = taskViewModel, onBack = { currentTab = ScreenTab.Stats })
                                }
                                ScreenTab.EditHome -> {
                                    BackHandler { currentTab = ScreenTab.Settings }
                                    EditHomeScreen(viewModel = taskViewModel, onBack = { currentTab = ScreenTab.Settings })
                                }
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
                        val finalPath = activity.saveBackgroundImage(editingBgUri!!, scale, tx, ty)
                        taskViewModel.updateBackgroundPath(finalPath)
                        themeController.setBackgroundBlur(if (blurEnabled) 15f else 0f)
                        editingBgUri = null
                    },
                    onCancel = { editingBgUri = null }
                )
            }

            val enterToAdd by taskViewModel.enterToAdd.collectAsState()
            if (showAddDialog) {
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
                    onDismissRequest = { showAddDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    taskViewModel.addTask(newTaskTitle, isTaskStarred)
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
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = if (enterToAdd) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Default
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = {
                                    if (enterToAdd && newTaskTitle.isNotBlank()) {
                                        taskViewModel.addTask(newTaskTitle, isTaskStarred)
                                        newTaskTitle = ""
                                        showAddDialog = false
                                    }
                                }
                            )
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}
