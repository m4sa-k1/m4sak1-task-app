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
    disableAnimations: Boolean
) {
    if (!visible && disableAnimations) return
    val context = LocalContext.current

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
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/masaki-09/m4sak1-task-app/releases/latest"))
                    context.startActivity(intent)
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ダウンロードページへ")
            }
        }
    }
}
