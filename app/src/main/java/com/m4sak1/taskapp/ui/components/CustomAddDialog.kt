package com.m4sak1.taskapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m4sak1.taskapp.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomAddDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onAddTask: (String, Boolean) -> Unit,
    enterToAdd: Boolean,
    style: com.m4sak1.taskapp.data.AppAddDialogStyle = com.m4sak1.taskapp.data.AppAddDialogStyle.Center,
    disableAnimations: Boolean = false
) {
    var newTaskTitle by remember { mutableStateOf("") }
    var isTaskStarred by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(visible) {
        if (visible) {
            newTaskTitle = ""
            isTaskStarred = false
            delay(50) // Wait for animation to start
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    // Full screen overlay with fade
    AnimatedVisibility(
        visible = visible,
        enter = if (disableAnimations) EnterTransition.None else fadeIn(animationSpec = tween(200)),
        exit = if (disableAnimations) ExitTransition.None else fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.Center) it.imePadding() else it },
                contentAlignment = if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet) Alignment.BottomCenter else Alignment.Center
            ) {
                // Dialog content
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet) 1f else 0.85f)
                        .animateEnterExit(
                            enter = if (disableAnimations) EnterTransition.None else {
                                if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet) {
                                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(250))
                                } else {
                                    scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = tween(300))
                                }
                            },
                            exit = if (disableAnimations) ExitTransition.None else {
                                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))
                            }
                        )
                        .clip(
                            if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet) 
                                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) 
                            else 
                                RoundedCornerShape(28.dp)
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Consume clicks so they don't dismiss the dialog
                        )
                        .let { 
                            if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet) {
                                it.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                            } else {
                                it
                            }
                        }
                        .padding(horizontal = if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet) 16.dp else 24.dp, vertical = if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet) 16.dp else 24.dp)
                ) {
                    if (style == com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = if (enterToAdd) ImeAction.Done else ImeAction.Default
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (enterToAdd && newTaskTitle.isNotBlank()) {
                                            onAddTask(newTaskTitle, isTaskStarred)
                                        }
                                    }
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (newTaskTitle.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.task_placeholder),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                fontSize = 18.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { isTaskStarred = !isTaskStarred }) {
                                    Text(
                                        text = if (isTaskStarred) "★" else "☆",
                                        fontSize = 28.sp,
                                        color = if (isTaskStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        if (newTaskTitle.isNotBlank()) {
                                            onAddTask(newTaskTitle, isTaskStarred)
                                        }
                                    },
                                    enabled = newTaskTitle.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(stringResource(R.string.add_task), color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = stringResource(R.string.new_task),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
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
                                keyboardOptions = KeyboardOptions(
                                    imeAction = if (enterToAdd) ImeAction.Done else ImeAction.Default
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (enterToAdd && newTaskTitle.isNotBlank()) {
                                            onAddTask(newTaskTitle, isTaskStarred)
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = onDismissRequest) {
                                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newTaskTitle.isNotBlank()) {
                                            onAddTask(newTaskTitle, isTaskStarred)
                                        }
                                    },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(stringResource(R.string.add_task), color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
