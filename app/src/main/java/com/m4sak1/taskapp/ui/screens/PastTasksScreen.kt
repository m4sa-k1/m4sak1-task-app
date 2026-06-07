package com.m4sak1.taskapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.data.Task
import com.m4sak1.taskapp.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastTasksScreen(viewModel: TaskViewModel, onBack: () -> Unit) {
    val completedTasks by viewModel.allCompletedTasks.collectAsState(initial = emptyList())
    
    var selectionMode by remember { mutableStateOf(false) }
    var selectedTasks by remember { mutableStateOf(setOf<Task>()) }

    BackHandler(enabled = selectionMode) {
        selectionMode = false
        selectedTasks = emptySet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (selectionMode) "${selectedTasks.size} selected" else stringResource(R.string.past_tasks)) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectionMode) {
                            selectionMode = false
                            selectedTasks = emptySet()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    if (!selectionMode && completedTasks.isNotEmpty()) {
                        IconButton(onClick = { selectionMode = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Tasks")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (selectionMode) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            selectionMode = false
                            selectedTasks = emptySet()
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(
                            onClick = {
                                viewModel.deleteTasks(selectedTasks.toList())
                                selectionMode = false
                                selectedTasks = emptySet()
                            },
                            enabled = selectedTasks.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (completedTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.no_tasks), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                items(completedTasks, key = { it.id }) { task ->
                    PastTaskItem(
                        task = task,
                        selectionMode = selectionMode,
                        isSelected = selectedTasks.contains(task),
                        onToggle = {
                            if (selectionMode) {
                                selectedTasks = if (selectedTasks.contains(task)) {
                                    selectedTasks - task
                                } else {
                                    selectedTasks + task
                                }
                            } else {
                                viewModel.toggleTaskCompletion(task)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PastTaskItem(task: Task, selectionMode: Boolean, isSelected: Boolean, onToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null) { onToggle() }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectionMode && isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectionMode && isSelected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = task.title,
                fontSize = 18.sp,
                color = if (!selectionMode) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (!selectionMode) TextDecoration.LineThrough else TextDecoration.None
            )
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    }
}