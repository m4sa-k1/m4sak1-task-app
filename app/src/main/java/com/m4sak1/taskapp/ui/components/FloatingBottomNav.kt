package com.m4sak1.taskapp.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.m4sak1.taskapp.ScreenTab
import com.m4sak1.taskapp.ui.theme.LocalThemeController

import androidx.compose.foundation.border
import androidx.compose.ui.res.stringResource
import com.m4sak1.taskapp.R

val BarChartIcon: ImageVector
    get() = ImageVector.Builder(
        name = "BarChart",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 20f); lineTo(18f, 10f)
            moveTo(12f, 20f); lineTo(12f, 4f)
            moveTo(6f, 20f); lineTo(6f, 14f)
        }
    }.build()

@Composable
fun FloatingBottomNav(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = ScreenTab.entries.toTypedArray()
    val selectedIndex = tabs.indexOf(currentTab)
    val isDarkTheme = LocalThemeController.current.isDarkTheme

    Box(
        modifier = modifier
            .padding(bottom = 32.dp)
            .fillMaxWidth(0.75f)
            .height(64.dp)
            .border(
                width = 1.dp,
                color = if (isDarkTheme) Color.Transparent else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(32.dp)
            )
            .background(
                if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFFAFAFA),
                RoundedCornerShape(32.dp)
            )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val constraintsScope = this
            val tabWidth = constraintsScope.maxWidth / tabs.size
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "indicatorOffset"
            )

            // Animated Black Circle
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
            }

            // Icons
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex
                    val icon = when(tab) {
                        ScreenTab.Home -> Icons.Outlined.Home
                        ScreenTab.Stats -> BarChartIcon
                        ScreenTab.Settings -> Icons.Outlined.Settings
                    }
                    val iconColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                onTabSelected(tab)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}