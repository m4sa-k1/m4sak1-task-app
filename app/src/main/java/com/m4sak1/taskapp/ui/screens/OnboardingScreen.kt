package com.m4sak1.taskapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.data.AppAddDialogStyle
import com.m4sak1.taskapp.data.PreferenceManager
import com.m4sak1.taskapp.ui.theme.AppLanguage
import com.m4sak1.taskapp.ui.theme.AppThemeMode
import com.m4sak1.taskapp.ui.theme.ThemeController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    themeController: ThemeController,
    prefManager: PreferenceManager,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false // Prevent manual swiping to enforce flow
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> OnboardingLanguageStep(
                            themeController = themeController,
                            onNext = { scope.launch { pagerState.animateScrollToPage(1) } }
                        )
                        1 -> OnboardingAccentColorStep(
                            themeController = themeController,
                            onNext = { scope.launch { pagerState.animateScrollToPage(2) } }
                        )
                        2 -> OnboardingThemeStep(
                            themeController = themeController,
                            onNext = { scope.launch { pagerState.animateScrollToPage(3) } }
                        )
                        3 -> OnboardingAddStyleStep(
                            prefManager = prefManager,
                            onNext = { scope.launch { pagerState.animateScrollToPage(4) } }
                        )
                        4 -> OnboardingHideImmediatelyStep(
                            prefManager = prefManager,
                            onNext = { scope.launch { pagerState.animateScrollToPage(5) } }
                        )
                        5 -> OnboardingEnterToAddStep(
                            prefManager = prefManager,
                            onFinish = {
                                prefManager.isOnboardingCompleted = true
                                onFinish()
                            }
                        )
                    }
                }
            }
            
            // Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val isSelected = pagerState.currentPage == index
                    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp, label = "indicator_width")
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = width, height = 8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingLanguageStep(
    themeController: ThemeController,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please select your language to get started.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val languages = listOf(
            AppLanguage.System to "System Default",
            AppLanguage.English to "English",
            AppLanguage.Japanese to "日本語",
            AppLanguage.SimplifiedChinese to "简体中文",
            AppLanguage.TraditionalChinese to "繁體中文"
        )
        
        languages.forEach { (lang, label) ->
            val isSelected = themeController.appLanguage == lang
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                onClick = { themeController.setAppLanguage(lang) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OnboardingAccentColorStep(
    themeController: ThemeController,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.settings_accent_color),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        val colors = com.m4sak1.taskapp.ui.theme.AppAccentColor.values()
        
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(colors.size) { index ->
                val colorOption = colors[index]
                val isSelected = themeController.accentColor == colorOption
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (colorOption == com.m4sak1.taskapp.ui.theme.AppAccentColor.Custom) MaterialTheme.colorScheme.surfaceVariant else colorOption.color)
                        .clickable { themeController.setAccentColor(colorOption) }
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = if (colorOption == com.m4sak1.taskapp.ui.theme.AppAccentColor.Custom) MaterialTheme.colorScheme.primary else Color.White
                        )
                    } else if (colorOption == com.m4sak1.taskapp.ui.theme.AppAccentColor.Custom) {
                        Text(stringResource(id = R.string.customize), fontSize = 10.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(id = R.string.ok), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OnboardingThemeStep(
    themeController: ThemeController,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.settings_dark_mode),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Visual Representation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ThemeOptionMock(
                label = stringResource(id = R.string.theme_light),
                isDark = false,
                isSelected = themeController.themeMode == AppThemeMode.Light,
                onClick = { themeController.setThemeMode(AppThemeMode.Light) }
            )
            ThemeOptionMock(
                label = stringResource(id = R.string.theme_dark),
                isDark = true,
                isSelected = themeController.themeMode == AppThemeMode.Dark,
                onClick = { themeController.setThemeMode(AppThemeMode.Dark) }
            )
            ThemeOptionMock(
                label = stringResource(id = R.string.system_default),
                isDark = themeController.isDarkTheme, // Just represent current system state
                isSelected = themeController.themeMode == AppThemeMode.System,
                onClick = { themeController.setThemeMode(AppThemeMode.System) }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(id = R.string.ok), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ThemeOptionMock(
    label: String,
    isDark: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val contentColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1E1E1E)
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp, 120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Mock Header
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).background(contentColor.copy(alpha = 0.1f)))
                Spacer(modifier = Modifier.height(8.dp))
                // Mock Item
                Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)).background(contentColor.copy(alpha = 0.2f)))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)).background(contentColor.copy(alpha = 0.2f)))
                Spacer(modifier = Modifier.weight(1f))
                // Mock FAB
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(primaryColor).align(Alignment.End))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun OnboardingAddStyleStep(
    prefManager: PreferenceManager,
    onNext: () -> Unit
) {
    var addStyle by remember { mutableStateOf(prefManager.addDialogStyle) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.add_task_dialog_style),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AddStyleMock(
                label = stringResource(id = R.string.dialog_style_center),
                style = AppAddDialogStyle.Center,
                isSelected = addStyle == AppAddDialogStyle.Center,
                onClick = { 
                    addStyle = AppAddDialogStyle.Center
                    prefManager.addDialogStyle = addStyle
                }
            )
            AddStyleMock(
                label = stringResource(id = R.string.dialog_style_bottom_sheet),
                style = AppAddDialogStyle.BottomSheet,
                isSelected = addStyle == AppAddDialogStyle.BottomSheet,
                onClick = { 
                    addStyle = AppAddDialogStyle.BottomSheet
                    prefManager.addDialogStyle = addStyle
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(id = R.string.ok), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddStyleMock(
    label: String,
    style: AppAddDialogStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surface
    val onBgColor = MaterialTheme.colorScheme.onSurface

    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            while(true) {
                animateTrigger = true
                delay(1500)
                animateTrigger = false
                delay(1000)
            }
        } else {
            animateTrigger = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp, 160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor.copy(alpha = 0.5f))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // Background app representation
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).background(onBgColor.copy(alpha = 0.1f)))
            }

            // Dark overlay
            Box(modifier = Modifier.fillMaxSize()) {
                this@Column.AnimatedVisibility(
                    visible = animateTrigger,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                }
            }

            // Dialog Mock
            Box(modifier = Modifier.fillMaxSize()) {
                if (style == AppAddDialogStyle.Center) {
                    this@Column.AnimatedVisibility(
                        visible = animateTrigger,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Box(modifier = Modifier.size(80.dp, 60.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                    }
                } else {
                    this@Column.AnimatedVisibility(
                        visible = animateTrigger,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun OnboardingHideImmediatelyStep(
    prefManager: PreferenceManager,
    onNext: () -> Unit
) {
    var hideImmediately by remember { mutableStateOf(prefManager.hideImmediately) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.settings_immediate_hide),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HideImmediatelyMock(
                label = "On",
                isHide = true,
                isSelected = hideImmediately,
                onClick = { 
                    hideImmediately = true
                    prefManager.hideImmediately = hideImmediately
                }
            )
            HideImmediatelyMock(
                label = "Off",
                isHide = false,
                isSelected = !hideImmediately,
                onClick = { 
                    hideImmediately = false
                    prefManager.hideImmediately = hideImmediately
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(id = R.string.ok), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HideImmediatelyMock(
    label: String,
    isHide: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    var isChecked by remember { mutableStateOf(false) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            while(true) {
                isChecked = false
                delay(1000)
                isChecked = true
                delay(1500)
            }
        } else {
            isChecked = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp, 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                this@Column.AnimatedVisibility(
                    visible = !isChecked || !isHide,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (isChecked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.width(48.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isChecked) 0.3f else 1f)))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun OnboardingEnterToAddStep(
    prefManager: PreferenceManager,
    onFinish: () -> Unit
) {
    var enterToAdd by remember { mutableStateOf(prefManager.enterToAdd) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.settings_enter_to_add),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnterToAddMock(
                label = "On",
                isEnter = true,
                isSelected = enterToAdd,
                onClick = { 
                    enterToAdd = true
                    prefManager.enterToAdd = enterToAdd
                }
            )
            EnterToAddMock(
                label = "Off",
                isEnter = false,
                isSelected = !enterToAdd,
                onClick = { 
                    enterToAdd = false
                    prefManager.enterToAdd = enterToAdd
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Let's Go!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EnterToAddMock(
    label: String,
    isEnter: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    var showTask by remember { mutableStateOf(false) }
    var keyPress by remember { mutableStateOf(false) }
    
    LaunchedEffect(isSelected) {
        if (isSelected) {
            while(true) {
                showTask = false
                keyPress = false
                delay(800)
                keyPress = true
                delay(150) // Reduced key press time so it feels faster and snappy
                keyPress = false
                if (isEnter) {
                    showTask = true
                }
                delay(1200) // Keep the task visible for a while before looping
            }
        } else {
            showTask = false
            keyPress = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp, 120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Task List Area
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        this@Column.AnimatedVisibility(
                            visible = showTask,
                            enter = slideInVertically { it / 2 } + fadeIn(), // Slightly shorter slide for snappier animation
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Keyboard Area
                Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))) {
                    // Enter key
                    val keyColor by animateColorAsState(if (keyPress) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    val keyScale by animateFloatAsState(if (keyPress) 0.9f else 1f)
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(4.dp)
                            .size(28.dp, 32.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .graphicsLayer(scaleX = keyScale, scaleY = keyScale)
                            .background(keyColor)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onBackground)
    }
}
