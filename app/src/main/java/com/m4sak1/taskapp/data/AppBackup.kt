package com.m4sak1.taskapp.data

import com.m4sak1.taskapp.ui.theme.AppLanguage
import com.m4sak1.taskapp.ui.theme.AppThemeMode
import kotlinx.serialization.Serializable

@Serializable
data class AppBackup(
    val tasks: List<TaskBackup>,
    val settings: SettingsBackup
)

@Serializable
data class TaskBackup(
    val title: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val completedAt: Long?
)

@Serializable
data class SettingsBackup(
    val themeMode: String,
    val appLanguage: String,
    val accentColor: String = "Default",
    val fabOffsetX: Float,
    val fabOffsetY: Float,
    val hideImmediately: Boolean
)