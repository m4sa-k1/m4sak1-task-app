package com.m4sak1.taskapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.m4sak1.taskapp.BuildConfig
import com.m4sak1.taskapp.MainActivity
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.ui.components.CustomConfirmDialog
import com.m4sak1.taskapp.ui.components.CustomInfoDialog
import com.m4sak1.taskapp.ui.theme.AppAccentColor
import com.m4sak1.taskapp.ui.theme.AppLanguage
import com.m4sak1.taskapp.ui.theme.AppThemeMode
import com.m4sak1.taskapp.ui.theme.LocalThemeController
import com.m4sak1.taskapp.ui.theme.LocalThemeController
import com.m4sak1.taskapp.viewmodel.TaskViewModel
import androidx.compose.foundation.border

@Composable
fun SettingsScreen(
    viewModel: TaskViewModel, 
    onShowLicenses: () -> Unit, 
    onShowMITLicense: () -> Unit,
    onShowEditHome: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onPickBackground: () -> Unit,
    scrollState: androidx.compose.foundation.ScrollState = androidx.compose.foundation.rememberScrollState()
) {
    val themeController = LocalThemeController.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    val hideImmediately by viewModel.hideImmediately.collectAsState()
    val disableAnimations by viewModel.disableAnimations.collectAsState()
    val enterToAdd by viewModel.enterToAdd.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Spacer(modifier = Modifier.height(16.dp))
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
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_disable_animations),
                trailingContent = {
                    Switch(
                        checked = disableAnimations,
                        onCheckedChange = { viewModel.toggleDisableAnimations(it) }
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_enter_to_add),
                trailingContent = {
                    Switch(
                        checked = enterToAdd,
                        onCheckedChange = { viewModel.setEnterToAdd(it) }
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            val highlightOldTasks by viewModel.highlightOldTasks.collectAsState()
            SettingsItem(
                title = stringResource(R.string.settings_highlight_old_tasks),
                contentText = stringResource(R.string.settings_highlight_old_tasks_desc),
                trailingContent = {
                    Switch(
                        checked = highlightOldTasks,
                        onCheckedChange = { viewModel.setHighlightOldTasks(it) }
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            val addDialogStyle by viewModel.addDialogStyle.collectAsState()
            SettingsItem(
                title = stringResource(R.string.add_task_dialog_style),
                contentText = if (addDialogStyle == com.m4sak1.taskapp.data.AppAddDialogStyle.Center) stringResource(R.string.dialog_style_center) else stringResource(R.string.dialog_style_bottom_sheet),
                modifier = Modifier.clickable { viewModel.setAddDialogStyle(if (addDialogStyle == com.m4sak1.taskapp.data.AppAddDialogStyle.Center) com.m4sak1.taskapp.data.AppAddDialogStyle.BottomSheet else com.m4sak1.taskapp.data.AppAddDialogStyle.Center) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = stringResource(R.string.customize)) {
            SettingsItem(
                title = stringResource(R.string.settings_accent_color),
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
                title = stringResource(R.string.settings_background_image),
                modifier = Modifier.clickable { onPickBackground() },
                trailingContent = {
                    if (themeController.backgroundPath != null) {
                        TextButton(onClick = { 
                            themeController.setBackgroundPath(null)
                        }) {
                            Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_edit_home),
                modifier = Modifier.clickable { onShowEditHome() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.setNotificationsEnabled(true)
            } else {
                viewModel.setNotificationsEnabled(false)
            }
        }

        SettingsSection(title = stringResource(R.string.settings_notifications)) {
            SettingsItem(
                title = stringResource(R.string.settings_notifications_enable),
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                                        viewModel.setNotificationsEnabled(true)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    viewModel.setNotificationsEnabled(true)
                                }
                            } else {
                                viewModel.setNotificationsEnabled(false)
                            }
                        }
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_notifications_test),
                modifier = Modifier.clickable { 
                    if (notificationsEnabled) {
                        viewModel.sendTestNotification()
                    }
                }
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
            SettingsItem(
                title = stringResource(R.string.settings_version), 
                contentText = BuildConfig.VERSION_NAME,
                modifier = Modifier.clickable { showChangelogDialog = true }
            )
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
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            SettingsItem(
                title = stringResource(R.string.settings_disclaimer),
                modifier = Modifier.clickable { showDisclaimerDialog = true }
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
        
        Spacer(modifier = Modifier.height(100.dp))
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
            title = stringResource(R.string.settings_accent_color),
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
                        label = { Text(stringResource(R.string.hex_color_hint)) },
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

    CustomInfoDialog(
        visible = showAboutDialog,
        onDismissRequest = { showAboutDialog = false },
        title = stringResource(R.string.settings_about),
        confirmText = stringResource(R.string.close),
        disableAnimations = disableAnimations
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "m4 task",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.maintained_by),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }

    CustomInfoDialog(
        visible = showDisclaimerDialog,
        onDismissRequest = { showDisclaimerDialog = false },
        title = stringResource(R.string.settings_disclaimer),
        confirmText = stringResource(R.string.close),
        disableAnimations = disableAnimations
    ) {
        Text(
            text = stringResource(R.string.disclaimer_text),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    com.m4sak1.taskapp.ui.components.ReleaseHistoryDialog(
        visible = showChangelogDialog,
        onDismissRequest = { showChangelogDialog = false },
        disableAnimations = disableAnimations
    )

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
    val containerModifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(vertical = 4.dp)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Column(
            modifier = containerModifier
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