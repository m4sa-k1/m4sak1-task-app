package com.m4sak1.taskapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.m4sak1.taskapp.data.AppDatabase
import com.m4sak1.taskapp.data.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    // 完了した直後のタスクをメモリ上で保持するリスト
    private val _recentlyCompletedTasks = MutableStateFlow<List<Task>>(emptyList())

    // UIに表示するタスク（DBの未完了タスク + メモリ上の完了直後タスク）
    val uiTasks: StateFlow<List<Task>> = combine(
        taskDao.getIncompleteTasks(),
        _recentlyCompletedTasks
    ) { incomplete, completed ->
        (incomplete + completed).sortedByDescending { it.id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insert(Task(title = title))
        }
    }

    fun toggleTaskCompletion(task: Task) {
        val newStatus = !task.isCompleted
        val updatedTask = task.copy(isCompleted = newStatus)

        viewModelScope.launch {
            // DBを更新
            taskDao.update(updatedTask)

            if (newStatus) {
                // 完了状態になった場合、メモリに追加して15分ディレイを開始
                val currentList = _recentlyCompletedTasks.value.toMutableList()
                currentList.add(updatedTask)
                _recentlyCompletedTasks.value = currentList

                // 15分（900,000ミリ秒）後にメモリから削除してUIから消す
                delay(15 * 60 * 1000L)
                
                val listAfterDelay = _recentlyCompletedTasks.value.toMutableList()
                listAfterDelay.removeAll { it.id == updatedTask.id }
                _recentlyCompletedTasks.value = listAfterDelay
            } else {
                // 未完了に戻された場合、メモリから削除（DBの未完了フローに復活する）
                val currentList = _recentlyCompletedTasks.value.toMutableList()
                currentList.removeAll { it.id == updatedTask.id }
                _recentlyCompletedTasks.value = currentList
            }
        }
    }
}