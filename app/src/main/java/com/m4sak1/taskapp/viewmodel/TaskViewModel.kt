package com.m4sak1.taskapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.m4sak1.taskapp.data.*
import com.m4sak1.taskapp.ui.theme.AppAccentColor
import com.m4sak1.taskapp.ui.theme.AppLanguage
import com.m4sak1.taskapp.ui.theme.AppThemeMode
import com.m4sak1.taskapp.ui.theme.ThemeController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    private val _recentlyCompletedTasks = MutableStateFlow<List<Task>>(emptyList())
    
    private val _hideImmediately = MutableStateFlow(false)
    val hideImmediately = _hideImmediately.asStateFlow()

    private val _fabOffsetX = MutableStateFlow(0f)
    val fabOffsetX = _fabOffsetX.asStateFlow()
    private val _fabOffsetY = MutableStateFlow(-240f) 
    val fabOffsetY = _fabOffsetY.asStateFlow()

    val allCompletedTasks: Flow<List<Task>> = taskDao.getAllCompletedTasks()

    val uiTasks: StateFlow<List<Task>> = combine(
        taskDao.getIncompleteTasks(),
        _recentlyCompletedTasks
    ) { incomplete, recentlyCompleted ->
        (incomplete + recentlyCompleted).sortedByDescending { it.id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalCompletedCount = allCompletedTasks.map { it.size }
    val quickCompletionRate = allCompletedTasks.map { tasks ->
        if (tasks.isEmpty()) 0f
        else {
            val quickOnes = tasks.count { it.completedAt != null && (it.completedAt - it.createdAt) < 24 * 60 * 60 * 1000L }
            quickOnes.toFloat() / tasks.size
        }
    }

    fun toggleHideImmediately(hide: Boolean) {
        _hideImmediately.value = hide
        if (hide) {
            _recentlyCompletedTasks.value = emptyList()
        }
    }

    fun updateFabPosition(x: Float, y: Float) {
        _fabOffsetX.value = x
        _fabOffsetY.value = y
    }

    fun resetFabPosition() {
        _fabOffsetX.value = 0f
        _fabOffsetY.value = -240f
    }

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insert(Task(title = title))
        }
    }

    fun toggleTaskCompletion(task: Task) {
        val newStatus = !task.isCompleted
        val updatedTask = task.copy(
            isCompleted = newStatus,
            completedAt = if (newStatus) System.currentTimeMillis() else null
        )

        viewModelScope.launch {
            taskDao.update(updatedTask)

            if (newStatus) {
                if (!_hideImmediately.value) {
                    val currentList = _recentlyCompletedTasks.value.toMutableList()
                    currentList.add(updatedTask)
                    _recentlyCompletedTasks.value = currentList

                    delay(15 * 60 * 1000L)
                    
                    val listAfterDelay = _recentlyCompletedTasks.value.toMutableList()
                    listAfterDelay.removeAll { it.id == updatedTask.id }
                    _recentlyCompletedTasks.value = listAfterDelay
                }
            } else {
                val currentList = _recentlyCompletedTasks.value.toMutableList()
                currentList.removeAll { it.id == updatedTask.id }
                _recentlyCompletedTasks.value = currentList
            }
        }
    }

    // BACKUP & RESTORE
    fun exportBackup(context: Context, uri: Uri, themeController: ThemeController, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val allTasks = taskDao.getAllTasksDirect()
                val backup = AppBackup(
                    tasks = allTasks.map { TaskBackup(it.title, it.isCompleted, it.createdAt, it.completedAt) },
                    settings = SettingsBackup(
                        themeMode = themeController.themeMode.name,
                        appLanguage = themeController.appLanguage.name,
                        accentColor = themeController.accentColor.name,
                        customAccentColor = themeController.customAccentColor.toArgb().toLong(),
                        fabOffsetX = _fabOffsetX.value,
                        fabOffsetY = _fabOffsetY.value,
                        hideImmediately = _hideImmediately.value
                    )
                )
                val json = Json.encodeToString(backup)
                context.contentResolver.openOutputStream(uri)?.use { 
                    it.write(json.toByteArray())
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importBackup(context: Context, uri: Uri, themeController: ThemeController, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            } ?: run {
                onError()
                return@launch
            }
            
            try {
                val backup = Json.decodeFromString<AppBackup>(content)
                
                // 1. Clear and Restore DB
                taskDao.deleteAllTasks()
                taskDao.insertAll(backup.tasks.map { 
                    Task(title = it.title, isCompleted = it.isCompleted, createdAt = it.createdAt, completedAt = it.completedAt)
                })

                // 2. Restore Viewmodel State
                _fabOffsetX.value = backup.settings.fabOffsetX
                _fabOffsetY.value = backup.settings.fabOffsetY
                _hideImmediately.value = backup.settings.hideImmediately
                
                // 3. Restore UI State (Language, Theme, Accent)
                try {
                    themeController.setThemeMode(AppThemeMode.valueOf(backup.settings.themeMode))
                    themeController.setAppLanguage(AppLanguage.valueOf(backup.settings.appLanguage))
                    themeController.setAccentColor(AppAccentColor.valueOf(backup.settings.accentColor))
                    themeController.setCustomAccentColor(Color(backup.settings.customAccentColor.toInt()))
                } catch (e: Exception) { /* Fallback for older backups */ }
                
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }
}