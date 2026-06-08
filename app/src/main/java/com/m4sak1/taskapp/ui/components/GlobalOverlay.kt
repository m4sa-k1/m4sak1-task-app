package com.m4sak1.taskapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

val LocalGlobalOverlay = compositionLocalOf<MutableState<(@Composable () -> Unit)?>> {
    error("No GlobalOverlay provided")
}
