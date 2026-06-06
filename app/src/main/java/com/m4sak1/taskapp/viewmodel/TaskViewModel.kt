package com.m4sak1.taskapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.m4sak1.taskapp.data.*
import com.m4sak1.taskapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    private val _recentlyCompletedTasks = MutableStateFlow<List<Task>>(emptyList())
    
    private val _hideImmediately = MutableStateFlow(false)
    val hideImmediately = _hideImmediately.asStateFlow()

    private val _fabOffsetX = MutableStateFlow(0f)
    val fabOffsetX = _fabOffsetX.asStateFlow()
    private val _fabOffsetY = MutableStateFlow(-240f) 
    val fabOffsetY = _fabOffsetY.asStateFlow()

    private val _backgroundPath = MutableStateFlow<String?>(null)
    val backgroundPath = _backgroundPath.asStateFlow()

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

    fun updateBackgroundPath(path: String?) {
        _backgroundPath.value = path
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

    // ZIP BACKUP & RESTORE
    fun exportBackupZip(context: Context, uri: Uri, themeController: ThemeController, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allTasks = taskDao.getAllTasksDirect()
                val backupData = AppBackup(
                    tasks = allTasks.map { TaskBackup(it.title, it.isCompleted, it.createdAt, it.completedAt) },
                    settings = SettingsBackup(
                        themeMode = themeController.themeMode.name,
                        appLanguage = themeController.appLanguage.name,
                        accentColor = themeController.accentColor.name,
                        customAccentColor = themeController.customAccentColor.toArgb().toLong(),
                        backgroundBlur = themeController.backgroundBlur,
                        hasBackground = _backgroundPath.value != null,
                        fabOffsetX = _fabOffsetX.value,
                        fabOffsetY = _fabOffsetY.value,
                        hideImmediately = _hideImmediately.value
                    )
                )
                val json = Json.encodeToString(backupData)

                context.contentResolver.openOutputStream(uri)?.use { os ->
                    ZipOutputStream(os).use { zos ->
                        zos.putNextEntry(ZipEntry("backup.json"))
                        zos.write(json.toByteArray())
                        zos.closeEntry()

                        _backgroundPath.value?.let { path ->
                            val bgFile = File(path)
                            if (bgFile.exists()) {
                                zos.putNextEntry(ZipEntry("background.jpg"))
                                bgFile.inputStream().use { it.copyTo(zos) }
                                zos.closeEntry()
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importBackupZip(context: Context, uri: Uri, themeController: ThemeController, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var backupData: AppBackup? = null
                var hasBgInZip = false
                val tempBgFile = File(context.filesDir, "temp_bg.jpg")

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zis ->
                        var entry: ZipEntry? = zis.nextEntry
                        while (entry != null) {
                            when (entry.name) {
                                "backup.json" -> {
                                    val jsonContent = zis.bufferedReader().readText()
                                    backupData = Json.decodeFromString<AppBackup>(jsonContent)
                                }
                                "background.jpg" -> {
                                    FileOutputStream(tempBgFile).use { zis.copyTo(it) }
                                    hasBgInZip = true
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                }

                val backup = backupData ?: throw Exception("No JSON in ZIP")

                taskDao.deleteAllTasks()
                taskDao.insertAll(backup.tasks.map { 
                    Task(title = it.title, isCompleted = it.isCompleted, createdAt = it.createdAt, completedAt = it.completedAt)
                })

                withContext(Dispatchers.Main) {
                    _fabOffsetX.value = backup.settings.fabOffsetX
                    _fabOffsetY.value = backup.settings.fabOffsetY
                    _hideImmediately.value = backup.settings.hideImmediately
                    
                    themeController.setThemeMode(AppThemeMode.valueOf(backup.settings.themeMode))
                    themeController.setAppLanguage(AppLanguage.valueOf(backup.settings.appLanguage))
                    themeController.setAccentColor(AppAccentColor.valueOf(backup.settings.accentColor))
                    themeController.setCustomAccentColor(Color(backup.settings.customAccentColor.toInt()))
                    themeController.setBackgroundBlur(backup.settings.backgroundBlur)
                    
                    if (hasBgInZip) {
                        val realBgFile = File(context.filesDir, "background.jpg")
                        tempBgFile.renameTo(realBgFile)
                        _backgroundPath.value = realBgFile.absolutePath
                    } else {
                        _backgroundPath.value = null
                    }
                    
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError() }
            }
        }
    }
}