package com.m4sak1.taskapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.m4sak1.taskapp.ui.theme.LocalThemeController
import androidx.compose.foundation.border

@Composable
fun CustomConfirmDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "OK",
    dismissText: String? = null,
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val themeController = LocalThemeController.current
    val isGlass = themeController.isGlassModeEnabled

    val modifier = if (isGlass) {
        Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight()
            .clip(RoundedCornerShape(28.dp))
            .background(if (themeController.isDarkTheme) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f))
            .border(
                1.dp,
                if (themeController.isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f),
                RoundedCornerShape(28.dp)
            )
    } else {
        Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight()
            .clip(RoundedCornerShape(28.dp))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier,
            color = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isGlass) 0.dp else 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissText != null) {
                        TextButton(onClick = onDismiss) {
                            Text(text = dismissText, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(onClick = onConfirm) {
                        Text(text = confirmText, color = confirmColor)
                    }
                }
            }
        }
    }
}