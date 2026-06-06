package com.m4sak1.taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.m4sak1.taskapp.R

private data class LicenseItem(val name: String, val author: String, val license: String)

private val licenses = listOf(
    LicenseItem("Kotlin Standard Library", "JetBrains s.r.o.", "Apache License 2.0"),
    LicenseItem("Kotlinx Coroutines", "JetBrains s.r.o.", "Apache License 2.0"),
    LicenseItem("AndroidX Core KTX", "Google LLC", "Apache License 2.0"),
    LicenseItem("AndroidX Lifecycle", "Google LLC", "Apache License 2.0"),
    LicenseItem("AndroidX Activity Compose", "Google LLC", "Apache License 2.0"),
    LicenseItem("Jetpack Compose UI", "Google LLC", "Apache License 2.0"),
    LicenseItem("Jetpack Compose Material 3", "Google LLC", "Apache License 2.0"),
    LicenseItem("Jetpack Compose Foundation", "Google LLC", "Apache License 2.0"),
    LicenseItem("AndroidX Navigation Compose", "Google LLC", "Apache License 2.0"),
    LicenseItem("AndroidX Room", "Google LLC", "Apache License 2.0"),
    LicenseItem("AndroidX SQLite", "Google LLC", "Apache License 2.0"),
    LicenseItem("Kotlin Symbol Processing (KSP)", "Google LLC", "Apache License 2.0")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_licenses)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(licenses) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Copyright © ${item.author}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.license,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            }
        }
    }
}