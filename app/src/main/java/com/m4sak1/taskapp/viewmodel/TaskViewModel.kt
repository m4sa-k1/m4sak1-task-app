package com.m4sak1.taskapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.m4sak1.taskapp.data.AppDatabase
import com.m4sak1.taskapp.data.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    // 完了直後の一時保持
    private val _recentlyCompletedTasks = MutableStateFlow<List<Task>>(emptyList())
    
    // 設定：即座に非表示にするか
    private val _hideImmediately = MutableStateFlow(false)
    val hideImmediately = _hideImmediately.asStateFlow()

    // FAB Position (Offsets from bottom-right default)
    // Default Y is set to -80dp (approx -240px on many screens) to clear the footer
    private val _fabOffsetX = MutableStateFlow(0f)
    val fabOffsetX = _fabOffsetX.asStateFlow()
    private val _fabOffsetY = MutableStateFlow(-240f) 
    val fabOffsetY = _fabOffsetY.asStateFlow()

    // 全完了タスク
    val allCompletedTasks: Flow<List<Task>> = taskDao.getAllCompletedTasks()

    // ホーム表示用
    val uiTasks: StateFlow<List<Task>> = combine(
        taskDao.getIncompleteTasks(),
        _recentlyCompletedTasks
    ) { incomplete, recentlyCompleted ->
        (incomplete + recentlyCompleted).sortedByDescending { it.id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 統計データ
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
}