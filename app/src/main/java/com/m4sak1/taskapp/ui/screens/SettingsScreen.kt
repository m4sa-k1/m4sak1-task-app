package com.m4sak1.taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.m4sak1.taskapp.ui.theme.LocalThemeController
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import com.m4sak1.taskapp.R

@Composable
fun SettingsScreen(onShowLicenses: () -> Unit) {
    val themeController = LocalThemeController.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = stringResource(R.string.tab_settings),
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        SettingsSection(title = stringResource(R.string.settings_general)) {
            SettingsItem(title = stringResource(R.string.settings_dark_mode)) {
                Switch(
                    checked = themeController.isDarkTheme,
                    onCheckedChange = { themeController.toggleTheme(it) }
                )
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(title = stringResource(R.string.settings_show_loading)) {
                Switch(checked = true, onCheckedChange = {})
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = stringResource(R.string.settings_app_info)) {
            SettingsItem(title = stringResource(R.string.settings_version), contentText = "1.0.0")
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_licenses), 
                contentText = stringResource(R.string.settings_details),
                modifier = Modifier.clickable { onShowLicenses() }
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 4.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    contentText: String? = null,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        if (trailingContent != null) {
            trailingContent()
        } else if (contentText != null) {
            Text(
                text = contentText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}