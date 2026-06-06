package com.m4sak1.taskapp.data

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
    val customAccentColor: Long = 0xFF000000,
    val backgroundBlur: Float = 0f,
    val hasBackground: Boolean = false,
    val fabOffsetX: Float,
    val fabOffsetY: Float,
    val hideImmediately: Boolean
)