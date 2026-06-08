package com.m4sak1.taskapp.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NewReleaseDialog(
    release: GithubRelease,
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onIgnoreRequest: () -> Unit,
    disableAnimations: Boolean
) {
    if (!visible && disableAnimations) return
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    CustomInfoDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = "新しいアップデートがあります！",
        confirmText = "閉じる",
        disableAnimations = disableAnimations
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "バージョン ${release.tagName} が利用可能です。",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    try {
                        uriHandler.openUri("https://github.com/masaki-09/m4sak1-task-app/releases/latest")
                    } catch (e: Exception) {}
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ダウンロードページへ")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onIgnoreRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("このバージョンを無視する")
            }
        }
    }
}
