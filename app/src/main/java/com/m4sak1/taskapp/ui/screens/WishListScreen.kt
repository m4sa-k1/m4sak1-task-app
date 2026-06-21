package com.m4sak1.taskapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WishListScreen(viewModel: TaskViewModel) {
    val items by viewModel.wishListTasks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.tab_stats),
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_wishlist_items),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Box(modifier = Modifier.animateItemPlacement()) {
                        WishListItem(
                            task = item,
                            onToggle = { viewModel.toggleWishListItemCompletion(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishListItem(task: Task, onToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null) { onToggle() }
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle checkbox (no star shape, no starred feature for wishlist)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .then(
                        if (task.isCompleted) {
                            Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        } else {
                            Modifier.border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), CircleShape)
                        }
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            val textColor = if (task.isCompleted)
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.onSurface

            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                color = textColor,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    }
}
