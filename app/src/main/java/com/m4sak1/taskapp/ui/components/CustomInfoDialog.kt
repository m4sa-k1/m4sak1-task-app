package com.m4sak1.taskapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

import androidx.compose.ui.res.stringResource
import com.m4sak1.taskapp.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomInfoDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String? = null,
    disableAnimations: Boolean = false,
    content: @Composable () -> Unit
) {
    var showDialog by remember(visible) { mutableStateOf(visible) }
    var animateIn by remember { mutableStateOf(false) }

    val actualConfirmText = confirmText ?: stringResource(id = R.string.close)

    LaunchedEffect(visible) {
        if (visible) {
            showDialog = true
            animateIn = true
        } else {
            animateIn = false
            if (!disableAnimations) delay(250)
            showDialog = false
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = {
                animateIn = false
                if (disableAnimations) onDismissRequest() else onDismissRequest()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            val dialogWindowProvider = androidx.compose.ui.platform.LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider
            dialogWindowProvider?.window?.setWindowAnimations(-1)

            // Full screen overlay with fade
            AnimatedVisibility(
                visible = animateIn,
                enter = if (disableAnimations) EnterTransition.None else fadeIn(animationSpec = tween(250)),
                exit = if (disableAnimations) ExitTransition.None else fadeOut(animationSpec = tween(250))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                animateIn = false
                                onDismissRequest()
                            }
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Bottom sheet content sliding up
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateEnterExit(
                                enter = if (disableAnimations) EnterTransition.None else slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(250)
                                ),
                                exit = if (disableAnimations) ExitTransition.None else slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(200)
                                )
                            )
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {} // Consume clicks inside the sheet
                            )
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                            .navigationBarsPadding() // Keep it above navigation bars
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Small drag handle visual
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                content()
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Button(
                                onClick = {
                                    onDismissRequest()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(text = actualConfirmText, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
