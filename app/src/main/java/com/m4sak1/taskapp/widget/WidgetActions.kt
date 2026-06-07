package com.m4sak1.taskapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.m4sak1.taskapp.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[ActionParameters.Key<Int>("taskId")] ?: return
        
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.taskDao()
            val task = dao.getTaskById(taskId)
            if (task != null) {
                // Widget only allows completing tasks
                if (!task.isCompleted) {
                    val updatedTask = task.copy(
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                    dao.update(updatedTask)
                    
                    // Update all widgets
                    TaskAppWidget.forceUpdate(context)
                }
            }
        }
    }
}

class ToggleFilterAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[TaskAppWidget.filterKey] ?: false
            prefs.toMutablePreferences().apply {
                this[TaskAppWidget.filterKey] = !current
            }
        }
        TaskAppWidget.forceUpdate(context)
    }
}
