package com.m4sak1.taskapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.m4sak1.taskapp.ui.components.FloatingBottomNav
import com.m4sak1.taskapp.ui.screens.HomeScreen
import com.m4sak1.taskapp.ui.screens.SettingsScreen
import com.m4sak1.taskapp.ui.screens.StatsScreen

enum class ScreenTab { Home, Stats, Settings }

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(ScreenTab.Home) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentTab) {
            ScreenTab.Home -> HomeScreen()
            ScreenTab.Stats -> StatsScreen()
            ScreenTab.Settings -> SettingsScreen()
        }

        FloatingBottomNav(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}