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
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.m4sak1.taskapp.worker.NotificationWorker

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    private val prefManager = PreferenceManager(application)

    private val _recentlyCompletedTasks = MutableStateFlow<List<Task>>(emptyList())
    
    private val _hideImmediately = MutableStateFlow(prefManager.hideImmediately)
    val hideImmediately = _hideImmediately.asStateFlow()

    private val _disableAnimations = MutableStateFlow(prefManager.disableAnimations)
    val disableAnimations = _disableAnimations.asStateFlow()

    private val _enterToAdd = MutableStateFlow(prefManager.enterToAdd)
    val enterToAdd = _enterToAdd.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefManager.notificationsEnabled)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _fabOffsetX = MutableStateFlow(prefManager.fabOffsetX)
    val fabOffsetX = _fabOffsetX.asStateFlow()
    private val _fabOffsetY = MutableStateFlow(prefManager.fabOffsetY) 
    val fabOffsetY = _fabOffsetY.asStateFlow()

    private val _backgroundPath = MutableStateFlow(prefManager.backgroundPath)
    val backgroundPath = _backgroundPath.asStateFlow()

    // Version counter: increments every time background image is updated (even if path is the same)
    // This forces bitmap reloading in MainScreen even when the file is overwritten at the same path
    private val _backgroundVersion = MutableStateFlow(0)
    val backgroundVersion = _backgroundVersion.asStateFlow()

    val allCompletedTasks: Flow<List<Task>> = taskDao.getAllCompletedTasks()

    // CRITICAL FIX: Ensure no duplicate keys by filtering out tasks that are already in recentlyCompleted
    val uiTasks: StateFlow<List<Task>> = combine(
        taskDao.getIncompleteTasks(),
        _recentlyCompletedTasks
    ) { incomplete, recentlyCompleted ->
        // Filter out any incomplete tasks from DB that might somehow also be in the memory list
        // (Though usually they shouldn't overlap if DB is updated first)
        val recentlyIds = recentlyCompleted.map { it.id }.toSet()
        val filteredIncomplete = incomplete.filter { it.id !in recentlyIds }
        (filteredIncomplete + recentlyCompleted).sortedByDescending { it.id }
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
        prefManager.hideImmediately = hide
        if (hide) {
            _recentlyCompletedTasks.value = emptyList()
        }
    }

    fun toggleDisableAnimations(disable: Boolean) {
        _disableAnimations.value = disable
        prefManager.disableAnimations = disable
    }

    fun setEnterToAdd(enabled: Boolean) {
        _enterToAdd.value = enabled
        prefManager.enterToAdd = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefManager.notificationsEnabled = enabled
    }

    fun updateFabPosition(x: Float, y: Float) {
        _fabOffsetX.value = x
        _fabOffsetY.value = y
        prefManager.fabOffsetX = x
        prefManager.fabOffsetY = y
    }

    fun resetFabPosition() {
        val dx = 0f
        val dy = -240f
        _fabOffsetX.value = dx
        _fabOffsetY.value = dy
        prefManager.fabOffsetX = dx
        prefManager.fabOffsetY = dy
    }

    fun updateBackgroundPath(path: String?) {
        _backgroundPath.value = path
        prefManager.backgroundPath = path
        _backgroundVersion.value += 1  // Force bitmap reload in MainScreen
    }

    fun addTask(title: String, isStarred: Boolean = false) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insert(Task(title = title, isStarred = isStarred))
            
            if (_notificationsEnabled.value) {
                val inputData = Data.Builder()
                    .putString("task_title", title)
                    .build()
                
                val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(1, TimeUnit.DAYS)
                    .setInputData(inputData)
                    .build()
                    
                WorkManager.getInstance(getApplication()).enqueue(workRequest)
            }
        }
    }

    fun sendTestNotification() {
        val inputData = Data.Builder()
            .putString("task_title", "Test Task")
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputData)
            .build()
            
        WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }

    fun toggleTaskCompletion(task: Task) {
        val newStatus = !task.isCompleted
        val updatedTask = task.copy(
            isCompleted = newStatus,
            completedAt = if (newStatus) System.currentTimeMillis() else null
        )

        viewModelScope.launch {
            // 1. Update DB immediately
            taskDao.update(updatedTask)

            if (newStatus) {
                // 2. Handle memory list for 15m delay
                if (!_hideImmediately.value) {
                    val currentList = _recentlyCompletedTasks.value.toMutableList()
                    // Remove old version if exists (safety)
                    currentList.removeAll { it.id == task.id }
                    currentList.add(updatedTask)
                    _recentlyCompletedTasks.value = currentList

                    delay(15 * 60 * 1000L)
                    
                    val listAfterDelay = _recentlyCompletedTasks.value.toMutableList()
                    listAfterDelay.removeAll { it.id == updatedTask.id }
                    _recentlyCompletedTasks.value = listAfterDelay
                }
            } else {
                // 3. Reverting completion: remove from memory list so it appears in DB incomplete flow
                val currentList = _recentlyCompletedTasks.value.toMutableList()
                currentList.removeAll { it.id == updatedTask.id }
                _recentlyCompletedTasks.value = currentList
            }
        }
    }

    fun deleteTasks(tasks: List<Task>) {
        viewModelScope.launch {
            taskDao.deleteTasks(tasks)
            val deletedIds = tasks.map { it.id }.toSet()
            val currentList = _recentlyCompletedTasks.value.toMutableList()
            currentList.removeAll { deletedIds.contains(it.id) }
            _recentlyCompletedTasks.value = currentList
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
                        hideImmediately = _hideImmediately.value,
                        disableAnimations = _disableAnimations.value,
                        enterToAdd = _enterToAdd.value
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
                    _disableAnimations.value = backup.settings.disableAnimations
                    _enterToAdd.value = backup.settings.enterToAdd
                    
                    prefManager.fabOffsetX = backup.settings.fabOffsetX
                    prefManager.fabOffsetY = backup.settings.fabOffsetY
                    prefManager.hideImmediately = backup.settings.hideImmediately
                    prefManager.disableAnimations = backup.settings.disableAnimations
                    prefManager.enterToAdd = backup.settings.enterToAdd
                    prefManager.themeMode = AppThemeMode.valueOf(backup.settings.themeMode)
                    prefManager.appLanguage = AppLanguage.valueOf(backup.settings.appLanguage)
                    prefManager.accentColor = AppAccentColor.valueOf(backup.settings.accentColor)
                    prefManager.customAccentColor = backup.settings.customAccentColor.toInt()
                    prefManager.backgroundBlur = backup.settings.backgroundBlur

                    themeController.setThemeMode(prefManager.themeMode)
                    themeController.setAppLanguage(prefManager.appLanguage)
                    themeController.setAccentColor(prefManager.accentColor)
                    themeController.setCustomAccentColor(Color(prefManager.customAccentColor))
                    themeController.setBackgroundBlur(prefManager.backgroundBlur)
                    
                    if (hasBgInZip) {
                        val realBgFile = File(context.filesDir, "background.jpg")
                        tempBgFile.renameTo(realBgFile)
                        _backgroundPath.value = realBgFile.absolutePath
                        prefManager.backgroundPath = realBgFile.absolutePath
                    } else {
                        _backgroundPath.value = null
                        prefManager.backgroundPath = null
                    }
                    _backgroundVersion.value += 1  // Force bitmap reload after restore
                    
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError() }
            }
        }
    }
}