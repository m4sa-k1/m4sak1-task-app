package com.m4sak1.taskapp.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import com.m4sak1.taskapp.MainActivity
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.data.AppDatabase
import com.m4sak1.taskapp.data.PreferenceManager
import com.m4sak1.taskapp.ui.theme.AppThemeMode
import com.m4sak1.taskapp.ui.theme.AppAccentColor
import com.m4sak1.taskapp.ui.theme.getAppColorScheme
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first

class TaskAppWidget : GlanceAppWidget() {
    companion object {
        val filterKey = booleanPreferencesKey("filter_starred_only")
        val updateTriggerKey = longPreferencesKey("update_trigger")
        val taskIdKey = ActionParameters.Key<Int>("taskId")

        suspend fun forceUpdate(context: Context) {
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(TaskAppWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    val current = prefs[updateTriggerKey] ?: 0L
                    prefs[updateTriggerKey] = current + 1
                }
                TaskAppWidget().update(context, glanceId)
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch data
        val prefManager = PreferenceManager(context)
        val isDark = prefManager.themeMode == AppThemeMode.Dark || 
                    (prefManager.themeMode == AppThemeMode.System && 
                    (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES))
        
        val accentColorEnum = prefManager.accentColor
        val customColor = try { Color(prefManager.customAccentColor) } catch(e: Exception) { Color.Unspecified }
        val effectiveColor = if (accentColorEnum == AppAccentColor.Custom) customColor else accentColorEnum.color
        val colors = getAppColorScheme(isDark, effectiveColor)
        
        // This is called whenever the widget needs to update.
        // We use provideContent so we can read preferences inside composables.
        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val showStarredOnly = prefs[filterKey] ?: false
            val updateTrigger = prefs[updateTriggerKey] ?: 0L
            
            WidgetUI(context, showStarredOnly, colors, updateTrigger)
        }
    }

    @Composable
    private fun WidgetUI(context: Context, showStarredOnly: Boolean, colors: androidx.compose.material3.ColorScheme, updateTrigger: Long) {
        // Fetch tasks synchronously since we update the widget on DB changes
        // Warning: This blocks the composition if not careful, but Glance allows synchronous DB reads here
        val db = AppDatabase.getDatabase(context)
        // This is a bit of a hack in Glance: we can read blocking because it's a RemoteViews generation pass
        val allTasks = kotlinx.coroutines.runBlocking { db.taskDao().getAllTasksDirect() }
        val incompleteTasks = allTasks.filter { !it.isCompleted }.sortedWith(
            compareByDescending<com.m4sak1.taskapp.data.Task> { it.isStarred }.thenByDescending { it.id }
        )
        
        val tasksToShow = if (showStarredOnly) incompleteTasks.filter { it.isStarred } else incompleteTasks

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(Color(colors.surface.value))
                .cornerRadius(16.dp)
                .clickable(onClick = actionStartActivity<MainActivity>())
                .padding(16.dp),
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle Filter Button
                Text(
                    text = if (showStarredOnly) context.getString(R.string.filter_starred) else context.getString(R.string.filter_all),
                    style = TextStyle(
                        color = ColorProvider(colors.primary),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier
                        .clickable(onClick = actionRunCallback<ToggleFilterAction>())
                        .padding(8.dp)
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Add Button
                Text(
                    text = "+",
                    style = TextStyle(
                        color = ColorProvider(colors.primary),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(8.dp).clickable(
                        onClick = actionStartActivity<WidgetAddActivity>()
                    )
                )
            }

            if (tasksToShow.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = context.getString(R.string.no_tasks),
                        style = TextStyle(color = ColorProvider(colors.onSurface.copy(alpha = 0.5f)), fontSize = 14.sp)
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(tasksToShow) { task ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable(
                                    onClick = actionRunCallback<ToggleTaskAction>(
                                        parameters = actionParametersOf(taskIdKey to task.id)
                                    )
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Star or Circle text
                            Text(
                                text = if (task.isStarred) "☆" else "○",
                                style = TextStyle(
                                    color = ColorProvider(colors.onSurface),
                                    fontSize = 24.sp
                                ),
                                modifier = GlanceModifier.padding(end = 8.dp)
                            )
                            
                            Text(
                                text = task.title,
                                style = TextStyle(
                                    color = ColorProvider(colors.onSurface),
                                    fontSize = 18.sp
                                ),
                                maxLines = 1, // Truncate
                            )
                        }
                    }
                }
            }
        }
    }
}
