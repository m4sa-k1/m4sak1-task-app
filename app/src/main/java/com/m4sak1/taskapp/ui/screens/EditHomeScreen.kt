package com.m4sak1.taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.ScreenTab
import com.m4sak1.taskapp.ui.components.FloatingBottomNav
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHomeScreen(viewModel: TaskViewModel, onBack: () -> Unit) {
    val initialX by viewModel.fabOffsetX.collectAsState()
    val initialY by viewModel.fabOffsetY.collectAsState()
    
    var offsetX by remember { mutableStateOf(initialX) }
    var offsetY by remember { mutableStateOf(initialY) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings_edit_home),
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateFabPosition(offsetX, offsetY)
                            onBack()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background preview of the Home Screen
            HomeScreen(viewModel = viewModel)

            // Realistic Footer Preview (Non-interactive)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingBottomNav(
                    currentTab = ScreenTab.Home,
                    onTabSelected = {},
                    modifier = Modifier.alpha(0.5f)
                )
            }

            // Draggable FAB - Using same padding (16.dp) as MainScreen for 1:1 match
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                FloatingActionButton(
                    onClick = { /* Preview only */ },
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            // Reset Button positioned clearly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Button(
                    onClick = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(stringResource(R.string.reset_default))
                }
            }
        }
    }
}