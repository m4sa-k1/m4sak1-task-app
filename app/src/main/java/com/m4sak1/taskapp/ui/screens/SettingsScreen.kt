package com.m4sak1.taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.ui.components.CustomConfirmDialog
import com.m4sak1.taskapp.ui.theme.AppLanguage
import com.m4sak1.taskapp.ui.theme.AppThemeMode
import com.m4sak1.taskapp.ui.theme.LocalThemeController
import com.m4sak1.taskapp.viewmodel.TaskViewModel

@Composable
fun SettingsScreen(
    viewModel: TaskViewModel, 
    onShowLicenses: () -> Unit, 
    onShowMITLicense: () -> Unit,
    onShowEditHome: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    val themeController = LocalThemeController.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    val hideImmediately by viewModel.hideImmediately.collectAsState()

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
            SettingsItem(
                title = stringResource(R.string.settings_dark_mode),
                contentText = getThemeModeName(themeController.themeMode),
                modifier = Modifier.clickable { showThemeDialog = true }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_language),
                contentText = getLanguageName(themeController.appLanguage),
                modifier = Modifier.clickable { showLanguageDialog = true }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_immediate_hide),
                trailingContent = {
                    Switch(
                        checked = hideImmediately,
                        onCheckedChange = { viewModel.toggleHideImmediately(it) }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "Customize") {
            SettingsItem(
                title = stringResource(R.string.settings_edit_home),
                modifier = Modifier.clickable { onShowEditHome() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = stringResource(R.string.settings_backup)) {
            SettingsItem(
                title = stringResource(R.string.settings_export),
                modifier = Modifier.clickable { onBackup() }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_import),
                modifier = Modifier.clickable { showRestoreConfirm = true }
            )
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
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_mit_license),
                modifier = Modifier.clickable { onShowMITLicense() }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_about),
                modifier = Modifier.clickable { showAboutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "m4 task",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_footer),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }

    if (showThemeDialog) {
        CustomConfirmDialog(
            title = stringResource(R.string.settings_dark_mode),
            onConfirm = { showThemeDialog = false },
            onDismiss = { showThemeDialog = false },
            confirmText = "OK"
        ) {
            Column {
                AppThemeMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                themeController.setThemeMode(mode)
                                showThemeDialog = false
                            }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(selected = themeController.themeMode == mode, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = getThemeModeName(mode))
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        CustomConfirmDialog(
            title = stringResource(R.string.settings_language),
            onConfirm = { showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false },
            confirmText = "OK"
        ) {
            Column {
                AppLanguage.entries.forEach { lang ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                themeController.setAppLanguage(lang)
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(selected = themeController.appLanguage == lang, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = getLanguageName(lang))
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        CustomConfirmDialog(
            title = stringResource(R.string.confirm),
            onConfirm = { showAboutDialog = false },
            onDismiss = { showAboutDialog = false },
            confirmText = "OK"
        ) {
            Column {
                Text("m4 task")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Maintained by m4sak1")
            }
        }
    }

    if (showRestoreConfirm) {
        CustomConfirmDialog(
            title = stringResource(R.string.confirm),
            onConfirm = {
                onRestore()
                showRestoreConfirm = false
            },
            onDismiss = { showRestoreConfirm = false },
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(R.string.cancel),
            confirmColor = MaterialTheme.colorScheme.error
        ) {
            Text(stringResource(R.string.restore_warning))
        }
    }
}

@Composable
private fun getThemeModeName(mode: AppThemeMode): String = when (mode) {
    AppThemeMode.System -> "System Default"
    AppThemeMode.Light -> "Light"
    AppThemeMode.Dark -> "Dark"
}

@Composable
private fun getLanguageName(language: AppLanguage): String = when (language) {
    AppLanguage.System -> "System Default"
    AppLanguage.English -> "English"
    AppLanguage.Japanese -> "日本語"
    AppLanguage.SimplifiedChinese -> "简体中文"
    AppLanguage.TraditionalChinese -> "繁體中文"
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
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
            fontWeight = FontWeight.Normal
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