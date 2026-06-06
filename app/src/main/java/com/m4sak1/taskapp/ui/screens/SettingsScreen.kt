package com.m4sak1.taskapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.m4sak1.taskapp.MainActivity
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.ui.components.CustomConfirmDialog
import com.m4sak1.taskapp.ui.theme.AppAccentColor
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
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    val hideImmediately by viewModel.hideImmediately.collectAsState()

    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val path = (context as MainActivity).saveBackgroundImage(it)
            themeController.setBackgroundPath(path)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        SettingsSection(title = stringResource(R.string.customize)) {
            SettingsItem(
                title = "Accent Color",
                contentText = if (themeController.accentColor == AppAccentColor.Custom) {
                    "#" + Integer.toHexString(themeController.customAccentColor.toArgb()).uppercase().takeLast(6)
                } else {
                    themeController.accentColor.label
                },
                modifier = Modifier.clickable { showAccentDialog = true },
                trailingContent = {
                    val color = if (themeController.accentColor == AppAccentColor.Custom) themeController.customAccentColor else themeController.accentColor.color
                    if (color != Color.Unspecified) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color))
                    }
                }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = "Background Image",
                modifier = Modifier.clickable { bgLauncher.launch("image/*") },
                trailingContent = {
                    if (themeController.backgroundPath != null) {
                        TextButton(onClick = { themeController.setBackgroundPath(null) }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
            if (themeController.backgroundPath != null) {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Blur Amount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Slider(
                        value = themeController.backgroundBlur,
                        onValueChange = { themeController.setBackgroundBlur(it) },
                        valueRange = 0f..25f
                    )
                }
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
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
        
        Spacer(modifier = Modifier.height(100.dp)) // Extra space for footer
    }

    if (showThemeDialog) {
        CustomConfirmDialog(
            title = stringResource(R.string.settings_dark_mode),
            onConfirm = { showThemeDialog = false },
            onDismiss = { showThemeDialog = false },
            confirmText = stringResource(R.string.ok)
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

    if (showAccentDialog) {
        var customHex by remember { mutableStateOf("") }
        CustomConfirmDialog(
            title = "Accent Color",
            onConfirm = { showAccentDialog = false },
            onDismiss = { showAccentDialog = false },
            confirmText = stringResource(R.string.ok)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AppAccentColor.entries.forEach { accent ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                themeController.setAccentColor(accent)
                                if (accent != AppAccentColor.Custom) showAccentDialog = false
                            }
                            .padding(vertical = 12.dp)
                    ) {
                        RadioButton(selected = themeController.accentColor == accent, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = accent.label, color = if (accent == AppAccentColor.Default || accent == AppAccentColor.Custom) MaterialTheme.colorScheme.onSurface else accent.color)
                        if (accent != AppAccentColor.Default && accent != AppAccentColor.Custom) {
                            Spacer(modifier = Modifier.weight(1f))
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(accent.color))
                        }
                    }
                }
                if (themeController.accentColor == AppAccentColor.Custom) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customHex,
                        onValueChange = { input ->
                            if (input.length <= 7) {
                                customHex = input
                                if (input.startsWith("#") && input.length == 7) {
                                    try { themeController.setCustomAccentColor(Color(android.graphics.Color.parseColor(input))) } catch (e: Exception) {}
                                }
                            }
                        },
                        label = { Text("Hex Color (e.g. #FF5533)") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        singleLine = true
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        CustomConfirmDialog(
            title = stringResource(R.string.settings_language),
            onConfirm = { showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false },
            confirmText = stringResource(R.string.ok)
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
                        Text(text = getLanguageLabel(lang))
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
            confirmText = stringResource(R.string.ok)
        ) {
            Column {
                Text("m4 task")
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.maintained_by))
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
    AppThemeMode.System -> stringResource(R.string.system_default)
    AppThemeMode.Light -> stringResource(R.string.theme_light)
    AppThemeMode.Dark -> stringResource(R.string.theme_dark)
}

@Composable
private fun getLanguageName(language: AppLanguage): String = when (language) {
    AppLanguage.System -> stringResource(R.string.system_default)
    AppLanguage.English -> "English"
    AppLanguage.Japanese -> "日本語"
    AppLanguage.SimplifiedChinese -> "简体中文"
    AppLanguage.TraditionalChinese -> "繁體中文"
}

private fun getLanguageLabel(language: AppLanguage): String = when (language) {
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
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) // Semi-transparent for BG visibility
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