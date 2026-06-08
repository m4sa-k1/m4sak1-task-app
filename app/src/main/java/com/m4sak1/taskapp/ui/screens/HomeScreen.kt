package com.m4sak1.taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m4sak1.taskapp.data.Task
import com.m4sak1.taskapp.viewmodel.TaskViewModel

import androidx.compose.ui.res.stringResource
import com.m4sak1.taskapp.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.m4sak1.taskapp.ui.theme.LocalThemeController

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.uiTasks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.title_tasks),
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.no_tasks), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    Box(modifier = Modifier.animateItemPlacement()) {
                        TaskItem(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task) }
                        )
                    }
                }
            }
        }
    }
}

class StarShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width / 2f
        val innerRadius = outerRadius * 0.382f // Golden ratio roughly for a 5-point star
        
        for (i in 0 until 10) {
            val angle = (Math.PI / 5) * i - Math.PI / 2
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val x = center.x + cos(angle).toFloat() * radius
            val y = center.y + sin(angle).toFloat() * radius
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val rowModifier = Modifier
        .fillMaxWidth()
        .clickable(interactionSource = interactionSource, indication = null) { onToggle() }
        .padding(vertical = 16.dp)
    
    Column {
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val shape = if (task.isStarred) StarShape() else CircleShape
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(shape)
                    .then(
                        if (task.isCompleted) {
                            Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        } else {
                            val borderThickness = if (task.isStarred) 1.5.dp else 1.dp
                            Modifier.border(borderThickness, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), shape)
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = task.title,
                fontSize = 18.sp,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    }
}