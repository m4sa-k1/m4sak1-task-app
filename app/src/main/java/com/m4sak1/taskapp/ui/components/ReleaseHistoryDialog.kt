package com.m4sak1.taskapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m4sak1.taskapp.BuildConfig
import com.m4sak1.taskapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class GithubRelease(
    val name: String,
    val tagName: String,
    val publishedAt: String,
    val body: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseHistoryDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    disableAnimations: Boolean
) {
    if (!visible && disableAnimations) return

    var releases by remember { mutableStateOf<List<GithubRelease>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(visible) {
        if (visible) {
            isLoading = true
            errorMessage = null
            try {
                val fetchedReleases = fetchReleases()
                releases = fetchedReleases
            } catch (e: Exception) {
                errorMessage = "Failed to load release history."
            } finally {
                isLoading = false
            }
        }
    }

    CustomInfoDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = "リリース履歴",
        confirmText = stringResource(R.string.close),
        disableAnimations = disableAnimations
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 500.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (releases.isNullOrEmpty()) {
                Text(
                    text = "リリースが見つかりませんでした。",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(releases!!) { index, release ->
                        ReleaseItem(
                            release = release,
                            isLatest = index == 0,
                            disableAnimations = disableAnimations
                        )
                        if (index < releases!!.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReleaseItem(
    release: GithubRelease,
    isLatest: Boolean,
    disableAnimations: Boolean
) {
    var isExpanded by rememberSaveable { mutableStateOf(isLatest) }
    val isNewUpdate = isLatest && release.tagName.removePrefix("v") != BuildConfig.VERSION_NAME

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { isExpanded = !isExpanded })
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = release.name.ifEmpty { release.tagName },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isNewUpdate) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "New Update!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = release.publishedAt.take(10), // YYYY-MM-DD
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp).size(20.dp)
                )
            }
        }

        if (disableAnimations) {
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                SimpleMarkdownText(text = release.body)
            }
        } else {
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    SimpleMarkdownText(text = release.body)
                }
            }
        }
    }
}

@Composable
fun SimpleMarkdownText(text: String) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            when {
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# ").trim(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## ").trim(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### ").trim(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                    val indentSize = line.takeWhile { it == ' ' }.length
                    val content = line.trimStart().removePrefix("- ").removePrefix("* ").trim()
                    Row(modifier = Modifier.padding(start = (indentSize * 4 + 8).dp)) {
                        Text(
                            text = "• ",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = parseBoldMarkdown(content),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
                line.isNotBlank() -> {
                    Text(
                        text = parseBoldMarkdown(line),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

fun parseBoldMarkdown(text: String) = buildAnnotatedString {
    val parts = text.split("**")
    for ((index, part) in parts.withIndex()) {
        if (index % 2 == 1) {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(part)
            }
        } else {
            append(part)
        }
    }
}

private suspend fun fetchReleases(): List<GithubRelease> = withContext(Dispatchers.IO) {
    val url = URL("https://api.github.com/repos/masaki-09/m4sak1-task-app/releases")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
    connection.connectTimeout = 10000
    connection.readTimeout = 10000

    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(response)
        val releases = mutableListOf<GithubRelease>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            releases.add(
                GithubRelease(
                    name = obj.optString("name", ""),
                    tagName = obj.optString("tag_name", ""),
                    publishedAt = obj.optString("published_at", ""),
                    body = obj.optString("body", "")
                )
            )
        }
        return@withContext releases
    } else {
        throw Exception("Failed to fetch. Code: ${connection.responseCode}")
    }
}
